package com.example.musicbasic.ui.main

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicbasic.extensions.toMediaItem
import com.example.musicbasic.extensions.toRepeatMode
import com.example.musicbasic.repository.MusicRepository
import com.example.musicbasic.service.MusicService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class MainViewModel @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
    musicRepository: MusicRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainState())
    val uiState: StateFlow<MainState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MainEffect>()
    val effect: SharedFlow<MainEffect> = _effect.asSharedFlow()

    private val sessionToken = SessionToken(
        context,
        ComponentName(
            context,
            MusicService::class.java
        )
    )

    private val controllerFuture =
        MediaController.Builder(
            context,
            sessionToken
        ).buildAsync()

    private var mediaController: MediaController? = null

    private var progressJob: Job? = null
    private val musicList = musicRepository.getMusics()

    private val playerListener = object : Player.Listener {

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateCurrentMusic()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _uiState.update {
                it.copy(
                    repeatMode = repeatMode.toRepeatMode(),
                )
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    updatePlaybackProgress()
                }

                Player.STATE_ENDED -> {
                    updatePlaybackProgress()
                    _uiState.update {
                        it.copy(isPlaying = false)
                    }
                }

                else -> Unit
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { currentState ->
                currentState.copy(isPlaying = isPlaying)
            }

            if (isPlaying) {
                startProgressUpdates()
            } else {
                stopProgressUpdates()
                updatePlaybackProgress()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            stopProgressUpdates()

            _uiState.update { currentState ->
                currentState.copy(isPlaying = false)
            }

            Timber.e(error, "Playback error")
        }
    }

    private fun updateCurrentMusic() {
        val controller = mediaController ?: return
        val currentMusicIndex = controller.currentMediaItemIndex
        val currentMusic = musicList.getOrNull(
            currentMusicIndex
        ) ?: return

        _uiState.update {
            it.copy(
                currentMusic = currentMusic,
                currentMusicIndex = currentMusicIndex,
                progress = 0f,
                currentPositionMs = 0L,
                durationMs = 0L,
            )
        }
    }

    private fun startProgressUpdates() {
        if (progressJob?.isActive == true) return

        progressJob = viewModelScope.launch {
            while (isActive) {
                updatePlaybackProgress()
                delay(500.milliseconds)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updatePlaybackProgress() {
        val controller = mediaController ?: return

        val currentPositionMs =
            controller.currentPosition.coerceAtLeast(0L)

        val playerDurationMs = controller.duration

        val hasValidDuration =
            playerDurationMs != C.TIME_UNSET &&
                    playerDurationMs > 0L

        _uiState.update { currentState ->
            val durationMs = if (hasValidDuration) {
                playerDurationMs
            } else {
                currentState.durationMs
            }

            val progress = if (hasValidDuration) {
                calculateProgress(
                    currentPositionMs = currentPositionMs,
                    durationMs = playerDurationMs,
                )
            } else {
                currentState.progress
            }

            currentState.copy(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                progress = progress,
            )
        }
    }

    private fun calculateProgress(
        currentPositionMs: Long,
        durationMs: Long,
    ): Float {
        return (currentPositionMs.toFloat() / durationMs.toFloat())
            .coerceIn(0f, 1f)
    }

    init {
        connectToMusicService()
    }

    private fun connectToMusicService() {
        controllerFuture.addListener(
            {
                try {
                    val controller = controllerFuture.get()

                    mediaController = controller
                    controller.addListener(playerListener)

                    initPlaylist(controller)
                    synchronizePlayerState(controller)
                } catch (exception: Exception) {
                    Timber.e(
                        exception,
                        "Cannot connect to MusicService"
                    )
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun initPlaylist(
        controller: MediaController,
    ) {
        if (musicList.isEmpty()) {
            return
        }
        /*
        * Không tạo lại playlist mỗi lần UI kết nối lại.
        *
        * Ví dụ:
        * - xoay màn hình
        * - trở lại app sau khi bấm Home
        * - ViewModel được tạo lại
        */
        if (controller.mediaItemCount > 0) return

        val mediaItems = musicList.map { music ->
            music.toMediaItem(context = context)
        }
        controller.setMediaItems(mediaItems)
        controller.prepare()

        playMusicAt()
    }

    private fun synchronizePlayerState(
        controller: MediaController,
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                isPlaying = controller.isPlaying,
                currentMusicIndex = controller.currentMediaItemIndex,
                repeatMode = controller.repeatMode.toRepeatMode(),
                isShuffleEnabled = controller.shuffleModeEnabled,
            )
        }

        updateCurrentMusic()
        updatePlaybackProgress()

        if (controller.isPlaying) {
            startProgressUpdates()
        }
    }

    private fun playMusicAt(index: Int = 0) {
        val controller = mediaController ?: return

        if (index !in musicList.indices) return

        controller.seekTo(
            index,
            C.TIME_UNSET
        )

        controller.play()
    }

    fun onEvent(event: MainEvent) {
        when (event) {
            MainEvent.ShuffleClicked -> {
                actionShuffle()
            }

            MainEvent.PreviousClicked -> {
                actionPrevious()
            }

            MainEvent.PlayPauseClicked -> {
                togglePlayback()
            }

            MainEvent.NextClicked -> {
                actionNext()
            }

            MainEvent.RepeatClicked -> {
                actionRepeat()
            }

            is MainEvent.SeekChanged -> {
                seekToProgress(progress = event.progress)
            }
        }
    }

    private fun seekToProgress(
        progress: Float
    ) {
        val controller = mediaController ?: return
        val durationMs = controller.duration

        val hasValidDuration =
            durationMs != C.TIME_UNSET &&
                    durationMs > 0L

        if (!hasValidDuration) return

        val normalizedProgress =
            progress.coerceIn(0f, 1f)

        val targetPositionMs =
            (durationMs.toDouble() * normalizedProgress)
                .roundToLong()
                .coerceIn(0L, durationMs)

        controller.seekTo(targetPositionMs)
        controller.play()

        _uiState.update { currentState ->
            currentState.copy(
                currentPositionMs = targetPositionMs,
                progress = normalizedProgress,
            )
        }
    }

    private fun actionShuffle() {
        val controller = mediaController ?: return

        val newShuffleMode = !controller.shuffleModeEnabled

        controller.shuffleModeEnabled = newShuffleMode

        _uiState.update {
            it.copy(
                isShuffleEnabled = newShuffleMode,
            )
        }
    }

    private fun actionRepeat() {
        val controller = mediaController ?: return

        val newPlayerRepeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_OFF
        }

        controller.repeatMode = newPlayerRepeatMode

        _uiState.update {
            it.copy(
                repeatMode = newPlayerRepeatMode.toRepeatMode(),
            )
        }
    }

    private fun actionPrevious() {
        val controller = mediaController ?: return

        if (!controller.hasPreviousMediaItem()) {
            controller.seekTo(0L)
            viewModelScope.launch {
                _effect.emit(
                    MainEffect.MusicMessage(message = "No previous music")
                )
            }
            return
        }
        controller.seekToPreviousMediaItem()
        controller.play()
    }

    private fun actionNext() {
        val controller = mediaController ?: return

        if (!controller.hasNextMediaItem()) {
            viewModelScope.launch {
                _effect.emit(
                    MainEffect.MusicMessage(message = "No next music")
                )
            }
            return
        }
        controller.seekToNextMediaItem()
        controller.play()
    }

    private fun togglePlayback() {
        val controller = mediaController ?: return

        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    override fun onCleared() {
        stopProgressUpdates()

        mediaController?.removeListener(playerListener)
        mediaController = null

        MediaController.releaseFuture(controllerFuture)
        super.onCleared()
    }

}
package com.example.musicbasic.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicbasic.extensions.toAndroidResourceUri
import com.example.musicbasic.extensions.toRepeatMode
import com.example.musicbasic.repository.MusicRepository
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
    private val musicRepository: MusicRepository,
    private val exoPlayer: ExoPlayer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainState())
    val uiState: StateFlow<MainState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MainEffect>()
    val effect: SharedFlow<MainEffect> = _effect.asSharedFlow()

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
        val currentMusicIndex = exoPlayer.currentMediaItemIndex
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
        val currentPositionMs =
            exoPlayer.currentPosition.coerceAtLeast(0L)

        val playerDurationMs = exoPlayer.duration

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
        exoPlayer.addListener(playerListener)
        initPlaylist()
        playMusicAt()
    }

    private fun initPlaylist() {
        if (musicList.isEmpty()) {
            return
        }

        val mediaItems = musicList.map { music ->
            val uri = context.toAndroidResourceUri(resourceId = music.musicResId)
            MediaItem.fromUri(uri)
        }

        exoPlayer.apply {
            setMediaItems(mediaItems)
            prepare()
        }
    }

    private fun playMusicAt(index : Int = 0) {
        if (index !in musicList.indices) return

        exoPlayer.seekTo(
            index,
            C.TIME_UNSET
        )

        exoPlayer.play()
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
        val durationMs = exoPlayer.duration

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

        exoPlayer.seekTo(targetPositionMs)
        exoPlayer.play()

        _uiState.update { currentState ->
            currentState.copy(
                currentPositionMs = targetPositionMs,
                progress = normalizedProgress,
            )
        }
    }

    private fun actionShuffle() {
        val newShuffleMode = !exoPlayer.shuffleModeEnabled

        exoPlayer.shuffleModeEnabled = newShuffleMode

        _uiState.update {
            it.copy(
                isShuffleEnabled = newShuffleMode,
            )
        }
    }

    private fun actionRepeat() {
        val newPlayerRepeatMode = when (exoPlayer.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_OFF
        }

        exoPlayer.repeatMode = newPlayerRepeatMode

        _uiState.update {
            it.copy(
                repeatMode = newPlayerRepeatMode.toRepeatMode(),
            )
        }
    }

    private fun actionPrevious() {
        if (!exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekTo(0L)
            viewModelScope.launch {
                _effect.emit(
                    MainEffect.MusicMessage(message = "Not Privious")
                )
            }
            return
        }
        exoPlayer.seekToPreviousMediaItem()
        exoPlayer.play()
    }

    private fun actionNext() {
        if (!exoPlayer.hasNextMediaItem()) {
            viewModelScope.launch {
                _effect.emit(
                    MainEffect.MusicMessage(message = "Not Next")
                )
            }
            return
        }
        exoPlayer.seekToNextMediaItem()
        exoPlayer.play()
    }

    private fun togglePlayback() {
        val isPlaying = _uiState.value.isPlaying
        if (isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
        _uiState.update {
            it.copy(isPlaying = !isPlaying)
        }
    }

    override fun onCleared() {
        release()
        super.onCleared()
    }

    private fun release() {
        stopProgressUpdates()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }
}
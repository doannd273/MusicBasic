package com.example.musicbasic

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer,
) : ViewModel() {

    private val musicList = listOf(
        R.raw.mot_ngay_nao_do,
        R.raw.tam_su,
        R.raw.vuon_may_vua_hay,
    )

    private val _uiState = MutableStateFlow(MainState())
    val uiState: StateFlow<MainState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MainEffect>()
    val effect: SharedFlow<MainEffect> = _effect.asSharedFlow()

    private var progressJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> updatePlaybackPosition()
                Player.STATE_ENDED -> {
                    updatePlaybackPosition()
                    stopProgressUpdates()
                    _uiState.update { it.copy(isPause = true) }
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "ExoPlayer error", error)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPause = !isPlaying) }
            if (isPlaying) {
                startProgressUpdates()
            } else {
                stopProgressUpdates()
                updatePlaybackPosition()
            }
        }
    }

    private fun startProgressUpdates() {
        if (progressJob?.isActive == true) return

        progressJob = viewModelScope.launch {
            while (isActive) {
                updatePlaybackPosition()
                delay(500)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updatePlaybackPosition() {
        val currentMs = exoPlayer.currentPosition.coerceAtLeast(0L)
        val durationMs = exoPlayer.duration
        val hasDuration = durationMs != C.TIME_UNSET && durationMs > 0L

        _uiState.update { currentState ->
            currentState.copy(
                startTime = currentMs / 1000,
                endTime = if (hasDuration) durationMs / 1000 else currentState.endTime,
                progress = if (hasDuration) {
                    (currentMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                } else {
                    currentState.progress
                },
            )
        }
    }

    init {
        exoPlayer.addListener(playerListener)
        playRandomMusic()
    }

    private fun playRandomMusic() {
        initPlay(musicList.random())
    }

    private fun initPlay(musicId: Int) {
        val uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(context.packageName)
            .appendPath(musicId.toString())
            .build()
        val mediaItem = MediaItem.fromUri(uri)

        _uiState.update {
            it.copy(
                isPause = false,
                progress = 0f,
                startTime = 0L,
                endTime = 0L,
            )
        }

        exoPlayer.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }

        viewModelScope.launch {
            _effect.emit(MainEffect.PlayMusicSuccess)
        }
    }

    fun onEvent(event: MainEvent) {
        when (event) {
            MainEvent.ShuffleClicked -> Unit
            MainEvent.PreviousClicked -> {

            }
            MainEvent.PlayPauseClicked -> {
                val shouldResume = _uiState.value.isPause
                if (shouldResume) {
                    exoPlayer.play()
                } else {
                    exoPlayer.pause()
                }
                _uiState.value = _uiState.value.copy(isPause = !shouldResume)
            }
            MainEvent.NextClicked -> Unit
            MainEvent.RepeatClicked -> Unit
        }
    }

    override fun onCleared() {
        stopProgressUpdates()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
        super.onCleared()
    }

    private companion object {
        const val TAG = "MainViewModel"
    }
}

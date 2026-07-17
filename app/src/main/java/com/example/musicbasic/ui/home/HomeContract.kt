package com.example.musicbasic.ui.home

import com.example.musicbasic.model.Music
import com.example.musicbasic.model.RepeatMode

data class HomeState(
    val currentMusic: Music? = null, // id bài hát
    val currentMusicIndex: Int = 0, // vị trí bài hát
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isShuffleEnabled: Boolean = false,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
)

sealed class HomeEvent {
    data object ShuffleClicked : HomeEvent()

    data object PreviousClicked : HomeEvent()

    data object PlayPauseClicked : HomeEvent()

    data object NextClicked : HomeEvent()

    data object RepeatClicked : HomeEvent()

    data class SeekChanged(
        val progress: Float,
    ) : HomeEvent()

    data class MusicSelected(
        val musicId: Int
    ): HomeEvent()

    data object PlayShuffleClicked : HomeEvent()
}

sealed class HomeEffect {
    data class MusicMessage(
        val message: String,
    ) : HomeEffect()
}

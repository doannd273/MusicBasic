package com.example.musicbasic

import com.example.musicbasic.model.Music

data class MainState(
    val currentMusic: Music? = null, // id bài hát
    val currentMusicIndex: Int = 0, // vị trí bài hát
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
)

sealed class MainEvent {
    data object ShuffleClicked : MainEvent()
    data object PreviousClicked : MainEvent()
    data object PlayPauseClicked : MainEvent()
    data object NextClicked : MainEvent()
    data object RepeatClicked : MainEvent()
    data class SeekChanged(val progress: Float) : MainEvent()
}

sealed class MainEffect {
    data class MusicMessage(val message: String) : MainEffect()
}

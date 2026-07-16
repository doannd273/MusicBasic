package com.example.musicbasic

data class MainState(
    val isPause: Boolean = false,
    val progress: Float = 0f,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
)

fun Long.timeText(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

sealed class MainEvent {
    data object ShuffleClicked : MainEvent()
    data object PreviousClicked : MainEvent()
    data object PlayPauseClicked : MainEvent()
    data object NextClicked : MainEvent()
    data object RepeatClicked : MainEvent()
}

sealed class MainEffect {
    data object PlayMusicSuccess: MainEffect()
}

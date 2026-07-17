package com.example.musicbasic.ui.list

import com.example.musicbasic.model.Music

data class ListState(
    val musicList: List<Music> = listOf(),
)

sealed class ListEvent {
    data class MusicClick(
        val music: Music,
    ) : ListEvent()
}

sealed class ListEffect {
    data object Success : ListEffect()
}

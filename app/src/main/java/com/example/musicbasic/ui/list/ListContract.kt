package com.example.musicbasic.ui.list

import com.example.musicbasic.model.Music

data class ListState(
    val musicList: List<Music> = listOf(),
)

sealed class ListEvent {
    data class DownloadMusic(
        val id: Int,
    ) : ListEvent()

    data class ShareMusic(
        val id: Int,
    ) : ListEvent()

    data class ToggleLikeMusic(
        val id: Int,
    ) : ListEvent()
}

sealed class ListEffect {
    data object Success : ListEffect()
}

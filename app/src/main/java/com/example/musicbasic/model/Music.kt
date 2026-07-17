package com.example.musicbasic.model

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

data class Music(
    val id: Int,
    val title: String,
    val author: String,
    @param:DrawableRes val thumbnail: Int,
    @param:RawRes val musicResId: Int,
)
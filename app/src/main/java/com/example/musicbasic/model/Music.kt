package com.example.musicbasic.model

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

data class Music(
    val id: Int,
    val title: String,
    val author: String,
    @DrawableRes val thumbnail: Int,
    @RawRes val musicResId: Int,
)
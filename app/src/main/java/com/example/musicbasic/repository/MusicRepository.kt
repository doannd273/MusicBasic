package com.example.musicbasic.repository

import com.example.musicbasic.model.Music

interface MusicRepository {
    fun getMusics(): List<Music>
}
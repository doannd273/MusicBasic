package com.example.musicbasic.repository

import com.example.musicbasic.R
import com.example.musicbasic.model.Music
import jakarta.inject.Inject

class MusicRepositoryImpl @Inject constructor() : MusicRepository {
    override fun getMusics(): List<Music> {
        return listOf(
            Music(
                id = 1,
                title = "Một ngày nào đó",
                author = "Hoàng Dũng, Đen, Bạn Nhạc",
                musicResId = R.raw.mot_ngay_nao_do,
                thumbnail = R.drawable.ic_background
            ),
            Music(
                id = 2,
                title = "Tâm sự",
                author = "Solmee, Dyteller, hoangkiet",
                musicResId = R.raw.tam_su,
                thumbnail = R.drawable.ic_background_2
            ),
            Music(
                id = 3,
                title = "Mườn mây vừa hay",
                author = "Ân ngờ, MYLINA",
                musicResId = R.raw.vuon_may_vua_hay,
                thumbnail = R.drawable.ic_background_3
            ),
        )
    }
}
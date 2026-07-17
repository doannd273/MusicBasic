package com.example.musicbasic.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.media3.common.Player
import androidx.media3.ui.compose.material3.Player
import com.example.musicbasic.R
import com.example.musicbasic.model.RepeatMode
import com.example.musicbasic.ui.theme.PlayerAccentPurpleLight
import com.example.musicbasic.ui.theme.PlayerPrimaryText

fun Long.timeText(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

fun Context.toAndroidResourceUri(resourceId: Int): Uri {
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(packageName)
        .appendPath(resourceId.toString())
        .build()
}

fun RepeatMode.repeatIcon(): Int {
    return when (this) {
        RepeatMode.ALL, RepeatMode.OFF -> R.drawable.ic_repeat
        RepeatMode.ONE -> R.drawable.ic_repeat_one
    }
}

fun RepeatMode.repeatTint(): Color {
    return when (this) {
        RepeatMode.OFF -> PlayerPrimaryText
        RepeatMode.ALL, RepeatMode.ONE -> PlayerAccentPurpleLight
    }
}

fun Int.toRepeatMode(): RepeatMode {
    return when (this) {
        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
        Player.REPEAT_MODE_ONE -> RepeatMode.ONE
        else -> RepeatMode.OFF
    }
}

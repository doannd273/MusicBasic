package com.example.musicbasic.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri

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

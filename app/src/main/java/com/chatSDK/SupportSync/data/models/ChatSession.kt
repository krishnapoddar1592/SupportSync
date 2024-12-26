package com.chatSDK.SupportSync.data.models

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


data class ChatSession(
    val id: Long? = null,
    val user: AppUser? = null, // The customer
    val agent: AppUser? = null, // The support agent
    var startedAt: String? = null,
    var endedAt: String? = null
) {
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun setStartedAt(timestamp: Long) {
//        startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
//    }
}
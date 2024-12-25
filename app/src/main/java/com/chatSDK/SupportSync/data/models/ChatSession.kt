package com.chatSDK.SupportSync.data.models

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID


data class ChatSession(
    val id: Long? = null,
    val user: AppUser? = null, // The customer
    val agent: AppUser? = null, // The support agent
    var startedAt: LocalDateTime? = null,
    var endedAt: LocalDateTime? = null
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun setStartedAt(timestamp: Long) {
        startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    }
}
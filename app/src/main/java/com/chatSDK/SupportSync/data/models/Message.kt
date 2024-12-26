package com.chatSDK.SupportSync.data.models

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class Message(
    val id: Long? = null,
    val chatSession: ChatSession? = null,
    val sender: AppUser? = null, // The sender (customer or agent)
    var content: String,
    var timestamp: String? = null,
    var imageUrl: String? = null // Path to the stored image or Base64 string
) {
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun setTimestamp(epochMilli: Long) {
//        timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault())
//    }
}


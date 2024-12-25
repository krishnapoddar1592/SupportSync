package com.chatSDK.SupportSync.core

import com.chatSDK.SupportSync.ui.theme.SupportSyncTheme

// SupportSyncConfig remains unchanged
data class SupportSyncConfig(
    val serverUrl: String,
    val apiKey: String,
    val theme: SupportSyncTheme,
    val features: Features
)

data class Features(
    val imageUpload: Boolean = true,
    val voiceMessages: Boolean = false,
    val typing: Boolean = true
)
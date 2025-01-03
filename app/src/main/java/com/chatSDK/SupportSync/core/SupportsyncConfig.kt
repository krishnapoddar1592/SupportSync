package com.chatSDK.SupportSync.core

import com.chatSDK.SupportSync.data.models.AppUser
import com.chatSDK.SupportSync.ui.theme.SupportSyncTheme

// SupportSyncConfig remains unchanged
data class SupportSyncConfig(
    val serverUrl: String,
    val wsUrl:String,
    val apiKey: String,
    val theme: SupportSyncTheme,
    val features: Features,
    val user: AppUser
)

data class Features(
    val imageUpload: Boolean = true,
    val typing: Boolean = true
)
package com.chatSDK.SupportSync.data.api

import android.net.Uri
import com.chatSDK.SupportSync.data.models.AppUser
import com.chatSDK.SupportSync.data.models.ChatSession
import com.chatSDK.SupportSync.data.models.Message
import retrofit2.http.Body
import retrofit2.http.POST

// Network Services
interface RestApiService {
    @POST("chat/start")
    suspend fun startSession(@Body user: AppUser): ChatSession

    @POST("chat/message")
    suspend fun sendMessage(@Body message: Message): Message

    @POST("chat/upload")
    suspend fun uploadImage(@Body imageUri: Uri): String
}
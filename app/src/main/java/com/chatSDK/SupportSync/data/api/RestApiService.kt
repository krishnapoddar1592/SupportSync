package com.chatSDK.SupportSync.data.api

import android.net.Uri
import com.chatSDK.SupportSync.data.models.AppUser
import com.chatSDK.SupportSync.data.models.ChatSession
import com.chatSDK.SupportSync.data.models.Message
import com.chatSDK.SupportSync.data.models.SessionRequest
import com.chatSDK.SupportSync.data.models.UploadImageResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// Network Services
interface RestApiService {
    @POST("/chat.startSession")
    suspend fun startSession(@Body sessionRequest: SessionRequest): Response<ChatSession>

    @Multipart
    @POST("/chat/uploadImage")
    suspend fun uploadImage(
        @Part("userId") userId: Long,
        @Part file: MultipartBody.Part
    ): Response<UploadImageResponse>


    @GET("/chat/sessions/{sessionId}/messages")
    suspend fun getMessages(@Path("sessionId") sessionId: String): List<Message>
}

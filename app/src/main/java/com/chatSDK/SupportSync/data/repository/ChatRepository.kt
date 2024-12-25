package com.chatSDK.SupportSync.data.repository

import android.net.Uri
import com.chatSDK.SupportSync.data.api.RestApiService
import com.chatSDK.SupportSync.data.api.WebSocketService
import com.chatSDK.SupportSync.data.models.ChatSession
import com.chatSDK.SupportSync.data.models.Message
import com.chatSDK.SupportSync.data.models.AppUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MultipartBody
import java.util.UUID

class ChatRepository(
    private val webSocketService: WebSocketService,
    private val apiService: RestApiService
) {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())

    suspend fun startSession(user: AppUser): Result<ChatSession> {
        return try {
            val session = apiService.startSession(user)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(sessionId: String, message: String): Result<Message> {
        return try {
            val sentMessage = Message(content = message)
            webSocketService.sendMessage(sessionId, message)
            _messages.value += sentMessage
            Result.success(sentMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImage(sessionId: String, file: MultipartBody.Part): Result<String> {
        return try {
            val imageUrl = apiService.uploadImage(sessionId, file)
            Result.success(imageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeMessages(sessionId: String): Flow<List<Message>> {
        webSocketService.connect(
            sessionId = sessionId,
            onMessage = { message ->
                _messages.value = _messages.value + message
            },
            onError = { /* Handle error */ }
        )
        return _messages.asStateFlow()
    }
}

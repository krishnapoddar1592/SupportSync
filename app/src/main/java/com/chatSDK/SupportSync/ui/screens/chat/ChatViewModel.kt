package com.chatSDK.SupportSync.ui.screens.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatSDK.SupportSync.data.models.AppUser
import com.chatSDK.SupportSync.data.models.UserRole
import com.chatSDK.SupportSync.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.chatSDK.SupportSync.data.api.WebSocketService
import com.chatSDK.SupportSync.data.models.ChatSession
import com.chatSDK.SupportSync.data.models.Message
import com.chatSDK.SupportSync.utils.ImageUtils
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val webSocketService: WebSocketService
) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var currentSessionId: Long? = null
    private val _username = MutableStateFlow<String?>("")
    private val _userId = MutableStateFlow<Long?>(12345)

    fun startSession(userName: String) {
        viewModelScope.launch {
            _username.value = userName
            val result = chatRepository.startSession(AppUser(id = 123, username = userName, role = UserRole.CUSTOMER))
            result.onSuccess { session ->
                session.id?.let {
                    currentSessionId = it
                    connectWebSocket(session)
                }
            }.onFailure {
                _errorMessage.value = "Failed to start session: ${it.message}"
            }
        }
    }

    private fun connectWebSocket(session: ChatSession) {
        session.id?.let { sessionId ->
            session.user?.let {
                webSocketService.connect(
                    sessionId = sessionId,
                    onMessage = { message ->
                        viewModelScope.launch {
                            _messages.value += message
                        }
                    },
                    onError = { error ->
                        viewModelScope.launch {
                            _errorMessage.value = "WebSocket error: ${error.message}"
                            _isConnected.value = false
                            // Try to reconnect
                            delay(5000) // Wait 5 seconds before reconnecting
                            connectWebSocket(session)
                        }
                    }
                )
            }
            _isConnected.value = true
        }
    }

    fun sendMessage(content: String) {
        if (!_isConnected.value) {
            _errorMessage.value = "Not connected to chat server"
            return
        }

        currentSessionId?.let { sessionId ->
            viewModelScope.launch {
                try {
                    _username.value?.let { username ->
                        webSocketService.sendMessage(
                            sessionId = sessionId,
                            userId = 123,
                            username = username,
                            content = content
                        )
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to send message: ${e.localizedMessage}"
                }
            }
        }
    }

    fun uploadImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val compressResult = ImageUtils.compressImage(context, uri)

                compressResult.fold(
                    onSuccess = { compressedBytes ->
                        currentSessionId?.let { sessionId ->
                            val requestBody = compressedBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                            val multipartBody = MultipartBody.Part.createFormData(
                                name = "file",
                                filename = "image.jpg",
                                body = requestBody
                            )

                            val result = _userId.value?.let { chatRepository.uploadImage(it, multipartBody) }
                            result?.onSuccess { imageUrl ->
                                _username.value?.let { username ->
                                    webSocketService.sendMessage(
                                        sessionId = sessionId,
                                        userId = 123,
                                        username = username,
                                        content = "",
                                        imageUrl = imageUrl
                                    )
                                }
                            }?.onFailure { error ->
                                _errorMessage.value = when {
                                    error.message?.contains("413") == true ->
                                        "Image is too large. Please select a smaller image."
                                    else -> "Failed to upload image: ${error.localizedMessage}"
                                }
                            }
                        }
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Failed to process image"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error processing image: ${e.localizedMessage}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
    }

    fun reconnect() {
        viewModelScope.launch {
            currentSessionId?.let { sessionId ->
                val result = chatRepository.startSession(
                    AppUser(id = 123, username = _username.value ?: "", role = UserRole.CUSTOMER)
                )
                result.onSuccess { session ->
                    connectWebSocket(session)
                }.onFailure {
                    _errorMessage.value = "Failed to reconnect: ${it.message}"
                }
            }
        }
    }
}


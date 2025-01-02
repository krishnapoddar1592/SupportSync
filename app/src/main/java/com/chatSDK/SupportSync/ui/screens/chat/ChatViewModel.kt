package com.chatSDK.SupportSync.ui.screens.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatSDK.SupportSync.data.api.WebSocketService
import com.chatSDK.SupportSync.data.models.AppUser
import com.chatSDK.SupportSync.data.models.ChatSession
import com.chatSDK.SupportSync.data.models.IssueCategory
import com.chatSDK.SupportSync.data.models.Message
import com.chatSDK.SupportSync.data.models.UserRole
import com.chatSDK.SupportSync.data.repository.ChatRepository
import com.chatSDK.SupportSync.utils.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadedImageUrl = MutableStateFlow<String?>(null)
    val uploadedImageUrl: StateFlow<String?> = _uploadedImageUrl.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var currentSessionId: Long? = null
    private val _username = MutableStateFlow<String?>("")
    private val _userId = MutableStateFlow<Long?>(12345)
    fun setError(message: String) {
        _errorMessage.value = message
    }

    fun uploadImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _isUploading.value = true
                val compressResult = ImageUtils.compressImage(context, uri)

                compressResult.fold(
                    onSuccess = { compressedBytes ->
                        _userId.value?.let { userId ->
                            val requestBody = compressedBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                            val multipartBody = MultipartBody.Part.createFormData(
                                name = "file",
                                filename = "image.jpg",
                                body = requestBody
                            )

                            chatRepository.uploadImage(userId, multipartBody)
                                .onSuccess { imageUrl ->
                                    _uploadedImageUrl.value = imageUrl
                                }
                                .onFailure { error ->
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
            } finally {
                _isUploading.value = false
            }
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
                            userId = _userId.value ?: 123,
                            username = username,
                            content = content,
                            imageUrl = _uploadedImageUrl.value
                        )
                        // Clear the uploaded image URL after sending
                        _uploadedImageUrl.value = null
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to send message: ${e.localizedMessage}"
                }
            }
        }
    }
    data class InitialMessage(
        val title: String,
        val description: String
    )

    fun startSession(
        userName: String,
        title: String,
        category: IssueCategory?,
        desc: String
    ) {
        viewModelScope.launch {
            _username.value = userName
            val result = chatRepository.startSession(
                AppUser(id = 123, username = userName, role = UserRole.CUSTOMER),
                category
            )
            result.onSuccess { session ->
                session.id?.let {
                    currentSessionId = it
                    connectWebSocket(session, InitialMessage(title, desc))
                }
            }.onFailure {
                _errorMessage.value = "Failed to start session: ${it.message}"
            }
        }
    }

    private fun connectWebSocket(
        session: ChatSession,
        initialMessage: InitialMessage? = null
    ) {
        session.id?.let { sessionId ->
            session.user?.let { user ->
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
                            // Try to reconnect with the same initial message
                            delay(5000)
                            connectWebSocket(session, initialMessage)
                        }
                    },
                    onConnected = {
                        viewModelScope.launch {
                            _isConnected.value = true
                            // Send initial message once connected
                            initialMessage?.let { msg ->
                                val formattedContent = """
                                Title: ${msg.title}
                                Description: ${msg.description}
                            """.trimIndent()

                                // Using the existing WebSocketService's sendMessage signature
                                user.id?.let {
                                    webSocketService.sendMessage(
                                        sessionId = sessionId,
                                        userId = it,
                                        username = user.username,
                                        content = formattedContent
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }

//    fun sendMessage(content: String) {
//        if (!_isConnected.value) {
//            _errorMessage.value = "Not connected to chat server"
//            return
//        }
//
//        currentSessionId?.let { sessionId ->
//            viewModelScope.launch {
//                try {
//                    _username.value?.let { username ->
//                        webSocketService.sendMessage(
//                            sessionId = sessionId,
//                            userId = 123,
//                            username = username,
//                            content = content
//                        )
//                    }
//                } catch (e: Exception) {
//                    _errorMessage.value = "Failed to send message: ${e.localizedMessage}"
//                }
//            }
//        }
//    }
//
//    fun uploadImage(uri: Uri, context: Context)
//    {
//        viewModelScope.launch {
//            try {
//                val compressResult = ImageUtils.compressImage(context, uri)
//
//                compressResult.fold(
//                    onSuccess = { compressedBytes ->
//                        currentSessionId?.let { sessionId ->
//                            val requestBody = compressedBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
//                            val multipartBody = MultipartBody.Part.createFormData(
//                                name = "file",
//                                filename = "image.jpg",
//                                body = requestBody
//                            )
//
//                            val result = _userId.value?.let { chatRepository.uploadImage(it, multipartBody) }
//                            result?.onSuccess { imageUrl ->
//                                _username.value?.let { username ->
//                                    webSocketService.sendMessage(
//                                        sessionId = sessionId,
//                                        userId = 123,
//                                        username = username,
//                                        content = "",
//                                        imageUrl = imageUrl
//                                    )
//                                }
//                            }?.onFailure { error ->
//                                _errorMessage.value = when {
//                                    error.message?.contains("413") == true ->
//                                        "Image is too large. Please select a smaller image."
//                                    else -> "Failed to upload image: ${error.localizedMessage}"
//                                }
//                            }
//                        }
//                    },
//                    onFailure = { error ->
//                        _errorMessage.value = error.message ?: "Failed to process image"
//                    }
//                )
//            } catch (e: Exception) {
//                _errorMessage.value = "Error processing image: ${e.localizedMessage}"
//            }
//        }
//    }

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
                    AppUser(id = 123, username = _username.value ?: "", role = UserRole.CUSTOMER),
                    IssueCategory.BILLING
                )
                result.onSuccess { session ->
                    connectWebSocket(session)
                }.onFailure {
                    _errorMessage.value = "Failed to reconnect: ${it.message}"
                }
            }
        }
    }
    fun clearUploadedImage() {
        _uploadedImageUrl.value = null
    }
}


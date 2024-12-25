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
import com.chatSDK.SupportSync.data.models.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var currentSessionId: String? = null

    fun startSession(userName: String) {
        viewModelScope.launch {
            val result = chatRepository.startSession(AppUser(username = userName, role = UserRole.CUSTOMER))
            result.onSuccess { session ->
                currentSessionId = session.id?.toString()
                observeMessages()
            }.onFailure {
                _errorMessage.value = "Failed to start session: ${it.localizedMessage}"
            }
        }
    }

    private fun observeMessages() {
        currentSessionId?.let { sessionId ->
            viewModelScope.launch {
                chatRepository.observeMessages(sessionId).collect { messages ->
                    _messages.value = messages
                }
            }
        }
    }

    fun sendMessage(content: String) {
        currentSessionId?.let { sessionId ->
            viewModelScope.launch {
                val result = chatRepository.sendMessage(sessionId, content)
                result.onFailure {
                    _errorMessage.value = "Failed to send message: ${it.localizedMessage}"
                }
            }
        }
    }

    fun uploadImage(uri: Uri, context: Context) {
        currentSessionId?.let { sessionId ->
            viewModelScope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("File not found")
                    val requestBody = inputStream.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
                    val multipartBody = MultipartBody.Part.createFormData(
                        name = "file",
                        filename = "image.jpg",
                        body = requestBody
                    )
                    val result = chatRepository.uploadImage(sessionId, multipartBody)
                    result.onFailure {
                        _errorMessage.value = "Failed to upload image: ${it.localizedMessage}"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error processing image: ${e.localizedMessage}"
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}


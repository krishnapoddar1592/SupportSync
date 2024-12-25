package com.chatSDK.SupportSync.ui.screens.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatSDK.SupportSync.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val sendMessageUseCase: SendMessageUseCase,
    private val uploadImageUseCase: UploadImageUseCase
) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var currentSessionId: String? = null

    fun setSession(sessionId: String) {
        currentSessionId = sessionId
        viewModelScope.launch {
            chatRepository.observeMessages(sessionId)
                .collect { messages ->
                    _messages.value = messages
                }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            currentSessionId?.let { sessionId ->
                sendMessageUseCase(sessionId, content)
            }
        }
    }

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            currentSessionId?.let { sessionId ->
                uploadImageUseCase(sessionId, uri)
            }
        }
    }
}

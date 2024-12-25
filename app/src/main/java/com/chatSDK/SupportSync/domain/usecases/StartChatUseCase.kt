package com.chatSDK.SupportSync.domain.usecases

import com.chatSDK.SupportSync.data.models.ChatSession
import com.chatSDK.SupportSync.data.repository.ChatRepository

class StartChatUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(userName: String): Result<ChatSession> {
        return chatRepository.startSession(userName)
    }
}
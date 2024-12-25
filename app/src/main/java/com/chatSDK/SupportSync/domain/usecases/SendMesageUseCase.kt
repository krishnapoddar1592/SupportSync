package com.chatSDK.SupportSync.domain.usecases

import com.chatSDK.SupportSync.data.repository.ChatRepository

class SendMessageUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(sessionId: String, content: String): Result<Message> {
        return chatRepository.sendMessage(sessionId, content)
    }
}
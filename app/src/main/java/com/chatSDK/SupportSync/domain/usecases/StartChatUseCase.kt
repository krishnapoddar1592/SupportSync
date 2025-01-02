package com.chatSDK.SupportSync.domain.usecases

import com.chatSDK.SupportSync.data.models.AppUser
import com.chatSDK.SupportSync.data.models.ChatSession
import com.chatSDK.SupportSync.data.models.IssueCategory
import com.chatSDK.SupportSync.data.repository.ChatRepository

class StartChatUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(user: AppUser): Result<ChatSession> {
        return chatRepository.startSession(user, IssueCategory.BILLING)
    }
}
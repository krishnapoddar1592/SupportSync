package com.chatSDK.SupportSync.domain.usecases

import android.net.Uri
import com.chatSDK.SupportSync.data.repository.ChatRepository

class UploadImageUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(sessionId: String, uri: Uri): Result<String> {
        return chatRepository.uploadImage(sessionId, uri)
    }
}
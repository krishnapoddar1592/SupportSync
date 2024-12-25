package com.chatSDK.SupportSync.core.di

import com.chatSDK.SupportSync.data.api.RestApiService
import com.chatSDK.SupportSync.data.api.WebSocketService
import com.chatSDK.SupportSync.data.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideChatRepository(
        webSocketService: WebSocketService,
        restApiService: RestApiService
    ): ChatRepository {
        return ChatRepository(webSocketService, restApiService)
    }
}

package com.chatSDK.SupportSync.core.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.chatSDK.SupportSync.core.Features
import com.chatSDK.SupportSync.ui.theme.SupportSyncTheme
import com.chatSDK.SupportSync.data.api.RestApiService
import com.chatSDK.SupportSync.data.api.WebSocketService
import com.chatSDK.SupportSync.data.repository.ChatRepository
import com.chatSDK.SupportSync.core.SupportSyncConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import java.util.Base64
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideConfig(): SupportSyncConfig {
        return SupportSyncConfig(
            serverUrl = "http://192.168.0.102:8080" ,
            wsUrl="http://192.168.0.102:8080/ws/websocket",// For REST API // For REST API
            apiKey = "Basic " + Base64.getEncoder().encodeToString("username:password".toByteArray()),
            theme = SupportSyncTheme.Default,
            features = Features()
        )
    }

    @Provides
    @Singleton
    fun provideWebSocketService(config: SupportSyncConfig): WebSocketService {
        return WebSocketService(config.wsUrl, config)
    }



    @Provides
    @Singleton
    fun provideRestApiService(retrofit: Retrofit): RestApiService {
        return retrofit.create(RestApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        webSocketService: WebSocketService,
        restApiService: RestApiService
    ): ChatRepository {
        return ChatRepository(webSocketService, restApiService)
    }
}
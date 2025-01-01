package com.chatSDK.SupportSync.core

import android.content.Context
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.chatSDK.SupportSync.data.api.RestApiService
import com.chatSDK.SupportSync.data.api.WebSocketService
import com.chatSDK.SupportSync.data.models.AppUser
import com.chatSDK.SupportSync.data.models.ChatSession
import com.chatSDK.SupportSync.data.repository.ChatRepository
import com.chatSDK.SupportSync.domain.usecases.StartChatUseCase
import com.chatSDK.SupportSync.ui.theme.SupportSyncTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SupportSync private constructor(
    private val context: Context,
    private val config: SupportSyncConfig
) {
    private lateinit var webSocketService: WebSocketService
    private lateinit var retrofit: Retrofit
    private lateinit var apiService: RestApiService
    private lateinit var chatRepository: ChatRepository

    init {
        setupServices()
    }

    private fun setupServices() {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(config.serverUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(RestApiService::class.java)
        webSocketService = WebSocketService(config.wsUrl, config)
        chatRepository = ChatRepository(webSocketService, apiService)
    }

    companion object {
        @Volatile private var instance: SupportSync? = null
        fun builder(context: Context) = Builder(context)
    }

    class Builder(private val context: Context) {
        private var serverUrl: String = ""
        private var wsUrl: String = ""
        private var apiKey: String = ""
        private var theme: SupportSyncTheme = SupportSyncTheme.Default
        private var features: Features = Features()
        private var customChatBubble: (@Composable () -> Unit)? = null
        private var errorHandler: ((Throwable) -> Unit)? = null

        fun serverUrl(url: String) = apply {
            this.serverUrl = url
            // Automatically set WebSocket URL if not explicitly set
            if (this.wsUrl.isEmpty()) {
                this.wsUrl = url.replace("http", "ws") + "/ws/websocket"
            }
        }

        fun webSocketUrl(url: String) = apply {
            this.wsUrl = url
        }

        fun apiKey(key: String) = apply {
            this.apiKey = key
        }

        fun theme(theme: SupportSyncTheme) = apply {
            this.theme = theme
        }

        fun features(features: Features) = apply {
            this.features = features
        }

        fun errorHandler(handler: (Throwable) -> Unit) = apply {
            this.errorHandler = handler
        }

        fun customChatBubble(composable: @Composable () -> Unit) = apply {
            this.customChatBubble = composable
        }

        fun build(): SupportSync {
            validateConfiguration()

            synchronized(SupportSync::class.java) {
                if (instance == null) {
                    val config = SupportSyncConfig(
                        serverUrl = serverUrl,
                        wsUrl = wsUrl,
                        apiKey = apiKey,
                        theme = theme.copy(customBubbleComposable = customChatBubble),
                        features = features
                    )
                    instance = SupportSync(context.applicationContext, config)
                }
                return requireNotNull(instance)
            }
        }

        private fun validateConfiguration() {
            require(serverUrl.isNotEmpty()) { "Server URL must be set" }
            require(wsUrl.isNotEmpty()) { "WebSocket URL must be set" }
            require(apiKey.isNotEmpty()) { "API key must be set" }
        }
    }

    fun startChat(
        user: AppUser,
        onSuccess: (ChatSession) -> Unit,
        onError: (Exception) -> Unit
    ) {
        GlobalScope.launch {
            try {
                val result = chatRepository.startSession(user)
                result.onSuccess(onSuccess).onFailure { onError(it as Exception) }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun endChat() {
        webSocketService.disconnect()
    }
}

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
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class SupportSync private constructor(
    private val context: Context,
    private val config: SupportSyncConfig
) {
    private val webSocketService = WebSocketService(config.serverUrl, config)
    private val retrofit = Retrofit.Builder()
        .baseUrl(config.serverUrl)
        .build()
    private val apiService = retrofit.create(RestApiService::class.java)
    private val chatRepository = ChatRepository(webSocketService, apiService)
    private val startChatUseCase = StartChatUseCase(chatRepository)

    companion object {
        @Volatile private var instance: SupportSync? = null

        fun builder(context: Context) = Builder(context)
    }

    class Builder(private val context: Context) {
        private var serverUrl: String = ""
        private var apiKey: String = ""
        private var theme: SupportSyncTheme = SupportSyncTheme.Default
        private var features: Features = Features()
        private var customChatBubble: (@Composable () -> Unit)? = null

        fun serverUrl(url: String) = apply {
            this.serverUrl = url
        }

        fun apiKey(key: String) = apply {
            this.apiKey = key
        }

        fun primaryColor(color: Color) = apply {
            this.theme = this.theme.copy(primaryColor = color)
        }

        fun secondaryColor(color: Color) = apply {
            this.theme = this.theme.copy(secondaryColor = color)
        }

        fun backgroundColor(color: Color) = apply {
            this.theme = this.theme.copy(backgroundColor = color)
        }

        fun textColor(color: Color) = apply {
            this.theme = this.theme.copy(textColor = color)
        }

        fun fontFamily(font: FontFamily) = apply {
            this.theme = this.theme.copy(fontFamily = font)
        }

        fun shapes(shapes: Shapes) = apply {
            this.theme = this.theme.copy(shapes = shapes)
        }

        fun customChatBubble(composable: @Composable () -> Unit) = apply {
            this.customChatBubble = composable
        }

        fun enableImageUpload(enabled: Boolean) = apply {
            this.features = this.features.copy(imageUpload = enabled)
        }

        fun enableVoiceMessages(enabled: Boolean) = apply {
            this.features = this.features.copy(voiceMessages = enabled)
        }

        fun enableTypingIndicator(enabled: Boolean) = apply {
            this.features = this.features.copy(typing = enabled)
        }

        fun build(): SupportSync {
            if (serverUrl.isEmpty()) {
                throw IllegalStateException("Server URL must be set")
            }
            if (apiKey.isEmpty()) {
                throw IllegalStateException("API key must be set")
            }

            synchronized(SupportSync::class.java) {
                if (instance == null) {
                    val config = SupportSyncConfig(
                        serverUrl = serverUrl,
                        apiKey = apiKey,
                        theme = theme.copy(customBubbleComposable = customChatBubble),
                        features = features
                    )
                    instance = SupportSync(context.applicationContext, config)
                }
                return requireNotNull(instance)
            }
        }
    }

    fun startChat(
        user: AppUser,
        onSuccess: (ChatSession) -> Unit,
        onError: (Exception) -> Unit
    ) {
        kotlinx.coroutines.GlobalScope.launch {
            startChatUseCase(user)
                .onSuccess(onSuccess)
                .onFailure { onError(it as Exception) }
        }
    }

    fun endChat() {
        webSocketService.disconnect()
    }
}

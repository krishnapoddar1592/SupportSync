package com.chatSDK.SupportSync.core

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.chatSDK.SupportSync.data.api.RestApiService
import com.chatSDK.SupportSync.data.api.WebSocketService
import com.chatSDK.SupportSync.data.models.AppUser
import com.chatSDK.SupportSync.data.models.IssueCategory
import com.chatSDK.SupportSync.data.models.UserRole
import com.chatSDK.SupportSync.data.repository.ChatRepository
import com.chatSDK.SupportSync.ui.screens.chat.ChatScreen
import com.chatSDK.SupportSync.ui.screens.chat.PreChatScreen
import com.chatSDK.SupportSync.ui.theme.SupportSyncTheme
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
        private var user: AppUser? = null // New field for username

        fun serverUrl(url: String) = apply {
            this.serverUrl = url
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

        fun user(userId:Long,username: String) = apply { // Method to set username
            this.user = AppUser(userId,username,UserRole.CUSTOMER)
        }

        fun build(): SupportSync {
            validateConfiguration()

            synchronized(SupportSync::class.java) {
                if (instance == null) {
                    val config = user?.let {
                        SupportSyncConfig(
                            serverUrl = serverUrl,
                            wsUrl = wsUrl,
                            apiKey = apiKey,
                            theme = theme.copy(customBubbleComposable = customChatBubble),
                            features = features,
                            user = it // Pass username to config
                        )
                    }
                    instance = config?.let { SupportSync(context.applicationContext, it) }
                }
                return requireNotNull(instance)
            }
        }

        private fun validateConfiguration() {
            require(serverUrl.isNotEmpty()) { "Server URL must be set" }
            require(wsUrl.isNotEmpty()) { "WebSocket URL must be set" }
            require(apiKey.isNotEmpty()) { "API key must be set" }
            require(user!=null) { "Username must be set" } // Validate username
        }
    }

    fun showSupportChat(
        activity: ComponentActivity,
        config: SupportSyncConfig
    ) {
        activity.setContent {
            var showChat by remember { mutableStateOf(false) }
            var category by remember { mutableStateOf<IssueCategory?>(null) }
            var title by remember { mutableStateOf("") }
            var description by remember { mutableStateOf("") }

            if (!showChat) {
                PreChatScreen { selectedCategory, issueTitle, desc ->
                    category = selectedCategory
                    title = issueTitle
                    description = desc
                    showChat = true
                }
            } else {
                ChatScreen(
                    viewModel = hiltViewModel(),
                    user = config.user,
                    title = title,
                    category = category,
                    desc = description
                )
            }
        }
    }
    fun endChat() {
        webSocketService.disconnect()
    }
}

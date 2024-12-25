package com.chatSDK.SupportSync.data.api

import com.chatSDK.SupportSync.core.SupportSyncConfig
import com.chatSDK.SupportSync.data.models.AppUser
import com.chatSDK.SupportSync.data.models.Message
import com.chatSDK.SupportSync.data.models.UserRole
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener


class WebSocketService(
    private val serverUrl: String,
    private val config: SupportSyncConfig
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    fun connect(
        sessionId: String,
        onMessage: (Message) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val request = Request.Builder()
            .url("$serverUrl/chat/$sessionId")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                // Parse message and invoke callback
                // This is a simplified implementation
                val message = Message(content = text, sender = AppUser(1,"test",UserRole.CUSTOMER))
                onMessage(message)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                onError(t)
            }
        })
    }

    fun sendMessage(message: Message) {
        webSocket?.send(message.toString())
    }

    fun disconnect() {
        webSocket?.close(1000, "Session ended")
        webSocket = null
    }
}
package com.chatSDK.SupportSync.data.api

import com.chatSDK.SupportSync.core.SupportSyncConfig
import com.chatSDK.SupportSync.data.models.AppUser
import com.chatSDK.SupportSync.data.models.Message
import com.chatSDK.SupportSync.data.models.UserRole
import com.google.gson.Gson
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
            .url("$serverUrl/ws")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                // Parse message and invoke callback
                val message = Gson().fromJson(text, Message::class.java)
                onMessage(message)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                onError(t)
            }
        })
    }

    fun sendMessage(sessionId: String, content: String) {
        val payload = mapOf("sessionId" to sessionId, "content" to content)
        webSocket?.send(Gson().toJson(payload))
    }

    fun disconnect() {
        webSocket?.close(1000, "Session ended")
        webSocket = null
    }
}

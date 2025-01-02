package com.chatSDK.SupportSync.data.api

import android.util.Log
import com.chatSDK.SupportSync.core.SupportSyncConfig
import com.chatSDK.SupportSync.data.models.Message
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

class WebSocketService(
    private val serverUrl: String,
    private val config: SupportSyncConfig
) {
    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()
    private val gson = Gson()
    private val TAG = "WebSocketService"
    private var isConnected = false

    private fun removeQuotesAndUnescape(uncleanJson: String): String {
        val noQuotes = uncleanJson.replace(Regex("^\"|\"$"), "")
        return org.apache.commons.text.StringEscapeUtils.unescapeJava(noQuotes)
    }

    fun connect(
        sessionId: Long,
        onMessage: (Message) -> Unit,
        onError: (Throwable) -> Unit,
        onConnected: () -> Unit
    ) {
        val wsUrl = serverUrl.replace("http://", "ws://")
        Log.d(TAG, "Attempting to connect to WebSocket at: $wsUrl for session: $sessionId")

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl)
        stompClient?.withClientHeartbeat(10000)?.withServerHeartbeat(10000)

        // Listen for connection lifecycle events
        compositeDisposable.add(
            stompClient!!.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { lifecycleEvent ->
                    when (lifecycleEvent.type) {
                        LifecycleEvent.Type.OPENED -> {
                            Log.d(TAG, "STOMP connection opened for session: $sessionId")
                            isConnected = true
                            onConnected()
                        }
                        LifecycleEvent.Type.CLOSED -> {
                            Log.d(TAG, "STOMP connection closed for session: $sessionId")
                            isConnected = false
                        }
                        LifecycleEvent.Type.ERROR -> {
                            Log.e(TAG, "STOMP connection error for session: $sessionId", lifecycleEvent.exception)
                            isConnected = false
                            onError(lifecycleEvent.exception ?: Exception("Unknown error"))
                        }
                        LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                            Log.e(TAG, "Failed server heartbeat for session: $sessionId")
                            isConnected = false
                            onError(Exception("Failed server heartbeat"))
                        }
                    }
                }
        )

        // Subscribe to session-specific topic
        compositeDisposable.add(
            stompClient!!.topic("/topic/chat/$sessionId")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ topicMessage ->
                    Log.d(TAG, "Received message for session $sessionId: ${topicMessage.payload}")
                    val message = gson.fromJson(removeQuotesAndUnescape(topicMessage.payload), Message::class.java)
                    onMessage(message)
                }, { throwable ->
                    Log.e(TAG, "Error on subscribe topic for session $sessionId", throwable)
                    onError(throwable)
                })
        )

        stompClient?.connect()
    }

    fun sendMessage(sessionId: Long, userId: Long, username: String, content: String, imageUrl: String? = null) {
        val messageContent = mapOf(
            "content" to content,
            "imageUrl" to imageUrl,
            "sender" to mapOf(
                "id" to userId,
                "username" to username,
                "role" to "CUSTOMER"
            ),
            "chatSession" to mapOf(
                "id" to sessionId,
                "user" to mapOf(
                    "id" to userId,
                    "username" to username,
                    "role" to "CUSTOMER"
                ),
                "agent" to null,
                "startedAt" to System.currentTimeMillis(),
                "endedAt" to null
            )
        )

        stompClient?.send(
            "/app/chat.sendMessage",
            gson.toJson(messageContent)
        )?.subscribe({
            Log.d(TAG, "Message sent successfully to session: $sessionId")
        }, { throwable ->
            Log.e(TAG, "Error sending message to session: $sessionId", throwable)
        })
    }

    fun disconnect() {
        compositeDisposable.clear()
        stompClient?.disconnect()
    }
}
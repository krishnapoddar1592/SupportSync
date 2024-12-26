package com.chatSDK.SupportSync.data.api

// Message data class to match the structure in HTML implementation


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
    fun removeQuotesAndUnescape(uncleanJson: String): String {
        val noQuotes = uncleanJson.replace(Regex("^\"|\"$"), "")
        return org.apache.commons.text.StringEscapeUtils.unescapeJava(noQuotes)
    }


    fun connect(
        onMessage: (Message) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val wsUrl = serverUrl.replace("http://", "ws://")
        Log.d(TAG, "Attempting to connect to WebSocket at: $wsUrl")

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl)

        // Optional: Configure connection
        stompClient?.withClientHeartbeat(10000)?.withServerHeartbeat(10000)

        // Listen for connection lifecycle events
        compositeDisposable.add(
            stompClient!!.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { lifecycleEvent ->
                    when (lifecycleEvent.type) {
                        LifecycleEvent.Type.OPENED -> {
                            Log.d(TAG, "STOMP connection opened")
                        }
                        LifecycleEvent.Type.CLOSED -> {
                            Log.d(TAG, "STOMP connection closed")
                        }
                        LifecycleEvent.Type.ERROR -> {
                            Log.e(TAG, "STOMP connection error", lifecycleEvent.exception)
                            onError(lifecycleEvent.exception ?: Exception("Unknown error"))
                        }

                        LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> TODO()
                    }
                }
        )

        // Subscribe to messages
        compositeDisposable.add(
            stompClient!!.topic("/topic/messages")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ topicMessage ->
                    Log.e("Tag",topicMessage.payload)
                    val message = gson.fromJson(removeQuotesAndUnescape(topicMessage.payload), Message::class.java)
                    onMessage(message)
                }, { throwable ->
                    Log.e(TAG, "Error on subscribe topic", throwable)
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

        var subscribe = stompClient?.send(
            "/app/chat.sendMessage",
            gson.toJson(messageContent)
        )?.subscribe({
            Log.d(TAG, "Message sent successfully")
        }, { throwable ->
            Log.e(TAG, "Error sending message", throwable)
        })
    }

    fun disconnect() {
        compositeDisposable.clear()
        stompClient?.disconnect()
    }
}
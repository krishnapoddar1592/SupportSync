package com.chatSDK.SupportSync.data.repository

import android.util.Log
import com.chatSDK.SupportSync.data.api.RestApiService
import com.chatSDK.SupportSync.data.api.WebSocketService
import com.chatSDK.SupportSync.data.models.AppUser
import com.chatSDK.SupportSync.data.models.ChatSession
import com.chatSDK.SupportSync.data.models.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MultipartBody
import retrofit2.HttpException

class ChatRepository(
    private val webSocketService: WebSocketService,
    private val apiService: RestApiService
) {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())

    suspend fun startSession(user: AppUser): Result<ChatSession> {
        return try {
            val response=apiService.startSession(user)
            if(response.isSuccessful){
                Result.success(response.body()?:ChatSession())
            }
            else{
                Log.e("Error",response.message())
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Log.e("Error",e.message.toString())
            Result.failure(e)
        }
    }

    suspend fun sendMessage(sessionId: Long, userId:Long,userName:String,message: String,imageUrl:String): Result<Message> {
        return try {
            val sentMessage = Message(content = message)
            webSocketService.sendMessage(sessionId,userId,userName,message,imageUrl)
            _messages.value += sentMessage
            Result.success(sentMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImage(userId: Long, file: MultipartBody.Part): Result<String> {
        return try {
            val response = apiService.uploadImage(userId, file)
            Log.e("TAG", response.toString())
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    // Parse the JSON response to extract filePath
                    val filePath = responseBody.filePath
                    Result.success(filePath)
                } else {
                    Log.e("Error", "Response body is null")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Log.e("Error", response.message())
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Log.e("Error at upload", e.message.toString())
            Result.failure(e)
        }
    }


    fun observeMessages(sessionId: Long): Flow<List<Message>> {

        webSocketService.connect(
            sessionId,
            onMessage = { message ->
                _messages.value = _messages.value + message
            },
            onError = { /* Handle error */ }
        )
        return _messages.asStateFlow()
    }
}

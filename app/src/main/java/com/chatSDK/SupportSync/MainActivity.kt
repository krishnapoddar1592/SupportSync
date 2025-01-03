package com.chatSDK.SupportSync

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.chatSDK.SupportSync.core.di.LocalDateTimeAdapter
import com.chatSDK.SupportSync.data.models.AppUser
import com.chatSDK.SupportSync.data.models.ChatSession
import com.chatSDK.SupportSync.data.models.IssueCategory
import com.chatSDK.SupportSync.data.models.UserRole
import com.chatSDK.SupportSync.ui.screens.chat.ChatScreen
import com.chatSDK.SupportSync.ui.screens.chat.ChatViewModel
import com.chatSDK.SupportSync.ui.screens.chat.PreChatScreen
import com.chatSDK.SupportSync.ui.theme.SupportSyncTheme


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .create()
        val jsonResponse = """{"id":49,"user":{"id":123,"username":"User123","role":"CUSTOMER"},"agent":null,"startedAt":"2024-12-25T23:50:23.161","endedAt":null}"""
        val session = gson.fromJson(jsonResponse, ChatSession::class.java)
        println(session)

        val selectedImageUri = mutableStateOf<Uri?>(null)
        val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedImageUri.value = uri
        }


        setContent {
            val viewModel: ChatViewModel = hiltViewModel()
            var showChat by remember { mutableStateOf(false) }
            var category by remember { mutableStateOf<IssueCategory?>(null) }
            var title by remember { mutableStateOf("") }
            var description by remember { mutableStateOf("") }
            SupportSyncTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    if (!showChat) {
                        PreChatScreen { selectedCategory, name, desc ->
                            category = selectedCategory
                            title = name
                            description = desc
                            showChat = true
                        }
                    } else {
                        ChatScreen(
                            viewModel = hiltViewModel(),
                            user = AppUser(123,"User123",UserRole.CUSTOMER),
                            title=title,
                            category=category,
                            desc=description
                        )
                    }
                }
            }
        }

    }
}

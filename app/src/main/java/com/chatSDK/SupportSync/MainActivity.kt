package com.chatSDK.SupportSync

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.chatSDK.SupportSync.ui.screens.chat.ChatScreen
import com.chatSDK.SupportSync.ui.screens.chat.ChatViewModel
import com.chatSDK.SupportSync.ui.theme.SupportSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Image Picker
        val selectedImageUri = mutableStateOf<Uri?>(null)
        val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedImageUri.value = uri
        }

        setContent {
            SupportSyncTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ChatScreen(
                        viewModel = hiltViewModel(),
                        userName = "User123",
                        onPickImage = {
                            imagePickerLauncher.launch("image/*")
                            selectedImageUri.value // Return the selected image URI
                        }
                    )
                }
            }
        }
    }
}

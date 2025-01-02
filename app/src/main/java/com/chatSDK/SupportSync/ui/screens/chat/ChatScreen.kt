package com.chatSDK.SupportSync.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chatSDK.SupportSync.ui.components.ImagePreview
import com.chatSDK.SupportSync.ui.components.LoadingIndicator
import com.chatSDK.SupportSync.ui.components.MessageBubble

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    userName: String
) {
    val messages by viewModel.messages.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadedImageUrl by viewModel.uploadedImageUrl.collectAsState()
    val context = LocalContext.current

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadImage(it, context)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Error Message
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
            Button(onClick = { viewModel.clearError() }) {
                Text("Dismiss")
            }
        }

        // Chat Messages
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            messages.forEach { message ->
                MessageBubble(
                    message = message.content,
                    sender = message.sender?.username.orEmpty(),
                    isUserMessage = message.sender?.username == userName,
                    imageUrl = message.imageUrl,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // Loading Indicator
        if (isUploading) {
           LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        // Image Preview
        uploadedImageUrl?.let { imageUrl ->
            ImagePreview(
                imageUrl = imageUrl,
                onRemove = { viewModel.clearUploadedImage() },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.Start)  // Center the preview horizontally
            )
        }

        // Input Section
        var inputText by remember { mutableStateOf("") }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { launcher.launch("image/*") },
                enabled = !isUploading,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(if (isUploading) "Uploading..." else "Add Image")
            }

            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )

            Button(
                onClick = {
                    if (inputText.isNotBlank() || uploadedImageUrl != null) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }
}
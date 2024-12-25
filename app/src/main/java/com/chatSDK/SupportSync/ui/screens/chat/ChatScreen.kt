package com.chatSDK.SupportSync.ui.screens.chat

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    userName: String,
    onPickImage: () -> Uri? // Callback to pick an image
) {
    val messages by viewModel.messages.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Start session when screen is launched
    LaunchedEffect(userName) {
        viewModel.startSession(userName)
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

        // Input and Actions
        var inputText by remember { mutableStateOf("") }

        Row(modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            )
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Send")
            }
        }

        // Upload Image Button
        Button(
            onClick = {
                coroutineScope.launch {
                    val uri = onPickImage()
                    if (uri != null) {
                        viewModel.uploadImage(uri, context)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("Upload Image")
        }
    }
}

@Composable
fun MessageBubble(
    message: String,
    sender: String,
    isUserMessage: Boolean,
    imageUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val bubbleColor: Color = if (isUserMessage) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor: Color = if (isUserMessage) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val bubbleShape: Shape = if (isUserMessage) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 250.dp)
                .background(bubbleColor, bubbleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, bubbleShape)
                .padding(8.dp)
        ) {
            // Sender name (optional for non-user messages)
            if (!isUserMessage) {
                Text(
                    text = sender,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Display image if available
            imageUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Attached Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .padding(bottom = 4.dp)
                )
            }

            // Message text
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                textAlign = if (isUserMessage) TextAlign.End else TextAlign.Start
            )
        }
    }
}




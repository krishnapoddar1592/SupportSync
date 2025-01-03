package com.chatSDK.SupportSync.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chatSDK.SupportSync.data.models.AppUser
import com.chatSDK.SupportSync.data.models.IssueCategory
import com.chatSDK.SupportSync.ui.components.ImagePreview
import com.chatSDK.SupportSync.ui.components.LoadingIndicator
import com.chatSDK.SupportSync.ui.components.MessageBubble

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    user: AppUser,
    title: String,
    category: IssueCategory?,
    desc: String
) {
    val messages by viewModel.messages.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadedImageUrl by viewModel.uploadedImageUrl.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(user) {
        viewModel.startSession(user,title,category,desc)
    }
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
                    isUserMessage = message.sender?.username == user.username,
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
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Start)  // Center the preview horizontally
            )
        }

        // Input Section

        var inputText by remember { mutableStateOf("") }
        ChatInput(
            value = inputText,
            onValueChange = {
                inputText=it
            },
            onSendClick = {
                if (inputText.isNotBlank() || uploadedImageUrl != null) {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                }
            },
            onImageClick = { launcher.launch("image/*") }
        )
    }
}

@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,  // Different surface color
                    shape = RoundedCornerShape(14.dp)
                )
                .shadow(                                              // Add elevation
                    elevation = 3.dp,
                    shape = RoundedCornerShape(14.dp)
                ),
            placeholder = { Text("Type a message...") },
            trailingIcon = {
                IconButton(onClick = onImageClick) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "Attach Image"
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        IconButton(
            onClick = onSendClick,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                Icons.Default.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
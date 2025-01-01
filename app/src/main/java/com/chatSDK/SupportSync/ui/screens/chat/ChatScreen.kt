package com.chatSDK.SupportSync.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ImagePreview(
    imageUrl: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .wrapContentWidth() // This will allow the width to adjust based on content
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Image Preview",
            contentScale = ContentScale.Fit,  // Changed to Fit to maintain aspect ratio
            modifier = Modifier
                .height(50.dp)
                .wrapContentWidth()
                .clip(RoundedCornerShape(8.dp))
        )

        // Updated close button styling
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(10.dp) // Reduced size for the background circle
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = CircleShape
                )
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove Image",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(12.dp) // Reduced icon size
            )
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
            if(imageUrl!=null && imageUrl!=""){
                AsyncImage(
                    model = imageUrl,
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
//                textAlign =textAlignΩ if (isUserMessage) TextAlign.End else TextAlign.Start
            )
        }
    }
}




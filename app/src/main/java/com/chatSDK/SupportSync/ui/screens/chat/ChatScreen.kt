package com.chatSDK.SupportSync.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.chatSDK.SupportSync.data.models.Message
import com.chatSDK.SupportSync.data.models.MessageType
import com.chatSDK.SupportSync.ui.theme.SupportSyncTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// UI Components
@Composable
fun ChatScreen(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    viewModel: ChatViewModel,
    theme: SupportSyncTheme
) {
    val messages by viewModel.messages.collectAsState()

    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxSize()
    ) {
        MessageList(messages = messages, theme = theme)
        MessageInput(
            onMessageSent = viewModel::sendMessage,
            onImageSelected = viewModel::uploadImage
        )
    }
}
@Composable
fun MessageList(
    messages: List<Message>,
    theme: SupportSyncTheme,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        reverseLayout = true
    ) {
        items(messages.reversed()) { message ->
            MessageBubble(
                message = message,
                theme = theme
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    theme: SupportSyncTheme,
    modifier: Modifier = Modifier
) {
    val isUserMessage = message.sender == "user"
    val bubbleColor = if (isUserMessage) theme.primaryColor else theme.secondaryColor
    val alignment = if (isUserMessage) Alignment.End else Alignment.Start
    val textColor = if (isUserMessage) Color.White else theme.textColor

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        when (message.type) {
            MessageType.TEXT -> TextMessageBubble(
                message = message,
                bubbleColor = bubbleColor,
                textColor = textColor,
                theme = theme
            )
            MessageType.IMAGE -> ImageMessageBubble(
                message = message,
                bubbleColor = bubbleColor,
                theme = theme
            )
            MessageType.VOICE -> VoiceMessageBubble(
                message = message,
                bubbleColor = bubbleColor,
                theme = theme
            )
        }
    }
}

@Composable
private fun TextMessageBubble(
    message: Message,
    bubbleColor: Color,
    textColor: Color,
    theme: SupportSyncTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(max = 280.dp)
            .clip(theme.shapes.medium)
            .background(bubbleColor)
            .padding(12.dp)
    ) {
        Text(
            text = message.content,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = theme.fontFamily
        )
        Text(
            text = formatTimestamp(message.timestamp),
            color = textColor.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun ImageMessageBubble(
    message: Message,
    bubbleColor: Color,
    theme: SupportSyncTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(max = 280.dp)
            .clip(theme.shapes.medium)
            .background(bubbleColor)
            .padding(4.dp)
    ) {
        AsyncImage(
            model = message.content,
            contentDescription = "Image message",
            modifier = Modifier
                .fillMaxWidth()
                .clip(theme.shapes.medium),
            contentScale = ContentScale.FillWidth
        )
        Text(
            text = formatTimestamp(message.timestamp),
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.End)
                .padding(4.dp)
        )
    }
}

@Composable
private fun VoiceMessageBubble(
    message: Message,
    bubbleColor: Color,
    theme: SupportSyncTheme,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .widthIn(max = 280.dp)
            .clip(theme.shapes.medium)
            .background(bubbleColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying }
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Simple audio waveform representation
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .background(Color.White.copy(alpha = 0.2f), theme.shapes.small)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = formatTimestamp(message.timestamp),
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun MessageInput(
    onMessageSent: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    var message by remember { mutableStateOf("") }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Divider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image attachment button
            IconButton(
                onClick = { imagePicker.launch("image/*") }
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Attach image"
                )
            }

            // Message input field
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                placeholder = { Text("Type a message") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
                maxLines = 5
            )

            // Send button
            IconButton(
                onClick = {
                    if (message.isNotBlank()) {
                        onMessageSent(message)
                        message = ""
                    }
                },
                enabled = message.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send message",
                    tint = if (message.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}
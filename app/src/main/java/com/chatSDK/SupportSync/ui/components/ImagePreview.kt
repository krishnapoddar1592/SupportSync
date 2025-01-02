package com.chatSDK.SupportSync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ImagePreview(
    imageUrl: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
)
{
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
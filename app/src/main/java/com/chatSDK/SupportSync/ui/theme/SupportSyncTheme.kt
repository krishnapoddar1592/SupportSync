package com.chatSDK.SupportSync.ui.theme

import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily


data class SupportSyncTheme(
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val textColor: Color,
    val fontFamily: FontFamily,
    val shapes: Shapes,
    val customBubbleComposable: (@Composable () -> Unit)? = null
) {
    companion object {
        val Default = SupportSyncTheme(
            primaryColor = Color.Green,
            secondaryColor = Color.Gray,
            backgroundColor = Color.White,
            textColor = Color.Black,
            fontFamily = FontFamily.Default,
            shapes = Shapes()
        )
    }
}
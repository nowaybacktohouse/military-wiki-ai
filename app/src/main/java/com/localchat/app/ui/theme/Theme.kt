package com.localchat.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LocalChatColors.primary,
    onPrimary = LocalChatColors.onPrimary,
    primaryContainer = LocalChatColors.primaryContainer,
    background = LocalChatColors.background,
    surface = LocalChatColors.surface,
    surfaceVariant = LocalChatColors.surfaceVariant,
    onSurface = LocalChatColors.onSurface,
    onSurfaceVariant = LocalChatColors.onSurfaceVariant,
    error = LocalChatColors.error
)

@Composable
fun LocalChatTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = LocalChatColors.background.toArgb()
            window.navigationBarColor = LocalChatColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}

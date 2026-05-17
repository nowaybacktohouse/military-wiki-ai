package com.localchat.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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

private val AmoledColorScheme = darkColorScheme(
    primary = LocalChatColors.primary,
    onPrimary = LocalChatColors.onPrimary,
    primaryContainer = LocalChatColors.primaryContainer,
    background = LocalChatColors.amoledBackground,
    surface = LocalChatColors.amoledSurface,
    surfaceVariant = LocalChatColors.amoledCard,
    onSurface = LocalChatColors.onSurface,
    onSurfaceVariant = LocalChatColors.onSurfaceVariant,
    error = LocalChatColors.error
)

private val LightColorScheme = lightColorScheme(
    primary = LocalChatColors.primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E5FF),
    background = Color(0xFFF8F8F8),
    surface = Color.White,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF666666),
    error = LocalChatColors.error
)

@Composable
fun LocalChatTheme(
    themeMode: String = "dark",
    content: @Composable () -> Unit
) {
    val isDarkSystem = isSystemInDarkTheme()

    val colorScheme = when (themeMode) {
        "light" -> LightColorScheme
        "dark" -> DarkColorScheme
        "amoled" -> AmoledColorScheme
        "system" -> if (isDarkSystem) DarkColorScheme else LightColorScheme
        else -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                dynamicDarkColorScheme(context)
            } else {
                DarkColorScheme
            }
        }
    }

    val isDark = themeMode != "light" && (themeMode != "system" || isDarkSystem)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

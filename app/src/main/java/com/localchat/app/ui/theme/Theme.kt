package com.localchat.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class AppColors(
    val background: Color = Black,
    val surface: Color = DarkSurface,
    val surfaceVariant: Color = DarkSurfaceVariant,
    val card: Color = DarkCard,
    val primary: Color = Primary,
    val primaryContainer: Color = PrimaryContainer,
    val onPrimary: Color = OnPrimary,
    val onSurface: Color = OnSurface,
    val onSurfaceVariant: Color = OnSurfaceVariant,
    val userBubble: Color = UserBubble,
    val assistantBubble: Color = AssistantBubble,
    val success: Color = Success,
    val error: Color = Error,
    val warning: Color = Warning
)

val LocalAppColors = staticCompositionLocalOf { AppColors() }

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    background = Black,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = OnSurface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        color = OnSurface
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = OnSurface
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = OnSurface
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        color = OnSurface
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        color = OnSurfaceVariant
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        color = OnSurfaceVariant
    )
)

@Composable
fun LocalChatTheme(content: @Composable () -> Unit) {
    val appColors = AppColors()
    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

object LocalChatTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}

package com.localchat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.localchat.app.ui.chat.ChatScreen
import com.localchat.app.ui.dumps.DumpsScreen
import com.localchat.app.ui.settings.SettingsScreen
import com.localchat.app.ui.theme.LocalChatColors
import com.localchat.app.ui.theme.LocalChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalChatTheme {
                MainScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Chat : Screen("chat", "Чат", Icons.Default.Chat)
    object Knowledge : Screen("knowledge", "Знания", Icons.Default.MenuBook)
    object Settings : Screen("settings", "Настройки", Icons.Default.Settings)
}

@Composable
fun MainScreen() {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Chat) }
    val screens = listOf(Screen.Chat, Screen.Knowledge, Screen.Settings)

    Scaffold(
        containerColor = LocalChatColors.background,
        bottomBar = {
            NavigationBar(
                containerColor = LocalChatColors.surface,
                contentColor = LocalChatColors.onSurface
            ) {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LocalChatColors.primary,
                            selectedTextColor = LocalChatColors.primary,
                            unselectedIconColor = LocalChatColors.onSurfaceVariant,
                            unselectedTextColor = LocalChatColors.onSurfaceVariant,
                            indicatorColor = LocalChatColors.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedScreen) {
                Screen.Chat -> ChatScreen()
                Screen.Knowledge -> DumpsScreen()
                Screen.Settings -> SettingsScreen()
            }
        }
    }
}

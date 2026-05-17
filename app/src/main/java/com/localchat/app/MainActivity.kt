package com.localchat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.localchat.app.service.CrashHandler
import com.localchat.app.service.PreferencesManager
import com.localchat.app.ui.chat.ChatScreen
import com.localchat.app.ui.dumps.DumpsScreen
import com.localchat.app.ui.settings.SettingsScreen
import com.localchat.app.ui.theme.LocalChatTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashHandler = CrashHandler(this)
        crashHandler.install()
        enableEdgeToEdge()
        setContent {
            val prefs = remember { PreferencesManager(this@MainActivity) }
            var themeMode by remember {
                mutableStateOf(runBlocking { prefs.themeMode.first() })
            }
            LaunchedEffect(Unit) {
                prefs.themeMode.collect { mode ->
                    themeMode = mode
                }
            }
            LocalChatTheme(themeMode = themeMode) {
                MainContent()
            }
        }
    }
}

sealed class Screen(val route: String, val icon: ImageVector, val labelRes: Int) {
    data object Chat : Screen("chat", Icons.Default.Chat, R.string.tab_chat)
    data object Knowledge : Screen("knowledge", Icons.Default.MenuBook, R.string.tab_knowledge)
    data object Settings : Screen("settings", Icons.Default.Settings, R.string.tab_settings)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainContent() {
    val screens = listOf(Screen.Chat, Screen.Knowledge, Screen.Settings)
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Chat) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = stringResource(screen.labelRes)) },
                        label = { Text(stringResource(screen.labelRes)) },
                        selected = selectedScreen.route == screen.route,
                        onClick = { selectedScreen = screen },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = selectedScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    is Screen.Chat -> ChatScreen()
                    is Screen.Knowledge -> DumpsScreen()
                    is Screen.Settings -> SettingsScreen()
                }
            }
        }
    }
}

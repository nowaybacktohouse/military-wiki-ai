package com.localchat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.localchat.app.ui.chat.ChatScreen
import com.localchat.app.ui.dumps.DumpsScreen
import com.localchat.app.ui.settings.SettingsScreen
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

data class NavItem(val label: String, val icon: ImageVector)

@Composable
fun MainScreen() {
    val navItems = listOf(
        NavItem("Чат", Icons.Default.Chat),
        NavItem("Знания", Icons.Default.MenuBook),
        NavItem("Настройки", Icons.Default.Settings)
    )
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = LocalChatTheme.colors.surface,
                contentColor = LocalChatTheme.colors.onSurface
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LocalChatTheme.colors.primary,
                            selectedTextColor = LocalChatTheme.colors.primary,
                            unselectedIconColor = LocalChatTheme.colors.onSurfaceVariant,
                            unselectedTextColor = LocalChatTheme.colors.onSurfaceVariant,
                            indicatorColor = LocalChatTheme.colors.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> ChatScreen(modifier = Modifier.padding(innerPadding))
            1 -> DumpsScreen(modifier = Modifier.padding(innerPadding))
            2 -> SettingsScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}

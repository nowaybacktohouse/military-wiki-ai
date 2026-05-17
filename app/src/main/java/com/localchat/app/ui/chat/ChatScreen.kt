package com.localchat.app.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localchat.app.data.model.ChatMessage
import com.localchat.app.ui.theme.LocalChatColors
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(modifier: Modifier = Modifier, viewModel: ChatViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isModelLoaded by viewModel.isModelLoaded.collectAsState()
    val loadedModelName by viewModel.loadedModelName.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LocalChatColors.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = LocalChatColors.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LocalChatColors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = LocalChatColors.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("LocalChat", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LocalChatColors.onSurface)
                    Text(
                        when {
                            isModelLoaded && loadedModelName != null -> "Модель: $loadedModelName"
                            isModelLoaded -> "Модель загружена"
                            else -> "Модель не загружена"
                        },
                        fontSize = 12.sp,
                        color = if (isModelLoaded) LocalChatColors.success else LocalChatColors.warning,
                        maxLines = 1
                    )
                }
                if (messages.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Очистить", tint = LocalChatColors.onSurfaceVariant)
                    }
                }
            }
        }

        // Messages
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item { WelcomeCard(isModelLoaded) }
            }
            items(messages, key = { it.id }) { message ->
                AnimatedVisibility(visible = true, enter = fadeIn()) {
                    MessageBubble(message)
                }
            }
            if (isGenerating) {
                item { TypingIndicator() }
            }
        }

        // Input
        Surface(modifier = Modifier.fillMaxWidth(), color = LocalChatColors.surface, tonalElevation = 4.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(if (isModelLoaded) "Введите сообщение..." else "Сначала загрузите модель", color = LocalChatColors.onSurfaceVariant) },
                    enabled = isModelLoaded && !isGenerating,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LocalChatColors.onSurface,
                        unfocusedTextColor = LocalChatColors.onSurface,
                        focusedBorderColor = LocalChatColors.primary,
                        unfocusedBorderColor = LocalChatColors.surfaceVariant,
                        cursorColor = LocalChatColors.primary,
                        focusedContainerColor = LocalChatColors.surfaceVariant,
                        unfocusedContainerColor = LocalChatColors.surfaceVariant
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText.trim())
                            inputText = ""
                        }
                    },
                    enabled = isModelLoaded && !isGenerating && inputText.isNotBlank(),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = LocalChatColors.primary,
                        disabledContainerColor = LocalChatColors.surfaceVariant
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Отправить", tint = LocalChatColors.onPrimary)
                }
            }
        }
    }
}

@Composable
fun WelcomeCard(isModelLoaded: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(64.dp), tint = LocalChatColors.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Добро пожаловать в LocalChat!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LocalChatColors.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (!isModelLoaded)
                    "Полностью локальный AI-ассистент.\n\n1. Откройте вкладку Настройки\n2. Скачайте модель\n3. Нажмите Загрузить\n4. Возвращайтесь в чат!"
                else
                    "Модель загружена! Напишите сообщение для начала общения.",
                fontSize = 14.sp, color = LocalChatColors.onSurfaceVariant, lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == ChatMessage.Role.USER
    val isSystem = message.role == ChatMessage.Role.SYSTEM
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 320.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isUser -> LocalChatColors.userBubble
                    isSystem -> LocalChatColors.warning.copy(alpha = 0.2f)
                    else -> LocalChatColors.assistantBubble
                }
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Text(text = message.content, modifier = Modifier.padding(12.dp), color = LocalChatColors.onSurface, fontSize = 15.sp, lineHeight = 21.sp)
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LocalChatColors.assistantBubble),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
        ) {
            Text("...  ", modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = LocalChatColors.onSurfaceVariant, fontSize = 14.sp)
        }
    }
}

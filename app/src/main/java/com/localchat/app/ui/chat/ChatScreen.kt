package com.localchat.app.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localchat.app.R
import com.localchat.app.data.model.ChatMessage
import com.localchat.app.ui.theme.LocalChatColors
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isModelLoaded by viewModel.isModelLoaded.collectAsState()
    val modelName by viewModel.modelName.collectAsState()
    val error by viewModel.error.collectAsState()
    val ragEnabled by viewModel.ragEnabled.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.tab_chat),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isModelLoaded && modelName != null) modelName!! else stringResource(R.string.model_not_selected),
                        fontSize = 12.sp,
                        color = if (isModelLoaded) LocalChatColors.success else LocalChatColors.onSurfaceVariant
                    )
                }
                // RAG toggle
                FilterChip(
                    selected = ragEnabled,
                    onClick = { viewModel.toggleRag() },
                    label = { Text("RAG", fontSize = 11.sp) },
                    leadingIcon = if (ragEnabled) {
                        { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.height(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.clear_chat), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Error
        error?.let { err ->
            Snackbar(
                modifier = Modifier.padding(8.dp),
                action = {
                    TextButton(onClick = { viewModel.dismissError() }) { Text(stringResource(R.string.dismiss)) }
                }
            ) {
                Text(err)
            }
        }

        // Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (messages.isEmpty() && !isModelLoaded) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(48.dp), tint = LocalChatColors.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(R.string.welcome_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.welcome_subtitle), fontSize = 14.sp, color = LocalChatColors.onSurfaceVariant, lineHeight = 20.sp)
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    context = context,
                    isLast = message == messages.lastOrNull()
                )
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = LocalChatColors.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.generating), fontSize = 12.sp, color = LocalChatColors.onSurfaceVariant)
                    }
                }
            }
        }

        // Regenerate + Stop buttons
        AnimatedVisibility(visible = messages.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                if (isGenerating) {
                    OutlinedButton(
                        onClick = { viewModel.stopGeneration() },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.stop), fontSize = 12.sp)
                    }
                } else if (messages.lastOrNull()?.role == ChatMessage.Role.ASSISTANT) {
                    OutlinedButton(
                        onClick = { viewModel.regenerateLastResponse() },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.regenerate), fontSize = 12.sp)
                    }
                }
            }
        }

        // Input field
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.type_message), fontSize = 14.sp) },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LocalChatColors.primary,
                        unfocusedBorderColor = LocalChatColors.surfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText.trim())
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isGenerating && isModelLoaded,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = LocalChatColors.primary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = stringResource(R.string.send), tint = LocalChatColors.onPrimary)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(message: ChatMessage, context: Context, isLast: Boolean = false) {
    val isUser = message.role == ChatMessage.Role.USER
    val bgColor = if (isUser) LocalChatColors.userBubble else LocalChatColors.assistantBubble
    val alignment = if (isUser) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("message", message.content))
                        Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show()
                    }
                ),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            if (isUser) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp,
                    color = LocalChatColors.onPrimary,
                    lineHeight = 20.sp
                )
            } else {
                MarkdownText(
                    markdown = message.content,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val markwon = remember {
        Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .build()
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                setTextColor(android.graphics.Color.parseColor("#E8E8E8"))
                textSize = 14f
                setLineSpacing(4f, 1f)
                movementMethod = LinkMovementMethod.getInstance()
                setLinkTextColor(android.graphics.Color.parseColor("#64B5F6"))
            }
        },
        update = { textView ->
            markwon.setMarkdown(textView, markdown)
        }
    )
}

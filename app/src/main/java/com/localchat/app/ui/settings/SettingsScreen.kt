package com.localchat.app.ui.settings

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localchat.app.R
import com.localchat.app.data.model.ModelCatalog
import com.localchat.app.data.model.ModelCategory
import com.localchat.app.data.model.ModelInfo
import com.localchat.app.service.AppState
import com.localchat.app.service.CrashHandler
import com.localchat.app.ui.theme.LocalChatColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadedModels by viewModel.downloadedModels.collectAsState()
    val loadedModelId by viewModel.loadedModelId.collectAsState()
    val systemPrompt by viewModel.systemPrompt.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val maxTokens by viewModel.maxTokens.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = listOf(
        stringResource(R.string.section_models),
        stringResource(R.string.section_generation),
        stringResource(R.string.section_appearance),
        stringResource(R.string.section_about)
    )

    val deviceRam = remember {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        (memInfo.totalMem / (1024 * 1024)).toInt()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = LocalChatColors.primary,
            edgePadding = 0.dp
        ) {
            sections.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    text = { Text(title, fontSize = 13.sp, maxLines = 1) }
                )
            }
        }

        when (selectedSection) {
            0 -> ModelsSection(viewModel, deviceRam, downloadProgress, downloadedModels, loadedModelId)
            1 -> GenerationSection(viewModel, systemPrompt, temperature, maxTokens)
            2 -> AppearanceSection(viewModel, themeMode)
            3 -> AboutSection(context)
        }
    }
}

@Composable
fun ModelsSection(
    viewModel: SettingsViewModel,
    deviceRam: Int,
    downloadProgress: Map<String, Float>,
    downloadedModels: Set<String>,
    loadedModelId: String?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Memory, contentDescription = null, tint = LocalChatColors.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${stringResource(R.string.device_ram)}: ${deviceRam} MB",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        ModelCategory.entries.forEach { category ->
            val models = ModelCatalog.getModelsByCategory(category)
            if (models.isNotEmpty()) {
                item {
                    Text(
                        text = category.label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                items(models, key = { it.id }) { model ->
                    ModelCard(
                        model = model,
                        isDownloaded = model.id in downloadedModels,
                        isLoaded = model.id == loadedModelId,
                        isRecommended = model.requiredRamMb <= deviceRam,
                        progress = downloadProgress[model.id],
                        onDownload = { viewModel.downloadModel(model) },
                        onLoad = { viewModel.loadModel(model) },
                        onDelete = { viewModel.deleteModel(model) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun ModelCard(
    model: ModelInfo,
    isDownloaded: Boolean,
    isLoaded: Boolean,
    isRecommended: Boolean,
    progress: Float?,
    onDownload: () -> Unit,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLoaded) LocalChatColors.primaryContainer else LocalChatColors.card
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = model.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (model.category == ModelCategory.UNCENSORED) {
                    Surface(
                        color = LocalChatColors.uncensored.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("18+", fontSize = 10.sp, color = LocalChatColors.uncensored,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                if (isRecommended) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.recommended),
                        modifier = Modifier.size(16.dp), tint = LocalChatColors.success)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = model.description, fontSize = 12.sp, color = LocalChatColors.onSurfaceVariant, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                model.tags.forEach { tag ->
                    Surface(
                        color = LocalChatColors.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(tag, fontSize = 10.sp, color = LocalChatColors.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Surface(color = LocalChatColors.surfaceVariant, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = formatSize(model.fileSize),
                        fontSize = 10.sp,
                        color = LocalChatColors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Surface(color = LocalChatColors.surfaceVariant, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = "RAM: ${model.requiredRamMb} MB",
                        fontSize = 10.sp,
                        color = LocalChatColors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (progress != null && progress < 1f) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = LocalChatColors.downloadBar,
                    trackColor = LocalChatColors.downloadBarTrack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = LocalChatColors.primary)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isDownloaded) {
                        if (isLoaded) {
                            AssistChip(
                                onClick = {},
                                label = { Text(stringResource(R.string.active), fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp), tint = LocalChatColors.success) },
                                modifier = Modifier.height(32.dp)
                            )
                        } else {
                            Button(
                                onClick = onLoad,
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LocalChatColors.success),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, null, Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.use_model), fontSize = 12.sp)
                            }
                        }
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Delete, null, Modifier.size(14.dp), tint = LocalChatColors.error)
                        }
                    } else {
                        Button(
                            onClick = onDownload,
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LocalChatColors.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Download, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.download), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GenerationSection(
    viewModel: SettingsViewModel,
    systemPrompt: String,
    temperature: Float,
    maxTokens: Int
) {
    var editingPrompt by remember { mutableStateOf(systemPrompt) }
    var editingTemp by remember { mutableStateOf(temperature) }
    var editingTokens by remember { mutableStateOf(maxTokens.toFloat()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.system_prompt), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editingPrompt,
                        onValueChange = { editingPrompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.setSystemPrompt(editingPrompt) },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = LocalChatColors.primary)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${stringResource(R.string.temperature)}: ${"%.1f".format(editingTemp)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Slider(
                        value = editingTemp,
                        onValueChange = { editingTemp = it },
                        onValueChangeFinished = { viewModel.setTemperature(editingTemp) },
                        valueRange = 0.1f..2.0f,
                        steps = 18,
                        colors = SliderDefaults.colors(thumbColor = LocalChatColors.primary, activeTrackColor = LocalChatColors.primary)
                    )
                    Text(stringResource(R.string.temperature_hint), fontSize = 11.sp, color = LocalChatColors.onSurfaceVariant)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${stringResource(R.string.max_tokens)}: ${editingTokens.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Slider(
                        value = editingTokens,
                        onValueChange = { editingTokens = it },
                        onValueChangeFinished = { viewModel.setMaxTokens(editingTokens.toInt()) },
                        valueRange = 64f..2048f,
                        steps = 30,
                        colors = SliderDefaults.colors(thumbColor = LocalChatColors.primary, activeTrackColor = LocalChatColors.primary)
                    )
                    Text(stringResource(R.string.max_tokens_hint), fontSize = 11.sp, color = LocalChatColors.onSurfaceVariant)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun AppearanceSection(viewModel: SettingsViewModel, themeMode: String) {
    val themes = listOf(
        "system" to stringResource(R.string.theme_system),
        "light" to stringResource(R.string.theme_light),
        "dark" to stringResource(R.string.theme_dark),
        "amoled" to stringResource(R.string.theme_amoled)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.theme_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(themes) { (mode, label) ->
            @OptIn(ExperimentalMaterial3Api::class)
            Card(
                onClick = { viewModel.setThemeMode(mode) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (themeMode == mode) LocalChatColors.primaryContainer else LocalChatColors.card
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        colors = RadioButtonDefaults.colors(selectedColor = LocalChatColors.primary)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun AboutSection(context: Context) {
    val crashHandler = remember { CrashHandler(context) }
    val crashLogs = remember { crashHandler.getCrashLogs() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(64.dp), tint = LocalChatColors.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("LocalChat", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("v1.2", fontSize = 16.sp, color = LocalChatColors.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.about_description),
                        fontSize = 14.sp,
                        color = LocalChatColors.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = LocalChatColors.surfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.about_author),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.about_features),
                        fontSize = 13.sp,
                        color = LocalChatColors.onSurfaceVariant,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }

        // Crash logs section
        if (crashLogs.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.crash_logs),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(crashLogs.take(5)) { file ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.BugReport, null, Modifier.size(16.dp), tint = LocalChatColors.error)
                        Spacer(Modifier.width(8.dp))
                        Text(file.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "LocalChat Crash Log")
                                putExtra(Intent.EXTRA_TEXT, file.readText())
                            }
                            context.startActivity(Intent.createChooser(intent, "Send crash log"))
                        }) {
                            Icon(Icons.Default.Share, null, Modifier.size(16.dp), tint = LocalChatColors.primary)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000L -> "%.1f GB".format(bytes / 1_000_000_000.0)
        bytes >= 1_000_000L -> "%.0f MB".format(bytes / 1_000_000.0)
        else -> "$bytes B"
    }
}

package com.localchat.app.ui.settings

import android.app.Application
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localchat.app.data.model.ModelCatalog
import com.localchat.app.data.model.ModelInfo
import com.localchat.app.service.AppState
import com.localchat.app.service.DownloadProgress
import com.localchat.app.service.DownloadService
import com.localchat.app.ui.theme.LocalChatColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val downloadService = DownloadService(application)
    private val modelsDir = File(application.filesDir, "models")

    private val _downloads = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val downloads: StateFlow<Map<String, DownloadProgress>> = _downloads.asStateFlow()

    private val _downloadedModels = MutableStateFlow<Set<String>>(emptySet())
    val downloadedModels: StateFlow<Set<String>> = _downloadedModels.asStateFlow()

    val isModelLoaded: StateFlow<Boolean> = AppState.isModelLoaded
    val loadedModelId: StateFlow<String?> = AppState.loadedModelId

    init {
        modelsDir.mkdirs()
        refreshDownloaded()
    }

    private fun refreshDownloaded() {
        val downloaded = mutableSetOf<String>()
        for (model in ModelCatalog.models) {
            if (File(modelsDir, model.fileName).exists()) {
                downloaded.add(model.id)
            }
        }
        _downloadedModels.value = downloaded
    }

    fun downloadModel(model: ModelInfo) {
        val file = File(modelsDir, model.fileName)
        viewModelScope.launch {
            downloadService.download(model.downloadUrl, model.fileName, modelsDir).collect { progress ->
                _downloads.value = _downloads.value + (model.id to progress)
                if (progress.isComplete) {
                    refreshDownloaded()
                }
            }
        }
    }

    fun deleteModel(model: ModelInfo) {
        if (AppState.loadedModelId.value == model.id) {
            AppState.setModelUnloaded()
        }
        File(modelsDir, model.fileName).delete()
        refreshDownloaded()
    }

    fun loadModel(model: ModelInfo) {
        val path = File(modelsDir, model.fileName).absolutePath
        AppState.setModelLoaded(model.id, model.name, path)
    }

    fun unloadModel() {
        AppState.setModelUnloaded()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = viewModel()) {
    val downloads by viewModel.downloads.collectAsState()
    val downloadedModels by viewModel.downloadedModels.collectAsState()
    val isModelLoaded by viewModel.isModelLoaded.collectAsState()
    val loadedModelId by viewModel.loadedModelId.collectAsState()
    var showAbout by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(LocalChatColors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Настройки", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LocalChatColors.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isModelLoaded) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isModelLoaded) LocalChatColors.success else LocalChatColors.warning,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (isModelLoaded) "Модель активна" else "Модель не загружена",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = LocalChatColors.onSurface
                        )
                        if (loadedModelId != null) {
                            val model = ModelCatalog.models.find { it.id == loadedModelId }
                            Text(model?.name ?: loadedModelId.toString(), fontSize = 13.sp, color = LocalChatColors.onSurfaceVariant)
                        }
                    }
                    if (isModelLoaded) {
                        OutlinedButton(onClick = { viewModel.unloadModel() }) {
                            Text("Выгрузить", color = LocalChatColors.warning)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Нейросети", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LocalChatColors.onSurface)
            Text("Скачайте модель и нажмите Загрузить", fontSize = 13.sp, color = LocalChatColors.onSurfaceVariant)
        }

        item {
            Text("Стандартные модели", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LocalChatColors.primary)
        }

        val standardModels = ModelCatalog.models.filter { !it.isUncensored }
        items(standardModels) { model ->
            ModelCard(model = model, isDownloaded = downloadedModels.contains(model.id), isLoaded = loadedModelId == model.id, progress = downloads[model.id], onDownload = { viewModel.downloadModel(model) }, onDelete = { viewModel.deleteModel(model) }, onLoad = { viewModel.loadModel(model) })
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = LocalChatColors.uncensored, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Без цензуры", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LocalChatColors.uncensored)
            }
            Text("Модели без ограничений и фильтров", fontSize = 12.sp, color = LocalChatColors.onSurfaceVariant)
        }

        val uncensoredModels = ModelCatalog.models.filter { it.isUncensored }
        items(uncensoredModels) { model ->
            ModelCard(model = model, isDownloaded = downloadedModels.contains(model.id), isLoaded = loadedModelId == model.id, progress = downloads[model.id], onDownload = { viewModel.downloadModel(model) }, onDelete = { viewModel.deleteModel(model) }, onLoad = { viewModel.loadModel(model) })
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                shape = RoundedCornerShape(12.dp),
                onClick = { showAbout = !showAbout }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = LocalChatColors.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("О приложении", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = LocalChatColors.onSurface)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(if (showAbout) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = LocalChatColors.onSurfaceVariant)
                    }
                    if (showAbout) {
                        Spacer(modifier = Modifier.height(12.dp))
                        @Suppress("DEPRECATION")
                        Divider(color = LocalChatColors.surfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("LocalChat v1.0", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LocalChatColors.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Полностью локальный AI-чатбот для Android.\nРаботает без интернета (кроме загрузки моделей и дампов).\n\nДвижок: llama.cpp (GGUF)\nИнтерфейс: Jetpack Compose Material 3\nБаза знаний: SQLite FTS4\nМин. Android: 8.0 (API 26)\nАрхитектуры: arm64-v8a, armeabi-v7a, x86, x86_64", fontSize = 13.sp, color = LocalChatColors.onSurfaceVariant, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        @Suppress("DEPRECATION")
                        Divider(color = LocalChatColors.surfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("author nowayqq aka Денис Федоров", fontSize = 14.sp, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, color = LocalChatColors.primary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun ModelCard(model: ModelInfo, isDownloaded: Boolean, isLoaded: Boolean, progress: DownloadProgress?, onDownload: () -> Unit, onDelete: () -> Unit, onLoad: () -> Unit) {
    val isDownloading = progress != null && !progress.isComplete && progress.error == null
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isLoaded) LocalChatColors.primaryContainer else LocalChatColors.card),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(model.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = LocalChatColors.onSurface)
                if (model.isUncensored) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(color = LocalChatColors.uncensored.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text("18+", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LocalChatColors.uncensored, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
                if (isLoaded) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(color = LocalChatColors.success.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text("ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LocalChatColors.success, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
            }
            Text("${model.tier.emoji} ${model.tier.label} | ${model.sizeFormatted}", fontSize = 12.sp, color = LocalChatColors.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(model.description, fontSize = 12.sp, color = LocalChatColors.onSurfaceVariant, lineHeight = 16.sp)
            if (isDownloading && progress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = progress.percent / 100f, modifier = Modifier.fillMaxWidth().height(6.dp), color = LocalChatColors.downloadBar, trackColor = LocalChatColors.downloadBarTrack)
                Text("${progress.percent}%", fontSize = 11.sp, color = LocalChatColors.onSurfaceVariant)
            }
            if (progress?.error != null) {
                Text("Ошибка: ${progress.error}", fontSize = 11.sp, color = LocalChatColors.error)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isDownloaded && !isDownloading) {
                    Button(onClick = onDownload, colors = ButtonDefaults.buttonColors(containerColor = LocalChatColors.primary), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Скачать", fontSize = 13.sp)
                    }
                }
                if (isDownloaded && !isLoaded) {
                    Button(onClick = onLoad, colors = ButtonDefaults.buttonColors(containerColor = LocalChatColors.success), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Загрузить", fontSize = 13.sp)
                    }
                }
                if (isDownloaded) {
                    OutlinedButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = LocalChatColors.error)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Удалить", fontSize = 13.sp, color = LocalChatColors.error)
                    }
                }
            }
        }
    }
}

package com.localchat.app.ui.settings

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localchat.app.data.model.ModelCatalog
import com.localchat.app.data.model.ModelInfo
import com.localchat.app.service.DownloadProgress
import com.localchat.app.service.DownloadService
import com.localchat.app.ui.theme.LocalChatTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val downloadService = DownloadService(application)

    private val _downloads = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val downloads: StateFlow<Map<String, DownloadProgress>> = _downloads.asStateFlow()

    private val _loadedModelId = MutableStateFlow<String?>(null)
    val loadedModelId: StateFlow<String?> = _loadedModelId.asStateFlow()

    fun isModelDownloaded(model: ModelInfo): Boolean {
        return downloadService.isFileDownloaded(model.fileName, downloadService.getModelsDir())
    }

    fun downloadModel(model: ModelInfo) {
        viewModelScope.launch {
            downloadService.download(model.downloadUrl, model.fileName, downloadService.getModelsDir())
                .collect { progress ->
                    _downloads.value = _downloads.value + (model.id to progress)
                }
        }
    }

    fun deleteModel(model: ModelInfo) {
        downloadService.deleteFile(model.fileName, downloadService.getModelsDir())
        _downloads.value = _downloads.value - model.id
    }

    fun getModelPath(model: ModelInfo): String {
        return downloadService.getFilePath(model.fileName, downloadService.getModelsDir())
    }

    fun setLoadedModel(modelId: String?) {
        _loadedModelId.value = modelId
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = viewModel()) {
    val downloads by viewModel.downloads.collectAsState()
    val loadedModelId by viewModel.loadedModelId.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LocalChatTheme.colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.statusBarsPadding())
            Text(
                "Настройки",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = LocalChatTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Управление моделями и настройками",
                fontSize = 14.sp,
                color = LocalChatTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "МОДЕЛИ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LocalChatTheme.colors.primary,
                letterSpacing = 1.sp
            )
        }

        items(ModelCatalog.models) { model ->
            val isDownloaded = viewModel.isModelDownloaded(model)
            val progress = downloads[model.id]
            val isLoaded = loadedModelId == model.id

            ModelCard(
                model = model,
                isDownloaded = isDownloaded,
                isLoaded = isLoaded,
                progress = progress,
                onDownload = { viewModel.downloadModel(model) },
                onDelete = { viewModel.deleteModel(model) },
                onLoad = {
                    val chatVm = (context as? android.app.Activity)?.let { _ ->
                        viewModel.setLoadedModel(model.id)
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "О ПРИЛОЖЕНИИ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LocalChatTheme.colors.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = LocalChatTheme.colors.card),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("LocalChat v1.0", fontWeight = FontWeight.Bold, color = LocalChatTheme.colors.onSurface)
                    Text("Полностью локальный AI-ассистент", fontSize = 13.sp, color = LocalChatTheme.colors.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("На базе llama.cpp", fontSize = 13.sp, color = LocalChatTheme.colors.onSurfaceVariant)
                    Text("Модели: Qwen2.5 (GGUF Q4_K_M)", fontSize = 13.sp, color = LocalChatTheme.colors.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
fun ModelCard(
    model: ModelInfo,
    isDownloaded: Boolean,
    isLoaded: Boolean,
    progress: DownloadProgress?,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onLoad: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isLoaded) LocalChatTheme.colors.primaryContainer else LocalChatTheme.colors.card
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${model.tier.emoji} ${model.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = LocalChatTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                SuggestionChip(
                    onClick = {},
                    label = { Text(model.tier.label, fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = LocalChatTheme.colors.surfaceVariant,
                        labelColor = LocalChatTheme.colors.primary
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(model.description, fontSize = 13.sp, color = LocalChatTheme.colors.onSurfaceVariant, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Размер: ${model.sizeFormatted}", fontSize = 12.sp, color = LocalChatTheme.colors.onSurfaceVariant)

            if (progress != null && !progress.isComplete && progress.error == null) {
                Spacer(modifier = Modifier.height(8.dp))
                @Suppress("DEPRECATION")
                LinearProgressIndicator(
                    progress = progress.percent / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = LocalChatTheme.colors.primary,
                    trackColor = LocalChatTheme.colors.surfaceVariant
                )
                Text(
                    "${progress.percent}% (${formatBytes(progress.bytesDownloaded)} / ${formatBytes(progress.totalBytes)})",
                    fontSize = 11.sp,
                    color = LocalChatTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (progress?.error != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Ошибка: ${progress.error}", fontSize = 12.sp, color = LocalChatTheme.colors.error)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isDownloaded && (progress == null || progress.error != null)) {
                    Button(
                        onClick = onDownload,
                        colors = ButtonDefaults.buttonColors(containerColor = LocalChatTheme.colors.primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Скачать", fontSize = 13.sp)
                    }
                }
                if (isDownloaded) {
                    Button(
                        onClick = onLoad,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLoaded) LocalChatTheme.colors.success else LocalChatTheme.colors.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isLoaded) "Активна" else "Загрузить", fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = LocalChatTheme.colors.error)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Удалить", fontSize = 13.sp, color = LocalChatTheme.colors.error)
                    }
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) "%.1f ГБ".format(gb) else "%.0f МБ".format(mb)
}

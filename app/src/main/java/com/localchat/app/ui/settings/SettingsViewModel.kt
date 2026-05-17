package com.localchat.app.ui.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localchat.app.data.model.ModelCatalog
import com.localchat.app.data.model.ModelInfo
import com.localchat.app.service.AppState
import com.localchat.app.service.DownloadService
import com.localchat.app.service.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SettingsVM"
    }

    private val prefs = PreferencesManager(application)
    private val downloadService = DownloadService(application)

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    private val _downloadedModels = MutableStateFlow<Set<String>>(emptySet())
    val downloadedModels: StateFlow<Set<String>> = _downloadedModels.asStateFlow()

    private val _loadedModelId = MutableStateFlow<String?>(null)
    val loadedModelId: StateFlow<String?> = _loadedModelId.asStateFlow()

    private val _systemPrompt = MutableStateFlow("")
    val systemPrompt: StateFlow<String> = _systemPrompt.asStateFlow()

    private val _temperature = MutableStateFlow(0.7f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _maxTokens = MutableStateFlow(512)
    val maxTokens: StateFlow<Int> = _maxTokens.asStateFlow()

    private val _themeMode = MutableStateFlow("dark")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    init {
        viewModelScope.launch {
            _systemPrompt.value = prefs.systemPrompt.first()
            _temperature.value = prefs.temperature.first()
            _maxTokens.value = prefs.maxTokens.first()
            _themeMode.value = prefs.themeMode.first()
            val savedModelId = prefs.selectedModelId.first()
            if (!savedModelId.isNullOrBlank()) {
                _loadedModelId.value = savedModelId
            }
        }

        viewModelScope.launch {
            AppState.selectedModelId.collectLatest { id ->
                _loadedModelId.value = id
            }
        }

        checkDownloadedModels()
    }

    fun downloadModel(model: ModelInfo) {
        viewModelScope.launch {
            val modelsDir = downloadService.getModelsDir()
            downloadService.download(model.url, model.fileName, modelsDir).collect { progress ->
                if (progress.error != null) {
                    Log.e(TAG, "Download error: ${progress.error}")
                    return@collect
                }
                val pct = if (progress.totalBytes > 0) progress.bytesDownloaded.toFloat() / progress.totalBytes else 0f
                _downloadProgress.value = _downloadProgress.value + (model.id to pct)
                if (progress.isComplete) {
                    _downloadedModels.value = _downloadedModels.value + model.id
                    _downloadProgress.value = _downloadProgress.value - model.id
                }
            }
        }
    }

    fun loadModel(model: ModelInfo) {
        val modelsDir = downloadService.getModelsDir()
        val modelFile = File(modelsDir, model.fileName)
        if (!modelFile.exists()) return

        viewModelScope.launch {
            AppState.setModelSelected(model.id, model.name, modelFile.absolutePath)
            prefs.setSelectedModelId(model.id)
            _loadedModelId.value = model.id
        }
    }

    fun deleteModel(model: ModelInfo) {
        viewModelScope.launch {
            val modelsDir = downloadService.getModelsDir()
            downloadService.deleteFile(model.fileName, modelsDir)
            _downloadedModels.value = _downloadedModels.value - model.id
            if (_loadedModelId.value == model.id) {
                AppState.setModelUnloaded()
                prefs.setSelectedModelId("")
                _loadedModelId.value = null
            }
        }
    }

    fun setSystemPrompt(prompt: String) {
        viewModelScope.launch {
            prefs.setSystemPrompt(prompt)
            _systemPrompt.value = prompt
        }
    }

    fun setTemperature(temp: Float) {
        viewModelScope.launch {
            prefs.setTemperature(temp)
            _temperature.value = temp
        }
    }

    fun setMaxTokens(tokens: Int) {
        viewModelScope.launch {
            prefs.setMaxTokens(tokens)
            _maxTokens.value = tokens
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            prefs.setThemeMode(mode)
            _themeMode.value = mode
        }
    }

    private fun checkDownloadedModels() {
        val modelsDir = downloadService.getModelsDir()
        val downloaded = mutableSetOf<String>()
        ModelCatalog.models.forEach { model ->
            if (downloadService.isFileDownloaded(model.fileName, modelsDir)) {
                downloaded.add(model.id)
            }
        }
        _downloadedModels.value = downloaded
    }
}

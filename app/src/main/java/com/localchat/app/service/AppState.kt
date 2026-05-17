package com.localchat.app.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppState {
    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()

    private val _loadedModelId = MutableStateFlow<String?>(null)
    val loadedModelId: StateFlow<String?> = _loadedModelId.asStateFlow()

    private val _loadedModelName = MutableStateFlow<String?>(null)
    val loadedModelName: StateFlow<String?> = _loadedModelName.asStateFlow()

    private val _loadedModelPath = MutableStateFlow<String?>(null)
    val loadedModelPath: StateFlow<String?> = _loadedModelPath.asStateFlow()

    private val _selectedModelId = MutableStateFlow<String?>(null)
    val selectedModelId: StateFlow<String?> = _selectedModelId.asStateFlow()

    fun setModelLoaded(id: String, name: String, path: String) {
        _loadedModelId.value = id
        _loadedModelName.value = name
        _loadedModelPath.value = path
        _isModelLoaded.value = true
        _selectedModelId.value = id
    }

    fun setModelUnloaded() {
        _loadedModelId.value = null
        _loadedModelName.value = null
        _loadedModelPath.value = null
        _isModelLoaded.value = false
    }

    fun setSelectedModelId(id: String?) {
        _selectedModelId.value = id
    }

    fun setModelSelected(id: String, name: String, path: String) {
        _selectedModelId.value = id
        _loadedModelName.value = name
        _loadedModelPath.value = path
        _isModelLoaded.value = true
        _loadedModelId.value = id
    }
}

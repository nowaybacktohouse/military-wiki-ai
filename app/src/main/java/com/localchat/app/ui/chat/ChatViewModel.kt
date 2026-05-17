package com.localchat.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localchat.app.data.model.ChatHistory
import com.localchat.app.data.model.ChatMessage
import com.localchat.app.service.AppState
import com.localchat.app.service.KnowledgeDatabase
import com.localchat.app.service.LlamaEngine
import com.localchat.app.service.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val llamaEngine = LlamaEngine(application)
    private val knowledgeDb = KnowledgeDatabase(application)
    private val prefs = PreferencesManager(application)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()

    private val _modelName = MutableStateFlow<String?>(null)
    val modelName: StateFlow<String?> = _modelName.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _chatHistories = MutableStateFlow<List<ChatHistory>>(emptyList())
    val chatHistories: StateFlow<List<ChatHistory>> = _chatHistories.asStateFlow()

    private val _ragEnabled = MutableStateFlow(true)
    val ragEnabled: StateFlow<Boolean> = _ragEnabled.asStateFlow()

    private var generationJob: Job? = null
    private var currentModelPath: String? = null

    init {
        llamaEngine.init()

        viewModelScope.launch {
            AppState.isModelLoaded.collectLatest { loaded ->
                _isModelLoaded.value = loaded
            }
        }

        viewModelScope.launch {
            AppState.loadedModelName.collectLatest { name ->
                _modelName.value = name
            }
        }

        viewModelScope.launch {
            AppState.loadedModelPath.collectLatest { path ->
                if (path != null && path != currentModelPath) {
                    loadModel(path)
                }
            }
        }

        viewModelScope.launch {
            prefs.ragEnabled.collectLatest { enabled ->
                _ragEnabled.value = enabled
            }
        }
    }

    private suspend fun loadModel(path: String) {
        currentModelPath = path
        val systemPrompt = prefs.systemPrompt.first()
        val result = llamaEngine.loadModel(path, systemPrompt)
        if (result.isFailure) {
            _error.value = "Failed to load model: ${result.exceptionOrNull()?.message}"
            AppState.setModelUnloaded()
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isGenerating.value) return

        val userMessage = ChatMessage(role = ChatMessage.Role.USER, content = text)
        _messages.value = _messages.value + userMessage

        generationJob = viewModelScope.launch {
            _isGenerating.value = true
            _error.value = null

            try {
                val maxTokens = prefs.maxTokens.first()
                val ragOn = _ragEnabled.value

                var knowledgeContext = ""
                if (ragOn) {
                    knowledgeContext = buildKnowledgeContext(text)
                }

                val assistantMessage = ChatMessage(
                    role = ChatMessage.Role.ASSISTANT,
                    content = "",
                    isStreaming = true
                )
                _messages.value = _messages.value + assistantMessage

                val sb = StringBuilder()
                llamaEngine.generateResponse(text, knowledgeContext, maxTokens).collect { token ->
                    sb.append(token)
                    val updated = _messages.value.toMutableList()
                    updated[updated.lastIndex] = assistantMessage.copy(
                        content = sb.toString(),
                        isStreaming = true
                    )
                    _messages.value = updated
                }

                val updated = _messages.value.toMutableList()
                updated[updated.lastIndex] = assistantMessage.copy(
                    content = sb.toString(),
                    isStreaming = false
                )
                _messages.value = updated

            } catch (e: Exception) {
                _error.value = e.message ?: "Generation error"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun stopGeneration() {
        llamaEngine.requestStop()
        generationJob?.cancel()
        _isGenerating.value = false

        val updated = _messages.value.toMutableList()
        if (updated.isNotEmpty() && updated.last().isStreaming) {
            updated[updated.lastIndex] = updated.last().copy(isStreaming = false)
            _messages.value = updated
        }
    }

    fun regenerateLastResponse() {
        val msgs = _messages.value.toMutableList()
        if (msgs.size < 2) return

        if (msgs.last().role == ChatMessage.Role.ASSISTANT) {
            msgs.removeAt(msgs.lastIndex)
        }

        val lastUserMsg = msgs.lastOrNull { it.role == ChatMessage.Role.USER }
        if (lastUserMsg != null) {
            _messages.value = msgs
            sendMessage(lastUserMsg.content)
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
    }

    fun toggleRag() {
        viewModelScope.launch {
            val newVal = !_ragEnabled.value
            _ragEnabled.value = newVal
            prefs.setRagEnabled(newVal)
        }
    }

    private suspend fun buildKnowledgeContext(query: String): String {
        return try {
            val articles = knowledgeDb.search(query, limit = 3)
            if (articles.isEmpty()) return ""
            articles.joinToString("\n\n") { "## ${it.title}\n${it.content.take(500)}" }
        } catch (_: Exception) {
            ""
        }
    }

    fun dismissError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        llamaEngine.unload()
    }
}

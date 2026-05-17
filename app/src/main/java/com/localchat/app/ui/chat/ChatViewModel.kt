package com.localchat.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localchat.app.data.model.ChatMessage
import com.localchat.app.service.AppState
import com.localchat.app.service.KnowledgeDatabase
import com.localchat.app.service.LlamaEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val llamaEngine = LlamaEngine(application)
    private val knowledgeDb = KnowledgeDatabase(application)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    val isModelLoaded: StateFlow<Boolean> = AppState.isModelLoaded
    val loadedModelName: StateFlow<String?> = AppState.loadedModelName

    private var generateJob: Job? = null
    private var engineInitialized = false

    init {
        llamaEngine.init()
        engineInitialized = true
        viewModelScope.launch {
            try { knowledgeDb.importMilitaryDump(application) } catch (_: Exception) { }
        }
        viewModelScope.launch {
            AppState.loadedModelPath.collect { path ->
                if (path != null && engineInitialized) {
                    loadModelInternal(path)
                }
            }
        }
    }

    private fun loadModelInternal(modelPath: String) {
        viewModelScope.launch {
            val result = llamaEngine.loadModel(modelPath)
            if (result.isFailure) {
                AppState.setModelUnloaded()
                addMessage(ChatMessage(
                    role = ChatMessage.Role.SYSTEM,
                    content = "Ошибка загрузки модели: ${result.exceptionOrNull()?.message}"
                ))
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isGenerating.value) return

        val userMessage = ChatMessage(role = ChatMessage.Role.USER, content = text)
        addMessage(userMessage)

        _isGenerating.value = true
        generateJob = viewModelScope.launch {
            try {
                val knowledgeContext = buildKnowledgeContext(text)
                val assistantMessage = ChatMessage(
                    role = ChatMessage.Role.ASSISTANT,
                    content = ""
                )
                addMessage(assistantMessage)

                val responseBuilder = StringBuilder()
                llamaEngine.generateResponse(text, knowledgeContext).collect { token ->
                    responseBuilder.append(token)
                    updateLastMessage(responseBuilder.toString())
                }

                if (responseBuilder.isEmpty()) {
                    updateLastMessage("[Пустой ответ от модели]")
                }
            } catch (e: Exception) {
                addMessage(ChatMessage(
                    role = ChatMessage.Role.ASSISTANT,
                    content = "Ошибка: ${e.message}"
                ))
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private suspend fun buildKnowledgeContext(query: String): String {
        return try {
            val articles = knowledgeDb.search(query, limit = 3)
            if (articles.isEmpty()) return ""
            articles.joinToString("\n\n") { "## ${it.title}\n${it.content}" }
        } catch (_: Exception) { "" }
    }

    private fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }

    private fun updateLastMessage(content: String) {
        val current = _messages.value.toMutableList()
        if (current.isNotEmpty()) {
            current[current.lastIndex] = current.last().copy(content = content)
            _messages.value = current
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        generateJob?.cancel()
        llamaEngine.shutdown()
    }
}

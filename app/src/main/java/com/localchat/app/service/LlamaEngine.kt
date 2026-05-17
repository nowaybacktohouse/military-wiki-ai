package com.localchat.app.service

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class LlamaEngine(private val context: Context) {

    companion object {
        init {
            System.loadLibrary("localchat")
        }
    }

    private external fun nativeInit(nativeLibDir: String)
    private external fun nativeLoadModel(modelPath: String): Int
    private external fun nativePrepare(): Int
    private external fun nativeProcessSystemPrompt(systemPrompt: String): Int
    private external fun nativeProcessUserPrompt(userPrompt: String, nPredict: Int): Int
    private external fun nativeGenerateNextToken(): String?
    private external fun nativeUnload()
    private external fun nativeShutdown()
    private external fun nativeSystemInfo(): String
    private external fun nativeIsModelLoaded(): Boolean

    val isModelLoaded: Boolean get() = nativeIsModelLoaded()

    fun init() {
        val nativeLibDir = context.applicationInfo.nativeLibraryDir
        nativeInit(nativeLibDir)
    }

    suspend fun loadModel(modelPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val loadResult = nativeLoadModel(modelPath)
            if (loadResult != 0) return@withContext Result.failure(Exception("Failed to load model (code: $loadResult)"))

            val prepareResult = nativePrepare()
            if (prepareResult != 0) return@withContext Result.failure(Exception("Failed to prepare model (code: $prepareResult)"))

            val systemPrompt = "Ты — полезный AI-ассистент. Отвечай точно и по делу. Если в контексте есть информация из базы знаний, используй её для ответа."
            val sysResult = nativeProcessSystemPrompt(systemPrompt)
            if (sysResult != 0) return@withContext Result.failure(Exception("Failed to set system prompt (code: $sysResult)"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun generateResponse(userMessage: String, knowledgeContext: String = "", maxTokens: Int = 512): Flow<String> = flow {
        val prompt = if (knowledgeContext.isNotBlank()) {
            "Контекст из базы знаний:\n$knowledgeContext\n\nВопрос пользователя: $userMessage"
        } else {
            userMessage
        }

        val result = nativeProcessUserPrompt(prompt, maxTokens)
        if (result != 0) {
            emit("[Ошибка обработки запроса]")
            return@flow
        }

        while (true) {
            val token = nativeGenerateNextToken() ?: break
            if (token.isNotEmpty()) {
                emit(token)
            }
        }
    }.flowOn(Dispatchers.IO)

    fun unload() {
        try {
            nativeUnload()
        } catch (_: Exception) {}
    }

    fun shutdown() {
        try {
            nativeShutdown()
        } catch (_: Exception) {}
    }

    fun getSystemInfo(): String {
        return try {
            nativeSystemInfo()
        } catch (_: Exception) {
            "N/A"
        }
    }
}

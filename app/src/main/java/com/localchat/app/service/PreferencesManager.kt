package com.localchat.app.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "localchat_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val SELECTED_MODEL_ID = stringPreferencesKey("selected_model_id")
        val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
        val TEMPERATURE = floatPreferencesKey("temperature")
        val TOP_P = floatPreferencesKey("top_p")
        val TOP_K = intPreferencesKey("top_k")
        val REPEAT_PENALTY = floatPreferencesKey("repeat_penalty")
        val MAX_TOKENS = intPreferencesKey("max_tokens")
        val CONTEXT_SIZE = intPreferencesKey("context_size")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val RAG_ENABLED = stringPreferencesKey("rag_enabled")

        const val DEFAULT_SYSTEM_PROMPT = "You are a helpful assistant, answer in Russian."
        const val DEFAULT_TEMPERATURE = 0.7f
        const val DEFAULT_TOP_P = 0.9f
        const val DEFAULT_TOP_K = 40
        const val DEFAULT_REPEAT_PENALTY = 1.1f
        const val DEFAULT_MAX_TOKENS = 512
        const val DEFAULT_CONTEXT_SIZE = 4096
    }

    val selectedModelId: Flow<String?> = context.dataStore.data.map { it[SELECTED_MODEL_ID] }
    val systemPrompt: Flow<String> = context.dataStore.data.map { it[SYSTEM_PROMPT] ?: DEFAULT_SYSTEM_PROMPT }
    val temperature: Flow<Float> = context.dataStore.data.map { it[TEMPERATURE] ?: DEFAULT_TEMPERATURE }
    val topP: Flow<Float> = context.dataStore.data.map { it[TOP_P] ?: DEFAULT_TOP_P }
    val topK: Flow<Int> = context.dataStore.data.map { it[TOP_K] ?: DEFAULT_TOP_K }
    val repeatPenalty: Flow<Float> = context.dataStore.data.map { it[REPEAT_PENALTY] ?: DEFAULT_REPEAT_PENALTY }
    val maxTokens: Flow<Int> = context.dataStore.data.map { it[MAX_TOKENS] ?: DEFAULT_MAX_TOKENS }
    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "dark" }
    val ragEnabled: Flow<Boolean> = context.dataStore.data.map { (it[RAG_ENABLED] ?: "true") == "true" }

    suspend fun setSelectedModelId(id: String?) {
        context.dataStore.edit { prefs ->
            if (id != null) prefs[SELECTED_MODEL_ID] = id
            else prefs.remove(SELECTED_MODEL_ID)
        }
    }

    suspend fun setSystemPrompt(prompt: String) {
        context.dataStore.edit { it[SYSTEM_PROMPT] = prompt }
    }

    suspend fun setTemperature(temp: Float) {
        context.dataStore.edit { it[TEMPERATURE] = temp }
    }

    suspend fun setTopP(topP: Float) {
        context.dataStore.edit { it[TOP_P] = topP }
    }

    suspend fun setTopK(topK: Int) {
        context.dataStore.edit { it[TOP_K] = topK }
    }

    suspend fun setRepeatPenalty(penalty: Float) {
        context.dataStore.edit { it[REPEAT_PENALTY] = penalty }
    }

    suspend fun setMaxTokens(tokens: Int) {
        context.dataStore.edit { it[MAX_TOKENS] = tokens }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setRagEnabled(enabled: Boolean) {
        context.dataStore.edit { it[RAG_ENABLED] = if (enabled) "true" else "false" }
    }
}

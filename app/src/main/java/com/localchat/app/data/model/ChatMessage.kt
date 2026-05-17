package com.localchat.app.data.model

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
) {
    enum class Role { USER, ASSISTANT, SYSTEM }
}

data class ChatHistory(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val messages: List<ChatMessage>,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

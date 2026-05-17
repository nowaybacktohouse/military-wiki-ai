package com.localchat.app.data.model

data class ModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val sizeBytes: Long,
    val downloadUrl: String,
    val fileName: String,
    val tier: ModelTier
) {
    enum class ModelTier(val label: String, val emoji: String) {
        LIGHT("Лёгкая", "⚡"),
        MEDIUM("Средняя", "\uD83D\uDCA1"),
        HEAVY("Тяжёлая", "\uD83D\uDE80")
    }

    val sizeFormatted: String
        get() {
            val gb = sizeBytes / (1024.0 * 1024.0 * 1024.0)
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (gb >= 1.0) "%.1f ГБ".format(gb) else "%.0f МБ".format(mb)
        }
}

object ModelCatalog {
    val models = listOf(
        ModelInfo(
            id = "qwen2.5-0.5b",
            name = "Qwen2.5 0.5B",
            description = "Сверхлёгкая модель для слабых устройств. Быстрые ответы, базовое качество.",
            sizeBytes = 400_000_000L,
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
            fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
            tier = ModelInfo.ModelTier.LIGHT
        ),
        ModelInfo(
            id = "qwen2.5-1.5b",
            name = "Qwen2.5 1.5B",
            description = "Лёгкая модель. Хороший баланс скорости и качества для средних устройств.",
            sizeBytes = 1_100_000_000L,
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
            fileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
            tier = ModelInfo.ModelTier.LIGHT
        ),
        ModelInfo(
            id = "qwen2.5-3b",
            name = "Qwen2.5 3B",
            description = "Средняя модель. Качественные ответы для устройств с 6+ ГБ RAM.",
            sizeBytes = 2_000_000_000L,
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q4_k_m.gguf",
            fileName = "qwen2.5-3b-instruct-q4_k_m.gguf",
            tier = ModelInfo.ModelTier.MEDIUM
        ),
        ModelInfo(
            id = "qwen2.5-7b",
            name = "Qwen2.5 7B",
            description = "Тяжёлая модель. Отличное качество для флагманов (8+ ГБ RAM).",
            sizeBytes = 4_700_000_000L,
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-7B-Instruct-GGUF/resolve/main/qwen2.5-7b-instruct-q4_k_m.gguf",
            fileName = "qwen2.5-7b-instruct-q4_k_m.gguf",
            tier = ModelInfo.ModelTier.HEAVY
        )
    )
}

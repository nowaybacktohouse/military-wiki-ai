package com.localchat.app.data.model

data class ModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val sizeBytes: Long,
    val downloadUrl: String,
    val fileName: String,
    val tier: ModelTier,
    val isUncensored: Boolean = false
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
            description = "Сверхлёгкая модель для слабых устройств. Быстрые ответы, базовое качество. Подходит для простых вопросов и быстрого чата.",
            sizeBytes = 400_000_000L,
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
            fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
            tier = ModelInfo.ModelTier.LIGHT
        ),
        ModelInfo(
            id = "qwen2.5-1.5b",
            name = "Qwen2.5 1.5B",
            description = "Лёгкая модель. Хороший баланс скорости и качества. Подходит для средних устройств с 4+ ГБ RAM.",
            sizeBytes = 1_100_000_000L,
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
            fileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
            tier = ModelInfo.ModelTier.LIGHT
        ),
        ModelInfo(
            id = "qwen2.5-3b",
            name = "Qwen2.5 3B",
            description = "Средняя модель. Качественные ответы на русском и английском. Для устройств с 6+ ГБ RAM.",
            sizeBytes = 2_000_000_000L,
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q4_k_m.gguf",
            fileName = "qwen2.5-3b-instruct-q4_k_m.gguf",
            tier = ModelInfo.ModelTier.MEDIUM
        ),
        ModelInfo(
            id = "qwen2.5-7b",
            name = "Qwen2.5 7B",
            description = "Тяжёлая модель. Отличное качество, глубокое понимание контекста. Для флагманов с 8+ ГБ RAM.",
            sizeBytes = 4_700_000_000L,
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-7B-Instruct-GGUF/resolve/main/qwen2.5-7b-instruct-q4_k_m.gguf",
            fileName = "qwen2.5-7b-instruct-q4_k_m.gguf",
            tier = ModelInfo.ModelTier.HEAVY
        ),
        ModelInfo(
            id = "gemma2-2b",
            name = "Gemma 2 2B",
            description = "Модель от Google. Компактная и быстрая, хорошо работает с фактами. Для устройств с 4+ ГБ RAM.",
            sizeBytes = 1_600_000_000L,
            downloadUrl = "https://huggingface.co/bartowski/gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf",
            fileName = "gemma-2-2b-it-Q4_K_M.gguf",
            tier = ModelInfo.ModelTier.LIGHT
        ),
        ModelInfo(
            id = "llama3.2-1b",
            name = "LLaMA 3.2 1B",
            description = "Модель от Meta. Сверхбыстрая, оптимизирована для мобильных устройств. Базовое качество.",
            sizeBytes = 750_000_000L,
            downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf",
            fileName = "Llama-3.2-1B-Instruct-Q4_K_M.gguf",
            tier = ModelInfo.ModelTier.LIGHT
        ),
        ModelInfo(
            id = "llama3.2-3b",
            name = "LLaMA 3.2 3B",
            description = "Модель от Meta. Хороший баланс скорости и качества, сильна в английском. 6+ ГБ RAM.",
            sizeBytes = 2_000_000_000L,
            downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_M.gguf",
            fileName = "Llama-3.2-3B-Instruct-Q4_K_M.gguf",
            tier = ModelInfo.ModelTier.MEDIUM
        ),
        ModelInfo(
            id = "mistral-7b-uncensored",
            name = "Mistral 7B Uncensored",
            description = "Без цензуры! Мощная модель без ограничений. Отвечает на любые вопросы без фильтров. 8+ ГБ RAM.",
            sizeBytes = 4_400_000_000L,
            downloadUrl = "https://huggingface.co/TheBloke/Mistral-7B-Instruct-v0.2-GGUF/resolve/main/mistral-7b-instruct-v0.2.Q4_K_M.gguf",
            fileName = "mistral-7b-instruct-v0.2.Q4_K_M.gguf",
            tier = ModelInfo.ModelTier.HEAVY,
            isUncensored = true
        ),
        ModelInfo(
            id = "dolphin-2.6-mistral-7b",
            name = "Dolphin 2.6 Mistral 7B",
            description = "Без цензуры! На базе Mistral, обучена без системных ограничений. Творческая и свободная. 8+ ГБ RAM.",
            sizeBytes = 4_400_000_000L,
            downloadUrl = "https://huggingface.co/TheBloke/dolphin-2.6-mistral-7B-GGUF/resolve/main/dolphin-2.6-mistral-7b.Q4_K_M.gguf",
            fileName = "dolphin-2.6-mistral-7b.Q4_K_M.gguf",
            tier = ModelInfo.ModelTier.HEAVY,
            isUncensored = true
        ),
        ModelInfo(
            id = "dolphin-phi2",
            name = "Dolphin Phi-2 2.7B",
            description = "Без цензуры! Компактная свободная модель. Быстрая и без фильтров. Для средних устройств 4+ ГБ RAM.",
            sizeBytes = 1_600_000_000L,
            downloadUrl = "https://huggingface.co/TheBloke/dolphin-2_6-phi-2-GGUF/resolve/main/dolphin-2_6-phi-2.Q4_K_M.gguf",
            fileName = "dolphin-2_6-phi-2.Q4_K_M.gguf",
            tier = ModelInfo.ModelTier.MEDIUM,
            isUncensored = true
        )
    )
}

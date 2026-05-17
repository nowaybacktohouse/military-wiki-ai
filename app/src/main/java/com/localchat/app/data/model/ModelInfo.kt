package com.localchat.app.data.model

data class ModelInfo(
    val id: String,
    val name: String,
    val fileName: String,
    val url: String,
    val fileSize: Long,
    val requiredRamMb: Int,
    val description: String,
    val descriptionEn: String,
    val tags: List<String>,
    val category: ModelCategory = ModelCategory.LIGHT
)

enum class ModelCategory(val label: String, val labelEn: String) {
    LIGHT("Лёгкие (< 1 ГБ)", "Light (< 1 GB)"),
    MEDIUM("Средние (2-4 ГБ)", "Medium (2-4 GB)"),
    HEAVY("Тяжёлые (5-7 ГБ)", "Heavy (5-7 GB)"),
    UNCENSORED("Без цензуры", "Uncensored"),
    CODING("Для кода", "Coding")
}

object ModelCatalog {
    val models = listOf(
        // LIGHT < 1 GB
        ModelInfo(
            id = "qwen2.5-0.5b",
            name = "Qwen2.5-0.5B-Instruct",
            fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
            url = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
            fileSize = 400L * 1024 * 1024,
            requiredRamMb = 512,
            description = "Сверхлёгкая модель, быстрые ответы. Идеальна для слабых устройств.",
            descriptionEn = "Ultra-light model, fast responses. Ideal for weak devices.",
            tags = listOf("RU", "EN", "fast"),
            category = ModelCategory.LIGHT
        ),
        ModelInfo(
            id = "tinyllama-1.1b",
            name = "TinyLlama-1.1B-Chat",
            fileName = "tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
            url = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
            fileSize = 670L * 1024 * 1024,
            requiredRamMb = 768,
            description = "Компактная чат-модель 1.1B. Хорошее качество для своего размера.",
            descriptionEn = "Compact 1.1B chat model. Good quality for its size.",
            tags = listOf("EN", "fast"),
            category = ModelCategory.LIGHT
        ),
        ModelInfo(
            id = "phi3-mini",
            name = "Phi-3-mini-4k-instruct",
            fileName = "Phi-3-mini-4k-instruct-q4.gguf",
            url = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf",
            fileSize = 2100L * 1024 * 1024,
            requiredRamMb = 2500,
            description = "Microsoft Phi-3 Mini. Отличная модель для инструкций и рассуждений.",
            descriptionEn = "Microsoft Phi-3 Mini. Excellent for instructions and reasoning.",
            tags = listOf("EN", "quality"),
            category = ModelCategory.MEDIUM
        ),
        ModelInfo(
            id = "gemma2-2b",
            name = "Gemma-2-2B-it",
            fileName = "gemma-2-2b-it-Q4_K_M.gguf",
            url = "https://huggingface.co/bartowski/gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf",
            fileSize = 1500L * 1024 * 1024,
            requiredRamMb = 2000,
            description = "Google Gemma 2 2B. Компактная но умная модель от Google.",
            descriptionEn = "Google Gemma 2 2B. Compact but smart model from Google.",
            tags = listOf("EN", "quality"),
            category = ModelCategory.LIGHT
        ),
        // MEDIUM 2-4 GB
        ModelInfo(
            id = "qwen2.5-1.5b",
            name = "Qwen2.5-1.5B-Instruct",
            fileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
            url = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
            fileSize = 987L * 1024 * 1024,
            requiredRamMb = 1200,
            description = "Qwen 1.5B — баланс скорости и качества. Хорошо понимает русский.",
            descriptionEn = "Qwen 1.5B — balance of speed and quality. Good Russian understanding.",
            tags = listOf("RU", "EN", "fast"),
            category = ModelCategory.MEDIUM
        ),
        ModelInfo(
            id = "qwen2.5-3b",
            name = "Qwen2.5-3B-Instruct",
            fileName = "qwen2.5-3b-instruct-q4_k_m.gguf",
            url = "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q4_k_m.gguf",
            fileSize = 1900L * 1024 * 1024,
            requiredRamMb = 2500,
            description = "Qwen 3B — отлично понимает русский. Рекомендуемый выбор.",
            descriptionEn = "Qwen 3B — excellent Russian understanding. Recommended choice.",
            tags = listOf("RU", "EN", "quality"),
            category = ModelCategory.MEDIUM
        ),
        ModelInfo(
            id = "llama3.2-3b",
            name = "Llama-3.2-3B-Instruct",
            fileName = "Llama-3.2-3B-Instruct-Q4_K_M.gguf",
            url = "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_M.gguf",
            fileSize = 2020L * 1024 * 1024,
            requiredRamMb = 2500,
            description = "Meta LLaMA 3.2 3B. Быстрая и качественная.",
            descriptionEn = "Meta LLaMA 3.2 3B. Fast and high quality.",
            tags = listOf("EN", "quality"),
            category = ModelCategory.MEDIUM
        ),
        ModelInfo(
            id = "phi3.5-mini",
            name = "Phi-3.5-mini-instruct",
            fileName = "Phi-3.5-mini-instruct-Q4_K_M.gguf",
            url = "https://huggingface.co/bartowski/Phi-3.5-mini-instruct-GGUF/resolve/main/Phi-3.5-mini-instruct-Q4_K_M.gguf",
            fileSize = 2180L * 1024 * 1024,
            requiredRamMb = 2800,
            description = "Microsoft Phi-3.5 Mini. Улучшенная версия с расширенным контекстом.",
            descriptionEn = "Microsoft Phi-3.5 Mini. Improved version with extended context.",
            tags = listOf("EN", "quality"),
            category = ModelCategory.MEDIUM
        ),
        // HEAVY 5-7 GB
        ModelInfo(
            id = "qwen2.5-7b",
            name = "Qwen2.5-7B-Instruct",
            fileName = "qwen2.5-7b-instruct-q4_k_m.gguf",
            url = "https://huggingface.co/Qwen/Qwen2.5-7B-Instruct-GGUF/resolve/main/qwen2.5-7b-instruct-q4_k_m.gguf",
            fileSize = 4680L * 1024 * 1024,
            requiredRamMb = 5500,
            description = "Qwen 7B — лучшее качество для русского языка. Для мощных устройств.",
            descriptionEn = "Qwen 7B — best quality for Russian. For powerful devices.",
            tags = listOf("RU", "EN", "quality"),
            category = ModelCategory.HEAVY
        ),
        ModelInfo(
            id = "llama3.1-8b",
            name = "Llama-3.1-8B-Instruct",
            fileName = "Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf",
            url = "https://huggingface.co/bartowski/Meta-Llama-3.1-8B-Instruct-GGUF/resolve/main/Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf",
            fileSize = 4920L * 1024 * 1024,
            requiredRamMb = 6000,
            description = "Meta LLaMA 3.1 8B. Флагманская модель. Pixel 8/9 и выше.",
            descriptionEn = "Meta LLaMA 3.1 8B. Flagship model. Pixel 8/9 and above.",
            tags = listOf("EN", "quality"),
            category = ModelCategory.HEAVY
        ),
        ModelInfo(
            id = "mistral-7b",
            name = "Mistral-7B-Instruct-v0.3",
            fileName = "Mistral-7B-Instruct-v0.3-Q4_K_M.gguf",
            url = "https://huggingface.co/bartowski/Mistral-7B-Instruct-v0.3-GGUF/resolve/main/Mistral-7B-Instruct-v0.3-Q4_K_M.gguf",
            fileSize = 4370L * 1024 * 1024,
            requiredRamMb = 5200,
            description = "Mistral 7B v0.3. Быстрая и точная европейская модель.",
            descriptionEn = "Mistral 7B v0.3. Fast and accurate European model.",
            tags = listOf("EN", "quality"),
            category = ModelCategory.HEAVY
        ),
        ModelInfo(
            id = "gemma2-9b",
            name = "Gemma-2-9B-it",
            fileName = "gemma-2-9b-it-Q4_K_M.gguf",
            url = "https://huggingface.co/bartowski/gemma-2-9b-it-GGUF/resolve/main/gemma-2-9b-it-Q4_K_M.gguf",
            fileSize = 5760L * 1024 * 1024,
            requiredRamMb = 7000,
            description = "Google Gemma 2 9B. Мощная модель от Google. Для топ-устройств.",
            descriptionEn = "Google Gemma 2 9B. Powerful Google model. For top devices.",
            tags = listOf("EN", "quality"),
            category = ModelCategory.HEAVY
        ),
        // UNCENSORED
        ModelInfo(
            id = "dolphin-llama3-8b",
            name = "Dolphin-2.9-Llama3-8B",
            fileName = "dolphin-2.9-llama3-8b-Q4_K_M.gguf",
            url = "https://huggingface.co/cognitivecomputations/dolphin-2.9-llama3-8b-gguf/resolve/main/dolphin-2.9-llama3-8b-Q4_K_M.gguf",
            fileSize = 4920L * 1024 * 1024,
            requiredRamMb = 6000,
            description = "Dolphin на базе LLaMA 3 8B. Без цензуры и ограничений.",
            descriptionEn = "Dolphin based on LLaMA 3 8B. No censorship or restrictions.",
            tags = listOf("EN", "uncensored"),
            category = ModelCategory.UNCENSORED
        ),
        ModelInfo(
            id = "dolphin-mistral-7b",
            name = "Dolphin-2.6-Mistral-7B",
            fileName = "dolphin-2.6-mistral-7b.Q4_K_M.gguf",
            url = "https://huggingface.co/TheBloke/dolphin-2.6-mistral-7B-GGUF/resolve/main/dolphin-2.6-mistral-7b.Q4_K_M.gguf",
            fileSize = 4370L * 1024 * 1024,
            requiredRamMb = 5200,
            description = "Dolphin на Mistral 7B. Без цензуры, хорошее качество.",
            descriptionEn = "Dolphin on Mistral 7B. Uncensored, good quality.",
            tags = listOf("EN", "uncensored"),
            category = ModelCategory.UNCENSORED
        ),
        ModelInfo(
            id = "nous-hermes-2",
            name = "Nous-Hermes-2-Mistral-7B-DPO",
            fileName = "Nous-Hermes-2-Mistral-7B-DPO.Q4_K_M.gguf",
            url = "https://huggingface.co/TheBloke/Nous-Hermes-2-Mistral-7B-DPO-GGUF/resolve/main/Nous-Hermes-2-Mistral-7B-DPO.Q4_K_M.gguf",
            fileSize = 4370L * 1024 * 1024,
            requiredRamMb = 5200,
            description = "Nous Hermes 2 на Mistral. Без цензуры, DPO-обучение.",
            descriptionEn = "Nous Hermes 2 on Mistral. Uncensored, DPO-trained.",
            tags = listOf("EN", "uncensored"),
            category = ModelCategory.UNCENSORED
        ),
        ModelInfo(
            id = "mythomax-13b",
            name = "MythoMax-L2-13B",
            fileName = "mythomax-l2-13b.Q3_K_M.gguf",
            url = "https://huggingface.co/TheBloke/MythoMax-L2-13B-GGUF/resolve/main/mythomax-l2-13b.Q3_K_M.gguf",
            fileSize = 5870L * 1024 * 1024,
            requiredRamMb = 7500,
            description = "MythoMax 13B Q3. Творческая без цензуры. Только для мощных устройств.",
            descriptionEn = "MythoMax 13B Q3. Creative uncensored. Only for powerful devices.",
            tags = listOf("EN", "uncensored"),
            category = ModelCategory.UNCENSORED
        ),
        ModelInfo(
            id = "dolphin-phi2",
            name = "Dolphin-Phi-2",
            fileName = "dolphin-2_6-phi-2.Q4_K_M.gguf",
            url = "https://huggingface.co/TheBloke/dolphin-2_6-phi-2-GGUF/resolve/main/dolphin-2_6-phi-2.Q4_K_M.gguf",
            fileSize = 1600L * 1024 * 1024,
            requiredRamMb = 2000,
            description = "Dolphin Phi-2. Лёгкая модель без цензуры для слабых устройств.",
            descriptionEn = "Dolphin Phi-2. Light uncensored model for weak devices.",
            tags = listOf("EN", "uncensored", "fast"),
            category = ModelCategory.UNCENSORED
        ),
        // CODING
        ModelInfo(
            id = "qwen2.5-coder-7b",
            name = "Qwen2.5-Coder-7B-Instruct",
            fileName = "qwen2.5-coder-7b-instruct-q4_k_m.gguf",
            url = "https://huggingface.co/Qwen/Qwen2.5-Coder-7B-Instruct-GGUF/resolve/main/qwen2.5-coder-7b-instruct-q4_k_m.gguf",
            fileSize = 4680L * 1024 * 1024,
            requiredRamMb = 5500,
            description = "Qwen 7B Coder. Специализирована на коде. Python, JS, Kotlin и др.",
            descriptionEn = "Qwen 7B Coder. Specialized for code. Python, JS, Kotlin etc.",
            tags = listOf("EN", "code", "quality"),
            category = ModelCategory.CODING
        ),
        ModelInfo(
            id = "deepseek-coder-lite",
            name = "DeepSeek-Coder-V2-Lite",
            fileName = "DeepSeek-Coder-V2-Lite-Instruct-Q4_K_M.gguf",
            url = "https://huggingface.co/bartowski/DeepSeek-Coder-V2-Lite-Instruct-GGUF/resolve/main/DeepSeek-Coder-V2-Lite-Instruct-Q4_K_M.gguf",
            fileSize = 9200L * 1024 * 1024,
            requiredRamMb = 10000,
            description = "DeepSeek Coder V2 Lite. Мощная кодинг-модель для топ-устройств.",
            descriptionEn = "DeepSeek Coder V2 Lite. Powerful coding model for top devices.",
            tags = listOf("EN", "code", "quality"),
            category = ModelCategory.CODING
        ),
        // Additional models
        ModelInfo(
            id = "llama3.2-1b",
            name = "Llama-3.2-1B-Instruct",
            fileName = "Llama-3.2-1B-Instruct-Q4_K_M.gguf",
            url = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf",
            fileSize = 770L * 1024 * 1024,
            requiredRamMb = 1000,
            description = "Meta LLaMA 3.2 1B. Лёгкая и быстрая от Meta.",
            descriptionEn = "Meta LLaMA 3.2 1B. Light and fast from Meta.",
            tags = listOf("EN", "fast"),
            category = ModelCategory.LIGHT
        )
    )

    fun getRecommendedModels(deviceRamMb: Int): List<ModelInfo> {
        return models.filter { it.requiredRamMb <= deviceRamMb }
    }

    fun getModelById(id: String): ModelInfo? {
        return models.find { it.id == id }
    }

    fun getModelsByCategory(category: ModelCategory): List<ModelInfo> {
        return models.filter { it.category == category }
    }
}

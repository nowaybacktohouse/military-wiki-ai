package com.localchat.app.data.model

data class DumpInfo(
    val id: String,
    val name: String,
    val description: String,
    val sizeBytes: Long,
    val downloadUrl: String,
    val fileName: String,
    val category: DumpCategory,
    val isBuiltIn: Boolean = false
) {
    enum class DumpCategory(val label: String, val icon: String) {
        WIKIPEDIA("Википедия", "\uD83D\uDCDA"),
        MILITARY("Военная техника", "⚔\uFE0F"),
        CUSTOM("Пользовательский", "\uD83D\uDCC1")
    }

    val sizeFormatted: String
        get() {
            val gb = sizeBytes / (1024.0 * 1024.0 * 1024.0)
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (gb >= 1.0) "%.1f ГБ".format(gb) else "%.0f МБ".format(mb)
        }
}

object DumpCatalog {
    val availableDumps = listOf(
        DumpInfo(
            id = "military-mini",
            name = "Военная техника (мини)",
            description = "Справочник: танки, БМП, самолёты, вертолёты, корабли, ракетные системы. ~500 статей.",
            sizeBytes = 0L,
            downloadUrl = "",
            fileName = "military_dump.db",
            category = DumpInfo.DumpCategory.MILITARY,
            isBuiltIn = true
        ),
        DumpInfo(
            id = "wiki-ru-mini",
            name = "Википедия РУ (мини)",
            description = "Краткие статьи из русской Википедии. Топ-100К статей.",
            sizeBytes = 2_000_000_000L,
            downloadUrl = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki_ru_mini.db",
            category = DumpInfo.DumpCategory.WIKIPEDIA
        ),
        DumpInfo(
            id = "wiki-ru-full",
            name = "Википедия РУ (полная)",
            description = "Полный дамп русской Википедии. Все статьи с полным текстом.",
            sizeBytes = 12_000_000_000L,
            downloadUrl = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki_ru_full.db",
            category = DumpInfo.DumpCategory.WIKIPEDIA
        ),
        DumpInfo(
            id = "wiki-en-full",
            name = "Wikipedia EN (полная)",
            description = "Полный дамп английской Википедии. Все статьи.",
            sizeBytes = 22_000_000_000L,
            downloadUrl = "https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki_en_full.db",
            category = DumpInfo.DumpCategory.WIKIPEDIA
        )
    )
}

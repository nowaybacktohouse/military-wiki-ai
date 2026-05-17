package com.localchat.app.data.model

data class DumpInfo(
    val id: String,
    val name: String,
    val description: String,
    val sizeBytes: Long,
    val downloadUrl: String,
    val fileName: String,
    val category: DumpCategory,
    val isPinned: Boolean = false,
    val isBuiltIn: Boolean = false
) {
    enum class DumpCategory(val label: String, val icon: String) {
        MILITARY("Военная техника", "⚔️"),
        WIKIPEDIA_RU("Википедия RU", "\uD83C\uDDF7\uD83C\uDDFA"),
        WIKIPEDIA_EN("Wikipedia EN", "\uD83C\uDDFA\uD83C\uDDF8"),
        TECHNOLOGY("Технологии", "\uD83D\uDD27"),
        SCIENCE("Наука", "\uD83D\uDD2C"),
        HISTORY("История", "\uD83C\uDFDB️"),
        CUSTOM("Свой дамп", "\uD83D\uDCC1")
    }

    val sizeFormatted: String
        get() {
            val gb = sizeBytes / (1024.0 * 1024.0 * 1024.0)
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (gb >= 1.0) "%.1f ГБ".format(gb) else "%.0f МБ".format(mb)
        }
}

object DumpCatalog {
    val dumps = listOf(
        DumpInfo(
            id = "military-mini",
            name = "Военная техника (мини)",
            description = "~50 статей о танках, авиации, кораблях, ракетах, БПЛА, артиллерии всех стран. Встроенный дамп.",
            sizeBytes = 0L,
            downloadUrl = "",
            fileName = "",
            category = DumpInfo.DumpCategory.MILITARY,
            isPinned = true,
            isBuiltIn = true
        ),
        DumpInfo(
            id = "wiki-ru-military",
            name = "Военная техника (полный)",
            description = "Полная база по военной технике всех стран и эпох: танки, БМП, самолёты, вертолёты, корабли, подлодки, ракеты, ПВО, БПЛА, стрелковое оружие.",
            sizeBytes = 500_000_000L,
            downloadUrl = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki-ru-military.db",
            category = DumpInfo.DumpCategory.MILITARY,
            isPinned = true
        ),
        DumpInfo(
            id = "wiki-ru-tech",
            name = "Техника и технологии (RU)",
            description = "Автомобили, поезда, корабли, самолёты, космос, электроника, компьютеры, робототехника — всё из русской Википедии.",
            sizeBytes = 800_000_000L,
            downloadUrl = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki-ru-tech.db",
            category = DumpInfo.DumpCategory.TECHNOLOGY
        ),
        DumpInfo(
            id = "wiki-ru-science",
            name = "Наука (RU)",
            description = "Физика, химия, биология, математика, астрономия, медицина — научные статьи из русской Википедии.",
            sizeBytes = 600_000_000L,
            downloadUrl = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki-ru-science.db",
            category = DumpInfo.DumpCategory.SCIENCE
        ),
        DumpInfo(
            id = "wiki-ru-history",
            name = "История (RU)",
            description = "Войны, битвы, империи, государства, правители, революции — исторические статьи из русской Википедии.",
            sizeBytes = 700_000_000L,
            downloadUrl = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki-ru-history.db",
            category = DumpInfo.DumpCategory.HISTORY
        ),
        DumpInfo(
            id = "wiki-ru-mini",
            name = "Википедия RU (мини)",
            description = "Топ-50000 популярных статей из русской Википедии. Компактный дамп для быстрого поиска.",
            sizeBytes = 2_000_000_000L,
            downloadUrl = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki-ru-mini.db",
            category = DumpInfo.DumpCategory.WIKIPEDIA_RU
        ),
        DumpInfo(
            id = "wiki-ru-full",
            name = "Википедия RU (полная)",
            description = "Полный дамп русской Википедии. Более 1.9 млн статей. Требует ~12 ГБ свободного места.",
            sizeBytes = 12_000_000_000L,
            downloadUrl = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki-ru-full.db",
            category = DumpInfo.DumpCategory.WIKIPEDIA_RU
        ),
        DumpInfo(
            id = "wiki-en-mini",
            name = "Wikipedia EN (mini)",
            description = "Top 100,000 popular articles from English Wikipedia. Compact dump for fast search.",
            sizeBytes = 4_000_000_000L,
            downloadUrl = "https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki-en-mini.db",
            category = DumpInfo.DumpCategory.WIKIPEDIA_EN
        ),
        DumpInfo(
            id = "wiki-en-full",
            name = "Wikipedia EN (full)",
            description = "Полный дамп английской Википедии. Более 6.7 млн статей. Требует ~22 ГБ свободного места.",
            sizeBytes = 22_000_000_000L,
            downloadUrl = "https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki-en-full.db",
            category = DumpInfo.DumpCategory.WIKIPEDIA_EN
        )
    )
}

package com.localchat.app.data.model

data class DumpInfo(
    val id: String,
    val name: String,
    val nameEn: String,
    val description: String,
    val descriptionEn: String,
    val url: String,
    val fileName: String,
    val fileSize: Long,
    val isPinned: Boolean = false,
    val isBuiltIn: Boolean = false,
    val category: DumpCategory = DumpCategory.GENERAL
)

enum class DumpCategory(val label: String, val labelEn: String) {
    MILITARY("Военная техника", "Military Equipment"),
    WIKIPEDIA_RU("Русская Вики", "Russian Wiki"),
    WIKIPEDIA_EN("Английская Вики", "English Wiki"),
    THEMATIC("Тематические", "Thematic"),
    GENERAL("Общее", "General")
}

object DumpCatalog {
    val dumps = listOf(
        DumpInfo(
            id = "military-mini",
            name = "Военная техника (встроенный)",
            nameEn = "Military Equipment (built-in)",
            description = "~50 статей о танках, БМП, авиации, вертолётах, кораблях, ЗРК, БПЛА",
            descriptionEn = "~50 articles about tanks, IFVs, aviation, helicopters, ships, SAMs, UAVs",
            url = "",
            fileName = "",
            fileSize = 0,
            isPinned = true,
            isBuiltIn = true,
            category = DumpCategory.MILITARY
        ),
        DumpInfo(
            id = "wiki-ru-military",
            name = "Военная техника (РУ Вики)",
            nameEn = "Military Equipment (RU Wiki)",
            description = "Полный дамп статей о военной технике из русской Википедии",
            descriptionEn = "Full dump of military equipment articles from Russian Wikipedia",
            url = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki-ru-military.xml.bz2",
            fileSize = 4_200_000_000L,
            isPinned = true,
            category = DumpCategory.MILITARY
        ),
        DumpInfo(
            id = "wiki-ru-featured",
            name = "Русская Вики (избранное)",
            nameEn = "Russian Wiki (featured)",
            description = "Избранные статьи русской Википедии, ~50 000 статей (~2 ГБ)",
            descriptionEn = "Featured articles from Russian Wikipedia, ~50k articles (~2 GB)",
            url = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki-ru-featured.xml.bz2",
            fileSize = 2_000_000_000L,
            category = DumpCategory.WIKIPEDIA_RU
        ),
        DumpInfo(
            id = "wiki-ru-full",
            name = "Русская Вики (полная)",
            nameEn = "Russian Wiki (full)",
            description = "Полный дамп русской Википедии. ~20 ГБ, ~2 млн статей",
            descriptionEn = "Full Russian Wikipedia dump. ~20 GB, ~2M articles",
            url = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki-ru-full.xml.bz2",
            fileSize = 20_000_000_000L,
            category = DumpCategory.WIKIPEDIA_RU
        ),
        DumpInfo(
            id = "wiki-en-featured",
            name = "Английская Вики (избранное)",
            nameEn = "English Wiki (featured)",
            description = "Избранные статьи английской Википедии, ~100 000 статей (~3 ГБ)",
            descriptionEn = "Featured articles from English Wikipedia, ~100k articles (~3 GB)",
            url = "https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki-en-featured.xml.bz2",
            fileSize = 3_000_000_000L,
            category = DumpCategory.WIKIPEDIA_EN
        ),
        DumpInfo(
            id = "wiki-en-full",
            name = "Английская Вики (полная)",
            nameEn = "English Wiki (full)",
            description = "Полный дамп английской Википедии. ~30 ГБ, ~7 млн статей",
            descriptionEn = "Full English Wikipedia dump. ~30 GB, ~7M articles",
            url = "https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2",
            fileName = "wiki-en-full.xml.bz2",
            fileSize = 30_000_000_000L,
            category = DumpCategory.WIKIPEDIA_EN
        ),
        DumpInfo(
            id = "thematic-science",
            name = "Наука",
            nameEn = "Science",
            description = "Статьи о физике, химии, биологии, математике",
            descriptionEn = "Articles about physics, chemistry, biology, mathematics",
            url = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "thematic-science.xml.bz2",
            fileSize = 1_500_000_000L,
            category = DumpCategory.THEMATIC
        ),
        DumpInfo(
            id = "thematic-history",
            name = "История",
            nameEn = "History",
            description = "Статьи об истории всех стран и эпох",
            descriptionEn = "Articles about history of all countries and eras",
            url = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "thematic-history.xml.bz2",
            fileSize = 2_000_000_000L,
            category = DumpCategory.THEMATIC
        ),
        DumpInfo(
            id = "thematic-tech",
            name = "Технологии и IT",
            nameEn = "Technology & IT",
            description = "Статьи о программировании, компьютерах, интернете",
            descriptionEn = "Articles about programming, computers, internet",
            url = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "thematic-tech.xml.bz2",
            fileSize = 1_200_000_000L,
            category = DumpCategory.THEMATIC
        ),
        DumpInfo(
            id = "thematic-medicine",
            name = "Медицина",
            nameEn = "Medicine",
            description = "Статьи о медицине, болезнях, лекарствах",
            descriptionEn = "Articles about medicine, diseases, drugs",
            url = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "thematic-medicine.xml.bz2",
            fileSize = 1_000_000_000L,
            category = DumpCategory.THEMATIC
        ),
        DumpInfo(
            id = "thematic-art",
            name = "Искусство",
            nameEn = "Art",
            description = "Статьи о музыке, кино, живописи, литературе",
            descriptionEn = "Articles about music, cinema, painting, literature",
            url = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "thematic-art.xml.bz2",
            fileSize = 1_500_000_000L,
            category = DumpCategory.THEMATIC
        ),
        DumpInfo(
            id = "thematic-programming",
            name = "Программирование",
            nameEn = "Programming",
            description = "Языки программирования, фреймворки, алгоритмы",
            descriptionEn = "Programming languages, frameworks, algorithms",
            url = "https://dumps.wikimedia.org/ruwiki/latest/ruwiki-latest-pages-articles.xml.bz2",
            fileName = "thematic-programming.xml.bz2",
            fileSize = 800_000_000L,
            category = DumpCategory.THEMATIC
        )
    )

    fun getPinnedDumps(): List<DumpInfo> = dumps.filter { it.isPinned }
    fun getDownloadableDumps(): List<DumpInfo> = dumps.filter { !it.isBuiltIn }
    fun getDumpsByCategory(category: DumpCategory): List<DumpInfo> = dumps.filter { it.category == category }
}

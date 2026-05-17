package com.localchat.app.ui.dumps

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localchat.app.data.model.DumpCatalog
import com.localchat.app.data.model.DumpInfo
import com.localchat.app.service.DownloadProgress
import com.localchat.app.service.DownloadService
import com.localchat.app.service.KnowledgeArticle
import com.localchat.app.service.KnowledgeDatabase
import com.localchat.app.ui.theme.LocalChatColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DumpsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = KnowledgeDatabase(application)
    private val downloadService = DownloadService(application)

    private val _searchResults = MutableStateFlow<List<KnowledgeArticle>>(emptyList())
    val searchResults: StateFlow<List<KnowledgeArticle>> = _searchResults.asStateFlow()

    private val _articleCount = MutableStateFlow(0)
    val articleCount: StateFlow<Int> = _articleCount.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _downloads = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val downloads: StateFlow<Map<String, DownloadProgress>> = _downloads.asStateFlow()

    private val _installedDumps = MutableStateFlow<Set<String>>(setOf("military-mini"))
    val installedDumps: StateFlow<Set<String>> = _installedDumps.asStateFlow()

    private val _categories = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val categories: StateFlow<List<Pair<String, Int>>> = _categories.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        viewModelScope.launch {
            try { db.importMilitaryDump(application) } catch (_: Exception) {}
            refreshData()
        }
    }

    private suspend fun refreshData() {
        try {
            _articleCount.value = db.getArticleCount()
            _categories.value = db.getCategories()
        } catch (_: Exception) {}
    }

    fun selectTab(tab: Int) { _selectedTab.value = tab }

    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = db.search(query, limit = 20)
            } catch (_: Exception) {
                _searchResults.value = emptyList()
            }
            _isSearching.value = false
        }
    }

    fun downloadDump(dump: DumpInfo) {
        viewModelScope.launch {
            _downloads.value = _downloads.value + (dump.id to DownloadProgress(0, dump.sizeBytes))
            try {
                val app = getApplication<Application>()
                val file = java.io.File(app.filesDir, "dumps/${dump.fileName}")
                file.parentFile?.mkdirs()
                downloadService.download(dump.downloadUrl, dump.fileName, file.parentFile!!).collect { progress ->
                    _downloads.value = _downloads.value + (dump.id to progress)
                    if (progress.isComplete) {
                        _installedDumps.value = _installedDumps.value + dump.id
                        refreshData()
                    }
                }
            } catch (e: Exception) {
                _downloads.value = _downloads.value + (dump.id to DownloadProgress(0, dump.sizeBytes, error = e.message))
            }
        }
    }

    fun deleteDump(dump: DumpInfo) {
        viewModelScope.launch {
            db.deleteDump(dump.id)
            _installedDumps.value = _installedDumps.value - dump.id
            refreshData()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DumpsScreen(modifier: Modifier = Modifier, viewModel: DumpsViewModel = viewModel()) {
    val searchResults by viewModel.searchResults.collectAsState()
    val articleCount by viewModel.articleCount.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    val installedDumps by viewModel.installedDumps.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().background(LocalChatColors.background)) {
        // Header
        Surface(modifier = Modifier.fillMaxWidth(), color = LocalChatColors.surface, tonalElevation = 2.dp) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Знания", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LocalChatColors.onSurface)
                Text("$articleCount статей в базе", fontSize = 13.sp, color = LocalChatColors.onSurfaceVariant)
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = LocalChatColors.surface,
            contentColor = LocalChatColors.primary
        ) {
            Tab(selected = selectedTab == 0, onClick = { viewModel.selectTab(0) }, text = { Text("Поиск") })
            Tab(selected = selectedTab == 1, onClick = { viewModel.selectTab(1) }, text = { Text("Дампы") })
            Tab(selected = selectedTab == 2, onClick = { viewModel.selectTab(2) }, text = { Text("Свой дамп") })
        }

        when (selectedTab) {
            0 -> SearchTab(searchQuery, { searchQuery = it; viewModel.search(it) }, searchResults, isSearching, categories)
            1 -> DumpsTab(downloads, installedDumps, viewModel)
            2 -> CustomDumpTab()
        }
    }
}

@Composable
fun SearchTab(query: String, onQueryChange: (String) -> Unit, results: List<KnowledgeArticle>, isSearching: Boolean, categories: List<Pair<String, Int>>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск по базе знаний...", color = LocalChatColors.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LocalChatColors.onSurfaceVariant) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LocalChatColors.onSurface,
                    unfocusedTextColor = LocalChatColors.onSurface,
                    focusedBorderColor = LocalChatColors.primary,
                    unfocusedBorderColor = LocalChatColors.surfaceVariant,
                    cursorColor = LocalChatColors.primary,
                    focusedContainerColor = LocalChatColors.surfaceVariant,
                    unfocusedContainerColor = LocalChatColors.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        if (isSearching) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LocalChatColors.primary)
                }
            }
        }

        if (query.isBlank() && categories.isNotEmpty()) {
            item {
                Text("Категории", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = LocalChatColors.onSurface)
            }
            items(categories) { (cat, count) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = LocalChatColors.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(cat, fontSize = 14.sp, color = LocalChatColors.onSurface, modifier = Modifier.weight(1f))
                        Text("$count", fontSize = 13.sp, color = LocalChatColors.onSurfaceVariant)
                    }
                }
            }
        }

        if (query.isNotBlank() && !isSearching) {
            if (results.isEmpty()) {
                item {
                    Text("Ничего не найдено", fontSize = 14.sp, color = LocalChatColors.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                }
            }
            items(results) { article ->
                ArticleCard(article)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleCard(article: KnowledgeArticle) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
        shape = RoundedCornerShape(12.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(article.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = LocalChatColors.onSurface, modifier = Modifier.weight(1f))
                Surface(color = LocalChatColors.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                    Text(article.category, fontSize = 10.sp, color = LocalChatColors.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (expanded) article.content else article.content.take(150) + if (article.content.length > 150) "..." else "",
                fontSize = 13.sp, color = LocalChatColors.onSurfaceVariant, lineHeight = 18.sp
            )
            if (article.source.isNotBlank()) {
                Text("Источник: ${article.source}", fontSize = 11.sp, color = LocalChatColors.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun DumpsTab(downloads: Map<String, DownloadProgress>, installedDumps: Set<String>, viewModel: DumpsViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Pinned
        val pinned = DumpCatalog.dumps.filter { it.isPinned }
        if (pinned.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PushPin, contentDescription = null, tint = LocalChatColors.pinned, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Закреплённые", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LocalChatColors.pinned)
                }
            }
            items(pinned) { dump ->
                DumpCard(dump, installedDumps.contains(dump.id), downloads[dump.id], { viewModel.downloadDump(dump) }, { viewModel.deleteDump(dump) })
            }
        }

        // By category
        val grouped = DumpCatalog.dumps.filter { !it.isPinned }.groupBy { it.category }
        for ((category, dumps) in grouped) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("${category.icon} ${category.label}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LocalChatColors.onSurface)
            }
            items(dumps) { dump ->
                DumpCard(dump, installedDumps.contains(dump.id), downloads[dump.id], { viewModel.downloadDump(dump) }, { viewModel.deleteDump(dump) })
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Suppress("DEPRECATION")
@Composable
fun DumpCard(dump: DumpInfo, isInstalled: Boolean, progress: DownloadProgress?, onDownload: () -> Unit, onDelete: () -> Unit) {
    val isDownloading = progress != null && !progress.isComplete && progress.error == null
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (dump.isPinned) LocalChatColors.primaryContainer else LocalChatColors.card),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(dump.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = LocalChatColors.onSurface, modifier = Modifier.weight(1f))
                if (dump.isBuiltIn) {
                    Surface(color = LocalChatColors.success.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text("Встроенный", fontSize = 10.sp, color = LocalChatColors.success, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                } else if (isInstalled) {
                    Surface(color = LocalChatColors.success.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text("Установлен", fontSize = 10.sp, color = LocalChatColors.success, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            if (!dump.isBuiltIn) {
                Text(dump.sizeFormatted, fontSize = 12.sp, color = LocalChatColors.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(dump.description, fontSize = 12.sp, color = LocalChatColors.onSurfaceVariant, lineHeight = 16.sp)

            if (isDownloading && progress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = progress.percent / 100f, modifier = Modifier.fillMaxWidth().height(6.dp), color = LocalChatColors.downloadBar, trackColor = LocalChatColors.downloadBarTrack)
                Text("${progress.percent}%", fontSize = 11.sp, color = LocalChatColors.onSurfaceVariant)
            }
            if (progress?.error != null) {
                Text("Ошибка: ${progress.error}", fontSize = 11.sp, color = LocalChatColors.error)
            }

            if (!dump.isBuiltIn) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isInstalled && !isDownloading) {
                        Button(onClick = onDownload, colors = ButtonDefaults.buttonColors(containerColor = LocalChatColors.primary), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Скачать", fontSize = 13.sp)
                        }
                    }
                    if (isInstalled) {
                        OutlinedButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = LocalChatColors.error)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Удалить", fontSize = 13.sp, color = LocalChatColors.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomDumpTab() {
    var url by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = LocalChatColors.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Загрузить свой дамп", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LocalChatColors.onSurface)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Укажите URL дампа Wikipedia для загрузки в базу знаний. Поддерживаются XML-дампы Wikimedia.", fontSize = 13.sp, color = LocalChatColors.onSurfaceVariant, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Название дампа") },
                        placeholder = { Text("Мой дамп", color = LocalChatColors.onSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LocalChatColors.onSurface,
                            unfocusedTextColor = LocalChatColors.onSurface,
                            focusedBorderColor = LocalChatColors.primary,
                            unfocusedBorderColor = LocalChatColors.surfaceVariant,
                            cursorColor = LocalChatColors.primary,
                            focusedLabelColor = LocalChatColors.primary,
                            unfocusedLabelColor = LocalChatColors.onSurfaceVariant
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("URL дампа") },
                        placeholder = { Text("https://dumps.wikimedia.org/...", color = LocalChatColors.onSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LocalChatColors.onSurface,
                            unfocusedTextColor = LocalChatColors.onSurface,
                            focusedBorderColor = LocalChatColors.primary,
                            unfocusedBorderColor = LocalChatColors.surfaceVariant,
                            cursorColor = LocalChatColors.primary,
                            focusedLabelColor = LocalChatColors.primary,
                            unfocusedLabelColor = LocalChatColors.onSurfaceVariant
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LocalChatColors.primary),
                        enabled = url.isNotBlank() && name.isNotBlank()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Загрузить дамп", fontSize = 14.sp)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Полезные ссылки", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = LocalChatColors.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("dumps.wikimedia.org/ruwiki/latest/ - Русская Вики", fontSize = 13.sp, color = LocalChatColors.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("dumps.wikimedia.org/enwiki/latest/ - English Wiki", fontSize = 13.sp, color = LocalChatColors.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("dumps.wikimedia.org/ukwiki/latest/ - Украинская Вики", fontSize = 13.sp, color = LocalChatColors.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("dumps.wikimedia.org/dewiki/latest/ - Немецкая Вики", fontSize = 13.sp, color = LocalChatColors.primary)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

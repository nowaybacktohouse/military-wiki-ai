package com.localchat.app.ui.dumps

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localchat.app.data.model.DumpCatalog
import com.localchat.app.data.model.DumpInfo
import com.localchat.app.service.DownloadService
import com.localchat.app.service.KnowledgeArticle
import com.localchat.app.service.KnowledgeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class DumpsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "DumpsVM"
    }

    private val knowledgeDb = KnowledgeDatabase(application)
    private val downloadService = DownloadService(application)

    private val _searchResults = MutableStateFlow<List<KnowledgeArticle>>(emptyList())
    val searchResults: StateFlow<List<KnowledgeArticle>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    private val _availableSources = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val availableSources: StateFlow<List<Pair<String, Int>>> = _availableSources.asStateFlow()

    private val _selectedSources = MutableStateFlow<Set<String>>(emptySet())
    val selectedSources: StateFlow<Set<String>> = _selectedSources.asStateFlow()

    private val _articleCount = MutableStateFlow(0)
    val articleCount: StateFlow<Int> = _articleCount.asStateFlow()

    private val _selectedArticle = MutableStateFlow<KnowledgeArticle?>(null)
    val selectedArticle: StateFlow<KnowledgeArticle?> = _selectedArticle.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    private val _downloadedDumps = MutableStateFlow<Set<String>>(emptySet())
    val downloadedDumps: StateFlow<Set<String>> = _downloadedDumps.asStateFlow()

    private val _importProgress = MutableStateFlow<String?>(null)
    val importProgress: StateFlow<String?> = _importProgress.asStateFlow()

    private var currentQuery = ""
    private var currentOffset = 0

    init {
        viewModelScope.launch {
            refreshSources()
            refreshArticleCount()
            checkDownloadedDumps()
            search("")
        }
    }

    fun search(query: String) {
        currentQuery = query
        currentOffset = 0
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null
            try {
                val sources = _selectedSources.value.ifEmpty { null }
                val results = knowledgeDb.search(query, limit = 50, sources = sources)
                _searchResults.value = results
                _articleCount.value = results.size
            } catch (e: Exception) {
                Log.e(TAG, "Search error", e)
                _searchError.value = e.message
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun loadMore() {
        currentOffset += 50
        viewModelScope.launch {
            try {
                val sources = _selectedSources.value.ifEmpty { null }
                val more = knowledgeDb.getAllArticles(offset = currentOffset, limit = 50, sources = sources)
                _searchResults.value = _searchResults.value + more
            } catch (e: Exception) {
                Log.e(TAG, "loadMore error", e)
            }
        }
    }

    fun toggleSource(source: String) {
        val current = _selectedSources.value.toMutableSet()
        if (source in current) current.remove(source) else current.add(source)
        _selectedSources.value = current
        search(currentQuery)
    }

    fun clearSourceFilter() {
        _selectedSources.value = emptySet()
        search(currentQuery)
    }

    fun selectArticle(article: KnowledgeArticle) {
        viewModelScope.launch {
            val full = knowledgeDb.getArticleById(article.id)
            _selectedArticle.value = full ?: article
        }
    }

    fun clearSelectedArticle() {
        _selectedArticle.value = null
    }

    fun downloadDump(dump: DumpInfo) {
        if (dump.isBuiltIn || dump.url.isBlank()) return
        viewModelScope.launch {
            downloadService.download(dump.url, dump.fileName, downloadService.getDumpsDir()).collect { progress ->
                if (progress.error != null) {
                    _searchError.value = "Download error: ${progress.error}"
                    return@collect
                }
                val pct = if (progress.totalBytes > 0) progress.bytesDownloaded.toFloat() / progress.totalBytes else 0f
                _downloadProgress.value = _downloadProgress.value + (dump.id to pct)
                if (progress.isComplete) {
                    _downloadedDumps.value = _downloadedDumps.value + dump.id
                    _downloadProgress.value = _downloadProgress.value - dump.id
                }
            }
        }
    }

    fun deleteDump(dump: DumpInfo) {
        viewModelScope.launch {
            downloadService.deleteFile(dump.fileName, downloadService.getDumpsDir())
            knowledgeDb.deleteDump(dump.id)
            _downloadedDumps.value = _downloadedDumps.value - dump.id
            refreshSources()
            refreshArticleCount()
            search(currentQuery)
        }
    }

    fun importCustomDump(context: Context, uri: Uri) {
        viewModelScope.launch {
            _importProgress.value = "Starting import..."
            try {
                withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw Exception("Cannot open file")
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    var lineCount = 0
                    var articleCount = 0
                    val dumpName = "custom-${System.currentTimeMillis()}"

                    var currentTitle = ""
                    var currentContent = StringBuilder()
                    var inArticle = false

                    val lines = reader.readLines()
                    reader.close()
                    for (line in lines) {
                        lineCount++
                        if (lineCount % 1000 == 0) {
                            _importProgress.value = "Read $lineCount lines, imported $articleCount articles..."
                        }

                        when {
                            line.contains("<title>") -> {
                                currentTitle = line.substringAfter("<title>").substringBefore("</title>").trim()
                                currentContent = StringBuilder()
                                inArticle = true
                            }
                            line.contains("<text") && inArticle -> {
                                val text = line.substringAfter(">").substringBefore("</text>")
                                currentContent.append(text)
                            }
                            line.contains("</text>") && inArticle -> {
                                currentContent.append(line.substringBefore("</text>"))
                            }
                            line.contains("</page>") && inArticle -> {
                                if (currentTitle.isNotBlank() && currentContent.isNotBlank()) {
                                    val content = currentContent.toString()
                                        .replace("[[", "")
                                        .replace("]]", "")
                                        .take(10000)
                                    knowledgeDb.insertArticle(
                                        title = currentTitle,
                                        content = content,
                                        category = "custom",
                                        source = dumpName,
                                        dumpId = dumpName
                                    )
                                    articleCount++
                                }
                                inArticle = false
                            }
                        }
                    }
                    _importProgress.value = "Import complete: $articleCount articles"
                }
                refreshSources()
                refreshArticleCount()
                search(currentQuery)
            } catch (e: Exception) {
                Log.e(TAG, "Import error", e)
                _importProgress.value = "Error: ${e.message}"
            }
        }
    }

    private suspend fun refreshSources() {
        _availableSources.value = knowledgeDb.getAvailableSources()
    }

    private suspend fun refreshArticleCount() {
        _articleCount.value = knowledgeDb.getArticleCount()
    }

    private fun checkDownloadedDumps() {
        val dir = downloadService.getDumpsDir()
        val downloaded = mutableSetOf<String>()
        DumpCatalog.dumps.forEach { dump ->
            if (dump.isBuiltIn || downloadService.isFileDownloaded(dump.fileName, dir)) {
                downloaded.add(dump.id)
            }
        }
        _downloadedDumps.value = downloaded
    }
}

package com.localchat.app.ui.dumps

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.localchat.app.service.KnowledgeArticle
import com.localchat.app.service.KnowledgeDatabase
import com.localchat.app.ui.theme.LocalChatTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DumpsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = KnowledgeDatabase(application)

    private val _searchResults = MutableStateFlow<List<KnowledgeArticle>>(emptyList())
    val searchResults: StateFlow<List<KnowledgeArticle>> = _searchResults.asStateFlow()

    private val _articleCount = MutableStateFlow(0)
    val articleCount: StateFlow<Int> = _articleCount.asStateFlow()

    private val _categories = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val categories: StateFlow<List<Pair<String, Int>>> = _categories.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        viewModelScope.launch {
            db.importMilitaryDump(application)
            refreshStats()
        }
    }

    private suspend fun refreshStats() {
        _articleCount.value = db.getArticleCount()
        _categories.value = db.getCategories()
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            _searchResults.value = db.search(query, limit = 20)
            _isSearching.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DumpsScreen(modifier: Modifier = Modifier, viewModel: DumpsViewModel = viewModel()) {
    val searchResults by viewModel.searchResults.collectAsState()
    val articleCount by viewModel.articleCount.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LocalChatTheme.colors.background)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalChatTheme.colors.surface)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                "База знаний",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = LocalChatTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Статей: $articleCount",
                fontSize = 14.sp,
                color = LocalChatTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.search(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск по базе знаний...", color = LocalChatTheme.colors.onSurfaceVariant) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = LocalChatTheme.colors.onSurfaceVariant)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LocalChatTheme.colors.onSurface,
                    unfocusedTextColor = LocalChatTheme.colors.onSurface,
                    focusedBorderColor = LocalChatTheme.colors.primary,
                    unfocusedBorderColor = LocalChatTheme.colors.surfaceVariant,
                    cursorColor = LocalChatTheme.colors.primary,
                    focusedContainerColor = LocalChatTheme.colors.surfaceVariant,
                    unfocusedContainerColor = LocalChatTheme.colors.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        if (isSearching) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = LocalChatTheme.colors.primary,
                trackColor = LocalChatTheme.colors.surfaceVariant
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (searchQuery.isBlank()) {
                item {
                    Text(
                        "КАТЕГОРИИ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalChatTheme.colors.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(categories) { (category, count) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LocalChatTheme.colors.card),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category, fontWeight = FontWeight.Medium, color = LocalChatTheme.colors.onSurface)
                            Badge(containerColor = LocalChatTheme.colors.primaryContainer) {
                                Text("$count", color = LocalChatTheme.colors.primary, fontSize = 12.sp)
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LocalChatTheme.colors.card),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Подсказка",
                                fontWeight = FontWeight.Bold,
                                color = LocalChatTheme.colors.primary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Информация из базы знаний автоматически используется чат-ботом при ответах на ваши вопросы.",
                                fontSize = 13.sp,
                                color = LocalChatTheme.colors.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            } else {
                if (searchResults.isEmpty() && !isSearching) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Ничего не найдено",
                                color = LocalChatTheme.colors.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                items(searchResults) { article ->
                    ArticleCard(article)
                }

                item {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
fun ArticleCard(article: KnowledgeArticle) {
    var expanded by remember { mutableStateOf(false) }

    @OptIn(ExperimentalMaterial3Api::class)
    Card(
        onClick = { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = LocalChatTheme.colors.card),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    article.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = LocalChatTheme.colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text(article.category, fontSize = 10.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = LocalChatTheme.colors.surfaceVariant,
                        labelColor = LocalChatTheme.colors.primary
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (expanded) article.content else article.content.take(150) + if (article.content.length > 150) "..." else "",
                fontSize = 13.sp,
                color = LocalChatTheme.colors.onSurfaceVariant,
                lineHeight = 18.sp
            )
            if (article.source.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Источник: ${article.source}",
                    fontSize = 11.sp,
                    color = LocalChatTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

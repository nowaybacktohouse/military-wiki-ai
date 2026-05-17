package com.localchat.app.ui.dumps

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localchat.app.R
import com.localchat.app.data.model.DumpCatalog
import com.localchat.app.data.model.DumpCategory
import com.localchat.app.data.model.DumpInfo
import com.localchat.app.service.KnowledgeArticle
import com.localchat.app.ui.theme.LocalChatColors
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DumpsScreen(viewModel: DumpsViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_search),
        stringResource(R.string.tab_dumps),
        stringResource(R.string.tab_custom_dump)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = LocalChatColors.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 13.sp, maxLines = 1) }
                )
            }
        }

        when (selectedTab) {
            0 -> SearchTab(viewModel)
            1 -> DumpsTab(viewModel)
            2 -> CustomDumpTab(viewModel)
        }
    }
}

@OptIn(FlowPreview::class)
@Composable
fun SearchTab(viewModel: DumpsViewModel) {
    val articles by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchError by viewModel.searchError.collectAsState()
    val availableSources by viewModel.availableSources.collectAsState()
    val selectedSources by viewModel.selectedSources.collectAsState()
    val articleCount by viewModel.articleCount.collectAsState()

    var query by remember { mutableStateOf("") }

    // Debounce search - 300ms
    LaunchedEffect(Unit) {
        snapshotFlow { query }
            .debounce(300)
            .collectLatest { q ->
                viewModel.search(q)
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.search_articles), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LocalChatColors.primary,
                unfocusedBorderColor = LocalChatColors.surfaceVariant
            )
        )

        // Filter chips
        if (availableSources.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                @OptIn(ExperimentalMaterial3Api::class)
                FilterChip(
                    selected = selectedSources.isEmpty(),
                    onClick = { viewModel.clearSourceFilter() },
                    label = { Text(stringResource(R.string.filter_all), fontSize = 12.sp) }
                )
                availableSources.forEach { (source, count) ->
                    @OptIn(ExperimentalMaterial3Api::class)
                    FilterChip(
                        selected = source in selectedSources,
                        onClick = { viewModel.toggleSource(source) },
                        label = { Text("${getSourceLabel(source)} ($count)", fontSize = 12.sp) }
                    )
                }
            }
        }

        // Article count
        Text(
            text = "${stringResource(R.string.articles_found)}: $articleCount",
            fontSize = 12.sp,
            color = LocalChatColors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Search error snackbar
        searchError?.let { err ->
            Text(
                text = "${stringResource(R.string.search_error)}: $err",
                fontSize = 12.sp,
                color = LocalChatColors.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Results
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearching) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LocalChatColors.primary)
                    }
                }
            } else {
                items(articles, key = { it.id }) { article ->
                    ArticleCard(article = article, onClick = { viewModel.selectArticle(article) })
                }

                if (articles.isEmpty() && !isSearching) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (query.isNotBlank()) stringResource(R.string.no_results) else stringResource(R.string.no_articles),
                                fontSize = 14.sp,
                                color = LocalChatColors.onSurfaceVariant
                            )
                        }
                    }
                }

                // Load more
                if (articles.size >= 50) {
                    item {
                        TextButton(
                            onClick = { viewModel.loadMore() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.load_more))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Article detail dialog
    val selectedArticle by viewModel.selectedArticle.collectAsState()
    selectedArticle?.let { article ->
        ArticleDetailDialog(
            article = article,
            onDismiss = { viewModel.clearSelectedArticle() }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ArticleCard(article: KnowledgeArticle, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = getSourceIcon(article.dumpId),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = getSourceColor(article.dumpId)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = getSourceLabel(article.dumpId),
                    fontSize = 11.sp,
                    color = getSourceColor(article.dumpId)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = article.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = article.content.take(200),
                fontSize = 13.sp,
                color = LocalChatColors.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ArticleDetailDialog(article: KnowledgeArticle, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(article.title, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                item {
                    Text(
                        text = article.content,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
        containerColor = LocalChatColors.card
    )
}

@Composable
fun DumpsTab(viewModel: DumpsViewModel) {
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadedDumps by viewModel.downloadedDumps.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Pinned dumps section
        item {
            Text(
                text = stringResource(R.string.pinned_dumps),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        val pinned = DumpCatalog.getPinnedDumps()
        items(pinned, key = { it.id }) { dump ->
            DumpCard(
                dump = dump,
                isDownloaded = dump.isBuiltIn || dump.id in downloadedDumps,
                progress = downloadProgress[dump.id],
                onDownload = { viewModel.downloadDump(dump) },
                onDelete = { viewModel.deleteDump(dump) },
                isPinned = true
            )
        }

        // Categories
        DumpCategory.entries.filter { it != DumpCategory.MILITARY }.forEach { category ->
            val categoryDumps = DumpCatalog.getDumpsByCategory(category).filter { !it.isPinned }
            if (categoryDumps.isNotEmpty()) {
                item {
                    Text(
                        text = category.label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(categoryDumps, key = { it.id }) { dump ->
                    DumpCard(
                        dump = dump,
                        isDownloaded = dump.id in downloadedDumps,
                        progress = downloadProgress[dump.id],
                        onDownload = { viewModel.downloadDump(dump) },
                        onDelete = { viewModel.deleteDump(dump) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun DumpCard(
    dump: DumpInfo,
    isDownloaded: Boolean,
    progress: Float?,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    isPinned: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPinned) {
                    Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(14.dp), tint = LocalChatColors.pinned)
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = dump.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (dump.isBuiltIn) {
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(R.string.built_in), fontSize = 10.sp) },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dump.description,
                fontSize = 13.sp,
                color = LocalChatColors.onSurfaceVariant,
                lineHeight = 18.sp
            )

            if (!dump.isBuiltIn) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatFileSize(dump.fileSize),
                    fontSize = 12.sp,
                    color = LocalChatColors.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (progress != null && progress < 1f) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = LocalChatColors.downloadBar,
                        trackColor = LocalChatColors.downloadBarTrack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = LocalChatColors.primary)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isDownloaded) {
                            AssistChip(
                                onClick = {},
                                label = { Text(stringResource(R.string.downloaded), fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp), tint = LocalChatColors.success) },
                                modifier = Modifier.height(32.dp)
                            )
                            OutlinedButton(
                                onClick = onDelete,
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp), tint = LocalChatColors.error)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.delete), fontSize = 12.sp, color = LocalChatColors.error)
                            }
                        } else {
                            Button(
                                onClick = onDownload,
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LocalChatColors.primary),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Download, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.download), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomDumpTab(viewModel: DumpsViewModel) {
    val context = LocalContext.current
    val importProgress by viewModel.importProgress.collectAsState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importCustomDump(context, it) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.custom_dump_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.custom_dump_instructions),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.supported_formats),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ".xml, .xml.bz2, .json, .zim",
                        fontSize = 13.sp,
                        color = LocalChatColors.onSurfaceVariant
                    )
                }
            }
        }

        // SAF file picker button
        item {
            Button(
                onClick = {
                    filePicker.launch(arrayOf(
                        "application/xml",
                        "text/xml",
                        "application/json",
                        "application/octet-stream",
                        "*/*"
                    ))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = LocalChatColors.primary)
            ) {
                Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.pick_file), fontSize = 14.sp)
            }
        }

        // Import progress
        importProgress?.let { progress ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.importing),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = LocalChatColors.downloadBar
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = progress,
                            fontSize = 12.sp,
                            color = LocalChatColors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Useful links
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalChatColors.card),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.useful_links),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val links = listOf(
                        "https://dumps.wikimedia.org/ruwiki/latest/" to "Russian Wikipedia Dumps",
                        "https://dumps.wikimedia.org/enwiki/latest/" to "English Wikipedia Dumps",
                        "https://dumps.wikimedia.org/ukwiki/latest/" to "Ukrainian Wikipedia Dumps",
                        "https://dumps.wikimedia.org/dewiki/latest/" to "German Wikipedia Dumps",
                        "https://download.kiwix.org/zim/" to "Kiwix ZIM Files"
                    )

                    links.forEach { (url, label) ->
                        ClickableLinkRow(url = url, label = label, context = context)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClickableLinkRow(url: String, label: String, context: Context) {
    val annotatedString = buildAnnotatedString {
        pushStringAnnotation(tag = "URL", annotation = url)
        withStyle(style = SpanStyle(
            color = LocalChatColors.link,
            textDecoration = TextDecoration.Underline,
            fontSize = 13.sp
        )) {
            append(label)
        }
        pop()
    }

    Row(
        modifier = Modifier.combinedClickable(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
            onLongClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("link", url))
                Toast.makeText(context, context.getString(R.string.link_copied), Toast.LENGTH_SHORT).show()
            }
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp), tint = LocalChatColors.link)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = annotatedString)
    }
}

fun getSourceIcon(dumpId: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        dumpId.contains("military") -> Icons.Default.Shield
        dumpId.contains("ru") -> Icons.Default.Language
        dumpId.contains("en") -> Icons.Default.Language
        dumpId.contains("custom") -> Icons.Default.Person
        else -> Icons.Default.Article
    }
}

fun getSourceColor(dumpId: String): androidx.compose.ui.graphics.Color {
    return when {
        dumpId.contains("military") -> LocalChatColors.pinned
        dumpId.contains("ru") -> LocalChatColors.primary
        dumpId.contains("en") -> LocalChatColors.success
        dumpId.contains("custom") -> LocalChatColors.warning
        else -> LocalChatColors.onSurfaceVariant
    }
}

fun getSourceLabel(dumpId: String): String {
    return when {
        dumpId.contains("military") -> "Military"
        dumpId.contains("wiki-ru") -> "RU Wiki"
        dumpId.contains("wiki-en") -> "EN Wiki"
        dumpId.contains("custom") -> "Custom"
        dumpId.contains("science") -> "Science"
        dumpId.contains("history") -> "History"
        dumpId.contains("tech") -> "Tech"
        dumpId.contains("medicine") -> "Medicine"
        dumpId.contains("art") -> "Art"
        dumpId.contains("programming") -> "Code"
        else -> dumpId
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000L -> "%.1f GB".format(bytes / 1_000_000_000.0)
        bytes >= 1_000_000L -> "%.1f MB".format(bytes / 1_000_000.0)
        bytes >= 1_000L -> "%.1f KB".format(bytes / 1_000.0)
        else -> "$bytes B"
    }
}

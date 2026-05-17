package com.localchat.app.service

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.localchat.app.util.FtsQuerySanitizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class KnowledgeArticle(
    val id: Long,
    val title: String,
    val content: String,
    val category: String,
    val source: String,
    val dumpId: String = ""
)

class KnowledgeDatabase(context: Context) : SQLiteOpenHelper(context, "knowledge.db", null, 3) {

    companion object {
        private const val TAG = "KnowledgeDB"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS articles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                category TEXT NOT NULL DEFAULT '',
                source TEXT NOT NULL DEFAULT '',
                dump_id TEXT NOT NULL DEFAULT 'military',
                date_added INTEGER NOT NULL DEFAULT 0
            )
        """)
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS articles_fts USING fts4(
                title, content, category, content=articles, tokenize=unicode61
            )
        """)
        db.execSQL("""
            CREATE TRIGGER IF NOT EXISTS articles_ai AFTER INSERT ON articles BEGIN
                INSERT INTO articles_fts(rowid, title, content, category) VALUES (new.id, new.title, new.content, new.category);
            END
        """)
        db.execSQL("""
            CREATE TRIGGER IF NOT EXISTS articles_ad AFTER DELETE ON articles BEGIN
                INSERT INTO articles_fts(articles_fts, rowid, title, content, category) VALUES('delete', old.id, old.title, old.content, old.category);
            END
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db.execSQL("DROP TRIGGER IF EXISTS articles_ai")
            db.execSQL("DROP TRIGGER IF EXISTS articles_ad")
            db.execSQL("DROP TABLE IF EXISTS articles_fts")
            db.execSQL("DROP TABLE IF EXISTS articles")
            onCreate(db)
        }
    }

    suspend fun search(query: String, limit: Int = 50, sources: Set<String>? = null): List<KnowledgeArticle> = withContext(Dispatchers.IO) {
        val results = mutableListOf<KnowledgeArticle>()
        val sanitized = FtsQuerySanitizer.sanitize(query)

        try {
            val db = readableDatabase
            if (sanitized.isBlank()) {
                val args = mutableListOf<String>()
                val sourceFilter = buildSourceFilter(sources, args)
                args.add(limit.toString())
                val cursor = db.rawQuery(
                    "SELECT id, title, substr(content, 1, 200), category, source, dump_id FROM articles $sourceFilter ORDER BY title ASC LIMIT ?",
                    args.toTypedArray()
                )
                cursor.use {
                    while (it.moveToNext()) {
                        results.add(KnowledgeArticle(it.getLong(0), it.getString(1), it.getString(2), it.getString(3), it.getString(4), it.getString(5)))
                    }
                }
            } else {
                val args = mutableListOf(sanitized)
                val sourceFilter = buildSourceFilterForJoin(sources, args)
                args.add(limit.toString())
                val cursor = db.rawQuery(
                    """SELECT a.id, a.title, snippet(articles_fts, '<b>', '</b>', '...', 1, 40), a.category, a.source, a.dump_id
                       FROM articles a
                       JOIN articles_fts f ON a.id = f.rowid
                       WHERE articles_fts MATCH ?
                       $sourceFilter
                       LIMIT ?""",
                    args.toTypedArray()
                )
                cursor.use {
                    while (it.moveToNext()) {
                        results.add(KnowledgeArticle(it.getLong(0), it.getString(1), it.getString(2), it.getString(3), it.getString(4), it.getString(5)))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search error for query='$query', sanitized='$sanitized'", e)
            try {
                val db = readableDatabase
                val likeQuery = "%${query.take(100)}%"
                val cursor = db.rawQuery(
                    "SELECT id, title, substr(content, 1, 200), category, source, dump_id FROM articles WHERE title LIKE ? OR content LIKE ? LIMIT ?",
                    arrayOf(likeQuery, likeQuery, limit.toString())
                )
                cursor.use {
                    while (it.moveToNext()) {
                        results.add(KnowledgeArticle(it.getLong(0), it.getString(1), it.getString(2), it.getString(3), it.getString(4), it.getString(5)))
                    }
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback search also failed", e2)
            }
        }
        results
    }

    private fun buildSourceFilter(sources: Set<String>?, args: MutableList<String>): String {
        if (sources == null || sources.isEmpty()) return ""
        val placeholders = sources.joinToString(",") { "?" }
        args.addAll(sources)
        return "WHERE dump_id IN ($placeholders)"
    }

    private fun buildSourceFilterForJoin(sources: Set<String>?, args: MutableList<String>): String {
        if (sources == null || sources.isEmpty()) return ""
        val placeholders = sources.joinToString(",") { "?" }
        args.addAll(sources)
        return "AND a.dump_id IN ($placeholders)"
    }

    suspend fun getArticleById(id: Long): KnowledgeArticle? = withContext(Dispatchers.IO) {
        try {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT id, title, content, category, source, dump_id FROM articles WHERE id = ?", arrayOf(id.toString()))
            cursor.use {
                if (it.moveToFirst()) {
                    KnowledgeArticle(it.getLong(0), it.getString(1), it.getString(2), it.getString(3), it.getString(4), it.getString(5))
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getArticleById error", e)
            null
        }
    }

    suspend fun getAllArticles(offset: Int = 0, limit: Int = 50, sources: Set<String>? = null): List<KnowledgeArticle> = withContext(Dispatchers.IO) {
        val results = mutableListOf<KnowledgeArticle>()
        try {
            val db = readableDatabase
            val args = mutableListOf<String>()
            val sourceFilter = buildSourceFilter(sources, args)
            args.add(limit.toString())
            args.add(offset.toString())
            val cursor = db.rawQuery(
                "SELECT id, title, substr(content, 1, 200), category, source, dump_id FROM articles $sourceFilter ORDER BY title ASC LIMIT ? OFFSET ?",
                args.toTypedArray()
            )
            cursor.use {
                while (it.moveToNext()) {
                    results.add(KnowledgeArticle(it.getLong(0), it.getString(1), it.getString(2), it.getString(3), it.getString(4), it.getString(5)))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAllArticles error", e)
        }
        results
    }

    suspend fun getArticleCount(): Int = withContext(Dispatchers.IO) {
        try {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT COUNT(*) FROM articles", null)
            cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
        } catch (_: Exception) { 0 }
    }

    suspend fun getArticleCountByDump(dumpId: String): Int = withContext(Dispatchers.IO) {
        try {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT COUNT(*) FROM articles WHERE dump_id = ?", arrayOf(dumpId))
            cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
        } catch (_: Exception) { 0 }
    }

    suspend fun getAvailableSources(): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val db = readableDatabase
            val result = mutableListOf<Pair<String, Int>>()
            val cursor = db.rawQuery(
                "SELECT dump_id, COUNT(*) as cnt FROM articles GROUP BY dump_id ORDER BY cnt DESC", null
            )
            cursor.use {
                while (it.moveToNext()) {
                    result.add(it.getString(0) to it.getInt(1))
                }
            }
            result
        } catch (_: Exception) { emptyList() }
    }

    suspend fun insertArticle(title: String, content: String, category: String, source: String, dumpId: String = "custom") = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", title)
            put("content", content)
            put("category", category)
            put("source", source)
            put("dump_id", dumpId)
            put("date_added", System.currentTimeMillis())
        }
        db.insert("articles", null, values)
    }

    suspend fun importMilitaryDump(context: Context) = withContext(Dispatchers.IO) {
        try {
            val count = getArticleCountByDump("military")
            if (count > 0) return@withContext

            val db = writableDatabase
            db.beginTransaction()
            try {
                for (article in MilitaryDumpData.articles) {
                    val values = ContentValues().apply {
                        put("title", article.title)
                        put("content", article.content)
                        put("category", article.category)
                        put("source", article.source)
                        put("dump_id", "military")
                        put("date_added", System.currentTimeMillis())
                    }
                    db.insert("articles", null, values)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(TAG, "importMilitaryDump error", e)
        }
    }

    suspend fun deleteDump(dumpId: String) = withContext(Dispatchers.IO) {
        try {
            val db = writableDatabase
            db.delete("articles", "dump_id = ?", arrayOf(dumpId))
        } catch (e: Exception) {
            Log.e(TAG, "deleteDump error", e)
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        try {
            val db = writableDatabase
            db.execSQL("DELETE FROM articles")
            db.execSQL("DELETE FROM articles_fts")
        } catch (e: Exception) {
            Log.e(TAG, "clearAll error", e)
        }
    }
}

package com.localchat.app.service

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class KnowledgeArticle(
    val id: Long,
    val title: String,
    val content: String,
    val category: String,
    val source: String
)

class KnowledgeDatabase(context: Context) : SQLiteOpenHelper(context, "knowledge.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS articles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                category TEXT NOT NULL DEFAULT '',
                source TEXT NOT NULL DEFAULT ''
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
        db.execSQL("DROP TABLE IF EXISTS articles_fts")
        db.execSQL("DROP TABLE IF EXISTS articles")
        onCreate(db)
    }

    suspend fun search(query: String, limit: Int = 5): List<KnowledgeArticle> = withContext(Dispatchers.IO) {
        val results = mutableListOf<KnowledgeArticle>()
        val db = readableDatabase
        try {
            val cursor = db.rawQuery(
                """SELECT a.id, a.title, a.content, a.category, a.source
                   FROM articles a
                   JOIN articles_fts f ON a.id = f.rowid
                   WHERE articles_fts MATCH ?
                   ORDER BY rank
                   LIMIT ?""",
                arrayOf(query, limit.toString())
            )
            cursor.use {
                while (it.moveToNext()) {
                    results.add(
                        KnowledgeArticle(
                            id = it.getLong(0),
                            title = it.getString(1),
                            content = it.getString(2),
                            category = it.getString(3),
                            source = it.getString(4)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            val cursor = db.rawQuery(
                """SELECT id, title, content, category, source FROM articles
                   WHERE title LIKE ? OR content LIKE ?
                   LIMIT ?""",
                arrayOf("%$query%", "%$query%", limit.toString())
            )
            cursor.use {
                while (it.moveToNext()) {
                    results.add(
                        KnowledgeArticle(
                            id = it.getLong(0),
                            title = it.getString(1),
                            content = it.getString(2),
                            category = it.getString(3),
                            source = it.getString(4)
                        )
                    )
                }
            }
        }
        results
    }

    suspend fun getArticleCount(): Int = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM articles", null)
        cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    suspend fun getCategories(): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val result = mutableListOf<Pair<String, Int>>()
        val cursor = db.rawQuery(
            "SELECT category, COUNT(*) as cnt FROM articles GROUP BY category ORDER BY cnt DESC", null
        )
        cursor.use {
            while (it.moveToNext()) {
                result.add(it.getString(0) to it.getInt(1))
            }
        }
        result
    }

    suspend fun insertArticle(title: String, content: String, category: String, source: String) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", title)
            put("content", content)
            put("category", category)
            put("source", source)
        }
        db.insert("articles", null, values)
    }

    suspend fun importMilitaryDump(context: Context) = withContext(Dispatchers.IO) {
        val count = getArticleCount()
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
                }
                db.insert("articles", null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.execSQL("DELETE FROM articles")
        db.execSQL("DELETE FROM articles_fts")
    }
}

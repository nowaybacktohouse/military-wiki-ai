package com.localchat.app.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class DownloadProgress(
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val isComplete: Boolean = false,
    val error: String? = null
) {
    val percent: Int
        get() = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else 0
}

class DownloadService(private val context: Context) {

    companion object {
        private const val TAG = "DownloadService"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    fun getModelsDir(): File {
        val dir = File(context.filesDir, "models")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getDumpsDir(): File {
        val dir = context.getExternalFilesDir(null)?.let { File(it, "dumps") }
            ?: File(context.filesDir, "dumps")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun isFileDownloaded(fileName: String, dir: File): Boolean {
        return File(dir, fileName).exists()
    }

    fun getFilePath(fileName: String, dir: File): String {
        return File(dir, fileName).absolutePath
    }

    fun deleteFile(fileName: String, dir: File): Boolean {
        val file = File(dir, fileName)
        return if (file.exists()) file.delete() else true
    }

    fun download(url: String, fileName: String, destDir: File): Flow<DownloadProgress> = flow {
        try {
            val file = File(destDir, fileName)
            val tempFile = File(destDir, "$fileName.tmp")
            destDir.mkdirs()

            val requestBuilder = Request.Builder().url(url)

            val startByte: Long
            if (tempFile.exists() && tempFile.length() > 0) {
                startByte = tempFile.length()
                requestBuilder.addHeader("Range", "bytes=$startByte-")
            } else {
                startByte = 0L
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val responseCode = response.code

            val totalBytes: Long
            val append: Boolean

            if (responseCode == 206) {
                val contentRange = response.header("Content-Range")
                totalBytes = contentRange?.substringAfter("/")?.toLongOrNull() ?: -1L
                append = true
            } else if (responseCode in 200..299) {
                totalBytes = (response.body?.contentLength() ?: -1L) + startByte
                append = startByte == 0L
                if (!append) {
                    tempFile.delete()
                }
            } else {
                emit(DownloadProgress(0, 0, error = "HTTP $responseCode"))
                response.close()
                return@flow
            }

            emit(DownloadProgress(if (append) startByte else 0L, totalBytes))

            val body = response.body ?: run {
                emit(DownloadProgress(0, 0, error = "Empty response"))
                response.close()
                return@flow
            }

            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(tempFile, append)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var downloaded = if (append) startByte else 0L

            inputStream.use { input ->
                outputStream.use { output ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloaded += bytesRead
                        emit(DownloadProgress(downloaded, totalBytes))
                    }
                }
            }

            response.close()
            tempFile.renameTo(file)
            emit(DownloadProgress(downloaded, totalBytes, isComplete = true))

        } catch (e: Exception) {
            Log.e(TAG, "Download error", e)
            emit(DownloadProgress(0, 0, error = e.message ?: "Download failed"))
        }
    }.flowOn(Dispatchers.IO)
}

package com.localchat.app.service

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

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

    fun getModelsDir(): File {
        val dir = File(context.filesDir, "models")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getDumpsDir(): File {
        val dir = File(context.filesDir, "dumps")
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

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            if (tempFile.exists()) {
                connection.setRequestProperty("Range", "bytes=${tempFile.length()}-")
            }

            connection.connect()
            val responseCode = connection.responseCode

            val totalBytes: Long
            val startByte: Long
            val append: Boolean

            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                totalBytes = connection.getHeaderField("Content-Range")
                    ?.substringAfter("/")?.toLongOrNull() ?: -1L
                startByte = tempFile.length()
                append = true
            } else {
                totalBytes = connection.contentLengthLong
                startByte = 0L
                append = false
            }

            emit(DownloadProgress(startByte, totalBytes))

            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(tempFile, append)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var downloaded = startByte

            inputStream.use { input ->
                outputStream.use { output ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloaded += bytesRead
                        emit(DownloadProgress(downloaded, totalBytes))
                    }
                }
            }

            connection.disconnect()
            tempFile.renameTo(file)
            emit(DownloadProgress(downloaded, totalBytes, isComplete = true))

        } catch (e: Exception) {
            emit(DownloadProgress(0, 0, error = e.message ?: "Download failed"))
        }
    }.flowOn(Dispatchers.IO)
}

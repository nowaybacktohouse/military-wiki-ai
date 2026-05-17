package com.localchat.app.service

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    companion object {
        private const val TAG = "CrashHandler"
    }

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    fun install() {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val crashDir = File(context.getExternalFilesDir(null), "crashes")
            crashDir.mkdirs()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
            val fileName = "crash_${dateFormat.format(Date())}.txt"
            val file = File(crashDir, fileName)

            val sw = StringWriter()
            val pw = PrintWriter(sw)
            pw.println("LocalChat Crash Report")
            pw.println("Date: ${Date()}")
            pw.println("Thread: ${thread.name}")
            pw.println("Android: ${android.os.Build.VERSION.SDK_INT}")
            pw.println("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
            pw.println()
            throwable.printStackTrace(pw)
            pw.flush()

            file.writeText(sw.toString())
            Log.e(TAG, "Crash log saved to: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save crash log", e)
        }

        defaultHandler?.uncaughtException(thread, throwable)
    }

    fun getCrashLogs(): List<File> {
        val crashDir = File(context.getExternalFilesDir(null), "crashes")
        return crashDir.listFiles()?.filter { it.name.endsWith(".txt") }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}

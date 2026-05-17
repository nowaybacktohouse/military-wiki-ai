package com.localchat.app

import android.app.Application

class LocalChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: LocalChatApp
            private set
    }
}

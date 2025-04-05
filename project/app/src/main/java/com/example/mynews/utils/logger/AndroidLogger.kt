package com.example.mynews.utils.logger

import android.util.Log

class AndroidLogger : Logger {
    override fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }
    override fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    override fun e(tag: String, msg: String, throwable: Throwable) {
        Log.e(tag, msg, throwable) // stack trace will show in Logcat
    }


}
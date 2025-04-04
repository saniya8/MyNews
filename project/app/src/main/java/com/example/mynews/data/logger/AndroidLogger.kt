package com.example.mynews.data.logger

import android.util.Log
import com.example.mynews.domain.logger.Logger

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
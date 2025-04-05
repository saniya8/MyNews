package com.example.mynews.utils.logger

import com.example.mynews.utils.logger.Logger

class NoOpLogger : Logger {

    override fun d(tag: String, msg: String) {
    }

    override fun e(tag: String, msg: String)  {
    }

    override fun e(tag: String, msg: String, throwable: Throwable) {
    }
}
package com.example.mynews.data.logger

import com.example.mynews.domain.logger.Logger

class NoOpLogger : Logger {

    override fun d(tag: String, msg: String) {
    }

    override fun e(tag: String, msg: String)  {
    }

    override fun e(tag: String, msg: String, throwable: Throwable) {
    }
}
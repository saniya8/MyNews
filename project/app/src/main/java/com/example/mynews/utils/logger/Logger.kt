package com.example.mynews.utils.logger

interface Logger {
    fun d(tag: String, msg: String)
    fun e(tag: String, msg: String)
    fun e(tag: String, msg: String, throwable: Throwable)
}
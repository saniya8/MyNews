package com.example.mynews.service.repositories

import com.example.mynews.BuildConfig

object Constant {
    val NEWS_API_KEY = BuildConfig.NEWS_API_KEY
    val DIFFBOT_API_KEY =  BuildConfig.DIFFBOT_API_KEY
    val HUGGINGFACE_API_KEY = BuildConfig.HUGGINGFACE_API_KEY
    const val NEWS_API_BASE_URL = "https://newsapi.org/v2/"
    const val NEWS_BIAS_API_BASE_URL = "https://www.allsides.com/"
    const val USER_AGENT = "Mozilla/5.0"
}

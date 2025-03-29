package com.example.mynews.domain.model

import com.example.mynews.data.api.news.Article

data class Reaction(
    val userID: String,
    val article: Article,
    val reaction: String,
    val timestamp: Long
)
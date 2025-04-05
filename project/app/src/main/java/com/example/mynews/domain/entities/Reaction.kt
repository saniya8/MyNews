package com.example.mynews.domain.entities

import com.example.mynews.service.news.Article

data class Reaction(
    val userID: String,
    val article: Article,
    val reaction: String,
    val timestamp: Long
)
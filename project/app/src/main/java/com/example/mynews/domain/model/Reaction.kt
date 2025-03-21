package com.example.mynews.domain.model

import com.example.mynews.data.api.news.Article

data class Reaction(
    val userId: String,
    val article: Article,
    val reaction: String,
    val timestamp: Long
) {
    val userID: String
        get() {
            return userId
        }
}
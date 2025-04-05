package com.example.mynews.domain.repositories

import com.example.mynews.service.news.Article

interface HomeRepository {
    suspend fun getReaction(userID: String, article: Article) : String?
    suspend fun setReaction(userID: String, article: Article, reaction: String?): Boolean
    suspend fun trackReactions(userID: String, onReactionChanged: (Map<String, String?>) -> Unit)
}
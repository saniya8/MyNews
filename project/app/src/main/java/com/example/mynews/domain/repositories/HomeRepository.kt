package com.example.mynews.domain.repositories

import com.example.mynews.data.api.news.Article
import com.example.mynews.domain.model.Reaction

interface HomeRepository {
    suspend fun getReaction(userID: String, article: Article) : String?
    suspend fun setReaction(userID: String, article: Article, reaction: String?)
    suspend fun trackReactions(userID: String, onReactionChanged: (Map<String, String?>) -> Unit)
}
package com.example.mynews.domain.repositories.home

import com.example.mynews.domain.entities.Article
import com.example.mynews.domain.result.UpdateReactionResult

interface HomeRepository {
    suspend fun getReaction(userID: String, article: Article) : String?
    suspend fun setReaction(userID: String, article: Article, reaction: String?): UpdateReactionResult
    fun trackReactions(userID: String, onReactionChanged: (Map<String, String?>) -> Unit)
}
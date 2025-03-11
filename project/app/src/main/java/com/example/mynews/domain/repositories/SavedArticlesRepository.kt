package com.example.mynews.domain.repositories

import com.example.mynews.data.api.Article

interface SavedArticlesRepository {
    suspend fun saveArticle(userID: String, article: Article): Boolean
    suspend fun deleteSavedArticle(userID: String, article: Article): Boolean
    fun getSavedArticles(userID: String, onResult: (List<Article>) -> Unit)
}
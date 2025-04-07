package com.example.mynews.domain.repositories.home

import com.example.mynews.domain.entities.Article

interface SavedArticlesRepository {
    suspend fun saveArticle(userID: String, article: Article): Boolean
    suspend fun deleteSavedArticle(userID: String, article: Article): Boolean
    fun getSavedArticles(userID: String, onResult: (List<Article>) -> Unit)
}
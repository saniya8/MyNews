package com.example.mynews.domain.model.home

import androidx.lifecycle.LiveData
import com.example.mynews.domain.entities.Article

interface SavedArticlesModel {
    val savedArticles: LiveData<List<Article>>
    suspend fun saveArticle(userID: String, article: Article): Boolean
    suspend fun deleteSavedArticle(userID: String, article: Article): Boolean
    fun getSavedArticles(userID: String)

}
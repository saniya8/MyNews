package com.example.mynews.domain.model.home

import kotlinx.coroutines.flow.StateFlow

interface CondensedNewsArticleModel {
    val currentArticleUrl: StateFlow<String?>
    val articleText: StateFlow<String>
    val summarizedText: StateFlow<String>
    suspend fun fetchArticleText(url: String)
    suspend fun fetchSummarizedText(url: String, text: String, userID: String)
    fun clearCondensedArticleState()
    fun clearArticleText()
    fun clearSummarizedText()
}
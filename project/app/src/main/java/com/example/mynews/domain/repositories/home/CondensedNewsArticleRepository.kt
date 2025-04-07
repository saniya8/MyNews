package com.example.mynews.domain.repositories.home

interface CondensedNewsArticleRepository {
    suspend fun getArticleText(url: String): String
    suspend fun summarizeText(text: String, wordLimit: Int): String
}
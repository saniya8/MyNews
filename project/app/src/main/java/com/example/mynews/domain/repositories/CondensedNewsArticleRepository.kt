package com.example.mynews.domain.repositories

interface CondensedNewsArticleRepository {
    suspend fun getArticleText(url: String): String
}
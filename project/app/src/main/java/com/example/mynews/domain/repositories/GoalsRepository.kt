package com.example.mynews.domain.repositories

import com.example.mynews.data.api.news.Article
import kotlinx.coroutines.flow.Flow

interface GoalsRepository {
    suspend fun logArticleRead(userId: String, article: Article)
    suspend fun getStreak(userId: String): Int
    suspend fun updateStreak(userId: String)
    fun getStreakFlow(userId: String): Flow<Pair<Int, String>>
}

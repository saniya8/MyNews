package com.example.mynews.domain.repositories

import com.example.mynews.data.api.news.Article
import kotlinx.coroutines.flow.Flow
import com.example.mynews.domain.model.Mission

interface GoalsRepository {
    suspend fun logArticleRead(userId: String, article: Article)
    suspend fun getStreak(userId: String): Int
    suspend fun updateStreak(userId: String)
    fun getStreakFlow(userId: String): Flow<Pair<Int, String>>

    suspend fun getMissions(userId: String): List<Mission>
    fun getMissionsFlow(userId: String): Flow<List<Mission>>
    suspend fun updateMissionProgress(userId: String, missionId: String, newCount: Int)
    suspend fun markMissionComplete(userId: String, missionId: String)
}

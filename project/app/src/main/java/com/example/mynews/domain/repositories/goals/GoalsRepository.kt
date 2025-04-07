package com.example.mynews.domain.repositories.goals

import com.example.mynews.domain.entities.Article
import kotlinx.coroutines.flow.Flow
import com.example.mynews.domain.entities.Mission

interface GoalsRepository {
    suspend fun logArticleRead(userId: String, article: Article)
    suspend fun logReactionAdded(userId: String)
    suspend fun logReactionRemoved(userId: String)
    suspend fun logAddOrRemoveFriend(userId: String)
    fun getStreakFlow(userId: String): Flow<Pair<Int, String>>
    suspend fun getMissions(userId: String): List<Mission>
    fun getMissionsFlow(userId: String): Flow<List<Mission>>
    suspend fun updateMissionProgress(userId: String, missionId: String, newCount: Int)
    suspend fun markMissionComplete(userId: String, missionId: String)
}

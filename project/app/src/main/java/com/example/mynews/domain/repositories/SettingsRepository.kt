package com.example.mynews.domain.repositories

interface SettingsRepository {
    suspend fun getNumWordsToSummarize(userId: String): Int?
    suspend fun updateNumWordsToSummarize(userId: String, newNumWords: Int)
}
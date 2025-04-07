package com.example.mynews.domain.repositories.settings

interface SettingsRepository {
    suspend fun getNumWordsToSummarize(userId: String): Int?
    suspend fun updateNumWordsToSummarize(userId: String, newNumWords: Int)
}
package com.example.mynews.domain.repositories

interface SettingsRepository {
    suspend fun getWordLimit(userId: String): Int?
    suspend fun updateWordLimit(userId: String, newLimit: Int)
}
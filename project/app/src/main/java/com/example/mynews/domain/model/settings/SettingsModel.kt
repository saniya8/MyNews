package com.example.mynews.domain.model.settings

import com.example.mynews.domain.result.DeleteAccountResult
import kotlinx.coroutines.flow.StateFlow

interface SettingsModel {
    val username: StateFlow<String?>
    val email: StateFlow<String?>
    val numWordsToSummarize: StateFlow<Int>
    suspend fun fetchUsername(userId: String)
    suspend fun fetchEmail(userId: String)
    suspend fun fetchNumWordsToSummarize(userId: String)
    suspend fun updateNumWordsToSummarize(userId: String, newNumWords: Int)
    suspend fun logout(): Boolean
    suspend fun deleteAccount(password: String): DeleteAccountResult
}

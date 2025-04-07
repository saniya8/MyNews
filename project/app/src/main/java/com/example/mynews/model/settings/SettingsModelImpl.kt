package com.example.mynews.model.settings

import android.util.Log
import com.example.mynews.domain.model.settings.SettingsModel
import com.example.mynews.domain.repositories.settings.SettingsRepository
import com.example.mynews.domain.repositories.user.UserRepository
import com.example.mynews.domain.repositories.authentication.AuthRepository
import com.example.mynews.domain.result.DeleteAccountResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SettingsModelImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : SettingsModel {

    private val _username = MutableStateFlow<String?>(null)
    override val username: StateFlow<String?> = _username

    private val _email = MutableStateFlow<String?>(null)
    override val email: StateFlow<String?> = _email

    private val _numWordsToSummarize = MutableStateFlow(100)
    override val numWordsToSummarize: StateFlow<Int> = _numWordsToSummarize

    override suspend fun fetchUsername(userId: String) {
        val user = userRepository.getUserById(userId)
        _username.value = user?.username
    }

    override suspend fun fetchEmail(userId: String) {
        val user = userRepository.getUserById(userId)
        _email.value = user?.email
    }

    override suspend fun fetchNumWordsToSummarize(userId: String) {
        val numWords = settingsRepository.getNumWordsToSummarize(userId)
        _numWordsToSummarize.value = numWords ?: 100 // fallback to 100 if null
        Log.d("SettingsModel", "Fetched numWordsToSummarize: ${_numWordsToSummarize.value}")
    }

    override suspend fun updateNumWordsToSummarize(userId: String, newNumWords: Int) {
        _numWordsToSummarize.value = newNumWords
        settingsRepository.updateNumWordsToSummarize(userId, newNumWords)
        Log.d("SettingsModel", "Updated numWordsToSummarize to $newNumWords in Firestore")
    }

    override suspend fun logout(): Boolean {
        return authRepository.logout()
    }

    override suspend fun deleteAccount(password: String): DeleteAccountResult {
        return authRepository.deleteAccount(password)
    }
}
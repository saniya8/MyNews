package com.example.mynews.presentation.viewmodel.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.repositories.SettingsRepository
import com.example.mynews.domain.repositories.UserRepository


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
): ViewModel() {



    private val _logoutState = MutableStateFlow<Boolean?>(null)
    val logoutState: StateFlow<Boolean?> = _logoutState

    private val _isDeletingAccount = MutableStateFlow(false)
    val isDeletingAccount: StateFlow<Boolean> = _isDeletingAccount

    private val _deleteAccountState = MutableStateFlow<DeleteAccountResult?>(null)
    val deleteAccountState: StateFlow<DeleteAccountResult?> = _deleteAccountState

    private val _username = MutableStateFlow<String?>("")
    val username: StateFlow<String?> = _username

    private val _email = MutableStateFlow<String?>("")
    val email: StateFlow<String?> = _email


    private val _numWordsToSummarize = MutableStateFlow(100) // stores the first x words of article that summarizer should summarize from
    val numWordsToSummarize: StateFlow<Int> = _numWordsToSummarize

    private val _hasLoadedNumWordsToSummarize = MutableStateFlow(false)
    val hasLoadedNumWordsToSummarize: StateFlow<Boolean> = _hasLoadedNumWordsToSummarize


    fun fetchNumWordsToSummarize() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            if (userId.isNullOrEmpty()) {
                Log.e("SettingsViewModel", "No user logged in. User ID is null or empty")
                return@launch
            }

            val numWords = settingsRepository.getNumWordsToSummarize(userId) //
            _numWordsToSummarize.value = numWords ?: 100 // fallback to 100 (default) if not found
            _hasLoadedNumWordsToSummarize.value = true
            Log.d("SettingsDebug", "Num words to summarize loaded: ${_numWordsToSummarize.value}")
        }
    }

    fun resetHasLoadedNumWordsToSummarize() {
        _hasLoadedNumWordsToSummarize.value = false
    }

    fun updateNumWordsToSummarize(newNumWords: Int) {
        if (newNumWords in 50..200) {
            Log.d("CondensedSettings", "Updated numWordsToSummarize to $newNumWords")
            _numWordsToSummarize.value = newNumWords

            viewModelScope.launch {

                // get current user
                val userID = userRepository.getCurrentUserId()

                if (userID.isNullOrEmpty()) {
                    Log.e("SavedArticlesViewModel", "No user logged in. User ID is null or empty")
                    return@launch // return
                }

                settingsRepository.updateNumWordsToSummarize(userID, newNumWords)
            }

        } else {
            Log.d("SettingsDebug", "Out of range $newNumWords. Kept num words to summarize at ${_numWordsToSummarize.value}")
        }
    }

    fun fetchUsername() {

        viewModelScope.launch { // firestore operations are async so need this
            // get current user
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                Log.e("SettingsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            val user = userRepository.getUserById(userID)
            _username.value = user?.username
        }
    }


    fun fetchEmail() {

        viewModelScope.launch { // firestore operations are async so need this
            // get current user
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                Log.e("SettingsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            val user = userRepository.getUserById(userID)
            _email.value = user?.email
        }
    }

    fun logout() {
        viewModelScope.launch {
            val result = authRepository.logout()
            _logoutState.value = result
            Log.d("LogoutDebug", "Logout success: $result")
        }
    }

    fun resetLogoutState() {
        _logoutState.value = null
    }

    // TO DO: when deleting account delete from all relevant subcollections
    fun deleteAccount(password: String) {

        viewModelScope.launch {
            _isDeletingAccount.value = true
            val result = authRepository.deleteAccount(password)
            _deleteAccountState.value = result
            _isDeletingAccount.value = false
            Log.d("LogoutDebug", "Delete account success: $result")

        }
    }

    fun resetDeleteAccountState() {
        _deleteAccountState.value = null
    }
}

sealed class DeleteAccountResult {
    object Success : DeleteAccountResult()
    object IncorrectPassword : DeleteAccountResult()
    object Error : DeleteAccountResult()
}
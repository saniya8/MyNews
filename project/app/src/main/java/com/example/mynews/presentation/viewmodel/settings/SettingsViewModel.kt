package com.example.mynews.presentation.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.domain.model.settings.SettingsModel
import com.example.mynews.domain.model.user.UserModel
import com.example.mynews.domain.result.DeleteAccountResult
import com.example.mynews.utils.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userModel: UserModel,
    private val settingsModel: SettingsModel,
    private val logger: Logger,
): ViewModel() {

    val username: StateFlow<String?> = settingsModel.username
    val email: StateFlow<String?> = settingsModel.email
    val numWordsToSummarize: StateFlow<Int> = settingsModel.numWordsToSummarize

    private val _logoutState = MutableStateFlow<Boolean?>(null)
    val logoutState: StateFlow<Boolean?> = _logoutState

    private val _isDeletingAccount = MutableStateFlow(false)
    val isDeletingAccount: StateFlow<Boolean> = _isDeletingAccount

    private val _deleteAccountState = MutableStateFlow<DeleteAccountResult?>(null)
    val deleteAccountState: StateFlow<DeleteAccountResult?> = _deleteAccountState

    private val _hasLoadedNumWordsToSummarize = MutableStateFlow(false)
    val hasLoadedNumWordsToSummarize: StateFlow<Boolean> = _hasLoadedNumWordsToSummarize


    fun fetchNumWordsToSummarize() {
        viewModelScope.launch {
            val userId = userModel.getCurrentUserId()
            if (userId.isNullOrEmpty()) {
                logger.e("SettingsViewModel", "No user logged in. User ID is null or empty")
                return@launch
            }

            settingsModel.fetchNumWordsToSummarize(userId)
            _hasLoadedNumWordsToSummarize.value = true
        }
    }

    fun resetHasLoadedNumWordsToSummarize() {
        _hasLoadedNumWordsToSummarize.value = false
    }

    fun updateNumWordsToSummarize(newNumWords: Int) {
        if (newNumWords in 50..200) {
            logger.d("CondensedSettings", "Updated numWordsToSummarize to $newNumWords")

            viewModelScope.launch {

                // get current user
                val userID = userModel.getCurrentUserId()

                if (userID.isNullOrEmpty()) {
                    logger.e("SettingsViewModel", "No user logged in. User ID is null or empty")
                    return@launch // return
                }

                settingsModel.updateNumWordsToSummarize(userID, newNumWords)
            }

        } else {
            logger.d("SettingsDebug", "Out of range $newNumWords")
        }
    }

    fun fetchUsername() {

        viewModelScope.launch {
            // get current user
            val userID = userModel.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                logger.e("SettingsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            settingsModel.fetchUsername(userID)
        }
    }

    fun fetchEmail() {

        viewModelScope.launch {
            // get current user
            val userID = userModel.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                logger.e("SettingsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            settingsModel.fetchEmail(userID)
        }
    }

    fun logout() {
        viewModelScope.launch {
            val result = settingsModel.logout()
            _logoutState.value = result
            logger.d("LogoutDebug", "Logout success: $result")
        }
    }

    fun resetLogoutState() {
        _logoutState.value = null
    }

    fun deleteAccount(password: String) {

        viewModelScope.launch {
            _isDeletingAccount.value = true
            val result = settingsModel.deleteAccount(password)
            _deleteAccountState.value = result
            _isDeletingAccount.value = false
            logger.d("LogoutDebug", "Delete account success: $result")

        }
    }

    fun resetDeleteAccountState() {
        _deleteAccountState.value = null
    }
}


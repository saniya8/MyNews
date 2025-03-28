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
import com.example.mynews.domain.repositories.UserRepository


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
): ViewModel() {



    private val _logoutState = MutableStateFlow<Boolean?>(null)
    val logoutState: StateFlow<Boolean?> = _logoutState

    private val _deleteAccountState = MutableStateFlow<DeleteAccountResult?>(null)
    val deleteAccountState: StateFlow<DeleteAccountResult?> = _deleteAccountState

    private val _username = MutableStateFlow<String?>("")
    val username: StateFlow<String?> = _username

    private val _wordLimit = MutableStateFlow(100)
    val wordLimit: StateFlow<Int> = _wordLimit

    fun logout() {
        viewModelScope.launch {
            val success = authRepository.logout()
            _logoutState.value = success
            Log.d("LogoutDebug", "Logout success: $success")
        }
    }

    fun getWordLimit(): Int {
        return _wordLimit.value
    }

    fun updateWordLimit(inputWordLimit: Int) {
        println("Updated wordlimit to $inputWordLimit")
        _wordLimit.value = inputWordLimit
    }

    fun getUsername(userId: String) {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            _username.value = user?.username
        }
    }

    fun resetLogoutState() {
        _logoutState.value = null
    }

    // TO DO: when deleting account delete from all relevant subcollections
    fun deleteAccount(password: String) {
        viewModelScope.launch {
            val result = authRepository.deleteAccount(password)
            _deleteAccountState.value = result

            if (result == DeleteAccountResult.Success) {
                _logoutState.value = true
            }
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
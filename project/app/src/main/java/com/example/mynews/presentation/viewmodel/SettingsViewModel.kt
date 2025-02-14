package com.example.mynews.presentation.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.mynews.domain.model.User
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

    fun logout() {
        viewModelScope.launch {
            val success = authRepository.logout()
            _logoutState.value = success
            Log.d("LogoutDebug", "Logout success: $success")
        }
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
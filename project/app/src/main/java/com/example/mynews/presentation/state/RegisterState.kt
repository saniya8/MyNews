package com.example.mynews.presentation.state

data class RegisterState(
    val emailInput: String = "",
    val usernameInput: String = "",
    val passwordInput: String = "",
    val passwordRepeatedInput: String = "",
    val isInputValid: Boolean = false,
    val isPasswordShown: Boolean = false,
    val isPasswordRepeatedShown: Boolean = false,
    val errorMessageInput: String? = null,
    val isLoading: Boolean = false,
    val isSuccessfullyRegistered: Boolean = false,
    val errorMessageRegister: String? = null
)


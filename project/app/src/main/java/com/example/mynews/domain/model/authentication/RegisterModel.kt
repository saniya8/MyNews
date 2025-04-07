package com.example.mynews.domain.model.authentication

import androidx.compose.runtime.MutableState

interface RegisterModel {

    suspend fun performRegistration(
        email: String,
        username: String,
        password: String,
        isUsernameTaken: MutableState<Boolean>,
        isEmailAlreadyUsed: MutableState<Boolean>
    ): Boolean

}
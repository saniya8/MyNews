package com.example.mynews.model.authentication

import androidx.compose.runtime.MutableState
import com.example.mynews.domain.model.authentication.RegisterModel
import com.example.mynews.domain.repositories.authentication.AuthRepository
import javax.inject.Inject

class RegisterModelImpl @Inject constructor(
    private val authRepository: AuthRepository
) : RegisterModel {

    override suspend fun performRegistration(
        email: String,
        username: String,
        password: String,
        isUsernameTaken: MutableState<Boolean>,
        isEmailAlreadyUsed: MutableState<Boolean>
    ): Boolean {
        return authRepository.register(
            email = email,
            username = username,
            password = password,
            isUsernameTaken = isUsernameTaken,
            isEmailAlreadyUsed = isEmailAlreadyUsed
        )
    }
}

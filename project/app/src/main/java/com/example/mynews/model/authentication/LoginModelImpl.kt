package com.example.mynews.model.authentication

import com.example.mynews.domain.model.authentication.LoginModel
import com.example.mynews.domain.repositories.authentication.AuthRepository
import javax.inject.Inject

class LoginModelImpl @Inject constructor(
    private val authRepository: AuthRepository
) : LoginModel {

    override suspend fun performLogin(email: String, password: String): Boolean {
        return authRepository.login(email = email, password = password)
    }
}
package com.example.mynews.data

import com.example.mynews.domain.repositories.AuthRepository
import kotlinx.coroutines.delay

class AuthRepositoryImpl : AuthRepository {

    override suspend fun login(email: String, password: String): Boolean {
        delay(1000)
        return true
    }

    override suspend fun register(email: String, username: String, password: String): Boolean {
        delay(1000)
        return true
    }





}
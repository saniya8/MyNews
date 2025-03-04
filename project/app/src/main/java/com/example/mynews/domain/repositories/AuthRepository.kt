package com.example.mynews.domain.repositories

import com.example.mynews.presentation.viewmodel.settings.DeleteAccountResult

interface AuthRepository {
    suspend fun login(email: String, password:String):Boolean
    suspend fun register(email:String, username: String, password: String):Boolean
    suspend fun logout(): Boolean
    suspend fun deleteAccount(userPassword: String): DeleteAccountResult
    suspend fun getLoginState(): Boolean
}

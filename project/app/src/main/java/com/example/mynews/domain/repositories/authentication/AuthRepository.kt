package com.example.mynews.domain.repositories.authentication

import androidx.compose.runtime.MutableState
import com.example.mynews.domain.result.DeleteAccountResult

interface AuthRepository {
    suspend fun login(email: String, password:String):Boolean
    suspend fun register(email:String,
                         username: String,
                         password: String,
                         isUsernameTaken: MutableState<Boolean>,
                         isEmailAlreadyUsed: MutableState<Boolean>): Boolean
    suspend fun logout(): Boolean
    suspend fun deleteAccount(userPassword: String): DeleteAccountResult
    suspend fun getLoginState(): Boolean
}

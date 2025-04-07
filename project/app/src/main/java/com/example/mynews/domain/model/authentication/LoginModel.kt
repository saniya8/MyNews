package com.example.mynews.domain.model.authentication

interface LoginModel {

    suspend fun performLogin(email: String, password: String): Boolean

}
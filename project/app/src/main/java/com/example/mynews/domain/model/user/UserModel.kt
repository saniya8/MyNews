package com.example.mynews.domain.model.user

interface UserModel {
    suspend fun getCurrentUserId(): String?
}
package com.example.mynews.domain.repositories

import com.example.mynews.domain.entities.User

interface UserRepository {
    suspend fun addUser(user: User): Boolean
    suspend fun isUsernameTaken(username: String): Boolean
    suspend fun reserveUsername(username: String, uid: String)
    suspend fun initializeUserSettings(userId: String)
    suspend fun getUserById(userId: String): User?
    suspend fun getCurrentUserId():String?
    suspend fun clearUserDataById(userId: String): Boolean
}
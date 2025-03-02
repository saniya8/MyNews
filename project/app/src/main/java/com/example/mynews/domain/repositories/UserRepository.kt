package com.example.mynews.domain.repositories

import com.example.mynews.domain.model.User

interface UserRepository {
    suspend fun addUser(user: User): Boolean
    suspend fun getUserById(userId: String): User?
    suspend fun getCurrentUserId():String?
    suspend fun deleteUserById(userId: String): Boolean
    suspend fun updateUserFriends(userId: String, friends: List<String>): Boolean
    suspend fun getUserFriends(userId: String): List<String>
    suspend fun getAllUsers(): List<String>
}
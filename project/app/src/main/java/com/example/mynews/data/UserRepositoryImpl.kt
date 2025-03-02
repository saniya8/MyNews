package com.example.mynews.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.mynews.domain.model.User
import com.example.mynews.domain.repositories.UserRepository

// Implementation of the UserRepository interface in .com.example.mynews/domain/repositories

class UserRepositoryImpl (
    private val firestore : FirebaseFirestore
): UserRepository {

    override suspend fun addUser(user: User): Boolean {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding user", e)
            false
        }
    }

    override suspend fun getUserById(userId: String): User? {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user", e)
            null
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    override suspend fun deleteUserById(userId: String): Boolean {
        return try {
            firestore.collection("users").document(userId).delete().await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error deleting user from firestore", e)
            false
        }
    }

    override suspend fun updateUserFriends(userId: String, friends: List<String>): Boolean {
        return try {
            firestore.collection("users").document(userId).update("friends", friends).await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user friends", e)
            false
        }
    }

    override suspend fun getUserFriends(userId: String): List<String> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            document.get("friends") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user friends", e)
            emptyList()
        }
    }

    // Fetch all users from Firestore
    override suspend fun getAllUsers(): List<String> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            snapshot.documents.map { it.id } // Assuming usernames are document IDs
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching users", e)
            emptyList()
        }
    }
}
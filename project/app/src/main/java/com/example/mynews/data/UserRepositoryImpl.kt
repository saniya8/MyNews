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
        try {
            // user's username and email already normalized in AuthRepository's register,
            // where this function is called from
            firestore.collection("users").document(user.uid).set(user).await()
            return true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding user", e)
            return false
        }
    }

    // isUsernameTaken: returns true if username is already taken by another user, false otherwise
    override suspend fun isUsernameTaken(username: String): Boolean {
        Log.d("UsernameDebug", "In isUsernameTaken")
        try {
            val normalizedUsername = username.trim().lowercase()
            val doc = firestore.collection("usernames").document(normalizedUsername).get().await()
            Log.d("UsernameDebug", "Username taken? : ${doc.exists()}")
            return doc.exists() // doc.exists() is true if username is taken, false otherwise,
        } catch (e: Exception) {
            Log.e("UsernameDebug", "Error checking username: ${e.message}", e)
            return true // Assume taken if Firestore query fails
        }
    }

    // reserveUsername: reserves username for the user so no one else can take it
    override suspend fun reserveUsername(username: String, uid: String) {
        try {

            val normalizedUsername = username.trim().lowercase()

            // create the username document
            firestore.collection("usernames").document(normalizedUsername).set(mapOf<String, Any>()).await()

            // store the UID in the private subcollection
            firestore.collection("usernames").document(normalizedUsername)
                .collection("private").document("uid")
                .set(mapOf("uid" to uid)).await()
            Log.d("UsernameDebug", "Username '$normalizedUsername' reserved for UID: $uid")
        } catch (e: Exception) {
            Log.e("UsernameDebug", "Error reserving username: ${e.message}", e)
        }
    }


    override suspend fun getUserById(userId: String): User? {
        try {
            val document = firestore.collection("users").document(userId).get().await()
            return document.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user", e)
            return null
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    override suspend fun deleteUserById(userId: String): Boolean {
        try {
            firestore.collection("users").document(userId).delete().await()
            return true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error deleting user from firestore", e)
            return false
        }
    }
}
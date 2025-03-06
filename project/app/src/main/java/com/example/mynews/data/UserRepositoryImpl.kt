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

    override suspend fun isUsernameTaken(username: String): Boolean {
        Log.d("UsernameDebug", "In isUsernameTaken")
        return try {
            val doc = firestore.collection("usernames").document(username).get().await()
            Log.d("UsernameDebug", "Username taken? : ${doc.exists()}")
            doc.exists() // doc.exists() is true if username is taken, false otherwise,
        } catch (e: Exception) {
            Log.e("UsernameDebug", "Error checking username: ${e.message}", e)
            true // Assume taken if Firestore query fails
        }
    }

    /*
    override suspend fun reserveUsername(username: String, uid: String) {
        try {
            firestore.collection("usernames").document(username).set(mapOf("uid" to uid)).await()
            Log.d("UsernameDebug", "Username '$username' reserved for UID: $uid")
        } catch (e: Exception) {
            Log.e("UsernameDebug", "Error reserving username: ${e.message}", e)
        }
    }

     */

    override suspend fun reserveUsername(username: String, uid: String) {
        try {
            // create the username document
            firestore.collection("usernames").document(username).set(mapOf<String, Any>()).await()

            // store the UID in the private subcollection
            firestore.collection("usernames").document(username)
                .collection("private").document("uid")
                .set(mapOf("uid" to uid)).await()
            Log.d("UsernameDebug", "Username '$username' reserved for UID: $uid")
        } catch (e: Exception) {
            Log.e("UsernameDebug", "Error reserving username: ${e.message}", e)
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
}
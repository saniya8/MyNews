package com.example.mynews.data


import android.util.Log
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.model.User
import com.example.mynews.domain.repositories.UserRepository
import com.example.mynews.presentation.viewmodel.DeleteAccountResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Implementation of the AuthRepository interface in .com.example.mynews/domain/repositories

class AuthRepositoryImpl (
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : AuthRepository {

    // version 4 - works with version 4 of onLoginClick in LoginViewModel
    override suspend fun login(email: String, password: String): Boolean {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                Log.d("FirebaseAuth", "User ${firebaseUser.uid} logged in successfully")

                // Firestore update runs in a separate coroutine (NON-BLOCKING)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        firestore.collection("users").document(firebaseUser.uid)
                            .update("loggedIn", true)
                            .await()
                        Log.d("Firestore", "User ${firebaseUser.uid} isLoggedIn updated successfully")
                    } catch (e: Exception) {
                        Log.e("Firestore", "Failed to update Firestore: ${e.message}")
                    }
                }

                return true // Return immediately, UI won't be stuck waiting for Firestore
            } else {
                Log.d("FirebaseAuth", "Login succeeded, but currentUser is null")
                return false
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Login failed: ${e.message}", e)
            return false
        }
    }

    // version 2 - works with version 2 of onRegisterClick in RegisterViewModel
    override suspend fun register(email: String, username: String, password: String): Boolean {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                Log.d("FirebaseAuth", "User ${firebaseUser.uid} registered successfully")

                // Firestore update runs in a separate coroutine (NON-BLOCKING)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val userFromFirestore = userRepository.getUserById(firebaseUser.uid)
                        if (userFromFirestore == null) {
                            // User doesn't exist in Firestore, so add them
                            val newUser = User(
                                uid = firebaseUser.uid,
                                username = username,
                                email = email,
                                loggedIn = true
                            )
                            userRepository.addUser(newUser)
                            Log.d("Firestore", "User ${firebaseUser.uid} added to Firestore successfully")
                        } else {
                            Log.d("Firestore", "User ${firebaseUser.uid} already exists in Firestore")
                        }
                    } catch (e: Exception) {
                        Log.e("Firestore", "Failed to add user to Firestore: ${e.message}")
                    }
                }

                return true // Register function returns immediately, UI won't be stuck waiting for Firestore
            } else {
                Log.d("FirebaseAuth", "Registration succeeded, but currentUser is null")
                return false
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Registration failed: ${e.message}", e)
            return false
        }
    }

    // version 1 - original - test later - might need Coroutine Scope
    /*
    override suspend fun logout(): Boolean {
        try {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            firebaseUser?.let { user ->
                val userId = user.uid
                // Update isLoggedIn field to false in Firestore
                firestore.collection("users").document(userId)
                    .update("loggedIn", false)
                    .await()
                return true
            }
            Log.e("AuthRepository", "No user logged in.")
            return false
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error logging out user", e)
            return false
        }
    }

     */

    // version 3 - working but might have to integrate stuff from version 1 back in
    override suspend fun logout(): Boolean {
        return try {
            FirebaseAuth.getInstance().signOut() // Only sign out the user
            Log.d("AuthRepository", "User signed out successfully")
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error logging out user", e)
            false
        }
    }


    // test later - might need Coroutine Scope
    override suspend fun deleteAccount(userPassword: String): DeleteAccountResult {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, userPassword)
                currentUser.reauthenticate(credential).await()

                val userId = currentUser.uid
                // delete them from the users collection
                userRepository.deleteUserById(userId)
                currentUser.delete().await()
                return DeleteAccountResult.Success
            } else {
                Log.e("AuthRepository", "No user logged in.")
                return DeleteAccountResult.Error
            }
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            Log.e("AuthRepository", "User needs to reauthenticate", e)
            return DeleteAccountResult.Error
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            // Incorrect password provided by the user
            Log.e("AuthRepository", "Incorrect password", e)
            return DeleteAccountResult.IncorrectPassword
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error deleting account", e)
            return DeleteAccountResult.Error
        }
    }

    // test later - might need Coroutine Scope
    override suspend fun getLoginState(): Boolean {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userDocument =
                    firestore.collection("users").document(currentUser.uid).get().await()
                return userDocument.getBoolean("loggedIn") ?: false
            } else {
                // If currentUser is null, the user is not logged in
                return false
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error getting login state", e)
            return false
        }
    }

}
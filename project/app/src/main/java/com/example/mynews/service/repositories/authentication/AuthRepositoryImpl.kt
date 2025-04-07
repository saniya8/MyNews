package com.example.mynews.service.repositories.authentication

import android.util.Log
import androidx.compose.runtime.MutableState
import com.example.mynews.domain.entities.User
import com.example.mynews.domain.repositories.authentication.AuthRepository
import com.example.mynews.domain.repositories.user.UserRepository
import com.example.mynews.domain.result.DeleteAccountResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepositoryImpl (
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : AuthRepository {

    override suspend fun login(email: String, password: String): Boolean {
        try {

            val normalizedEmail = email.trim().lowercase()

            val authResult = auth.signInWithEmailAndPassword(normalizedEmail, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                Log.d("FirebaseAuth", "User ${firebaseUser.uid} logged in successfully")

                // firestore update runs in a separate coroutine
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

                return true // return immediately, UI won't be stuck waiting for Firestore
            } else {
                Log.d("FirebaseAuth", "Login succeeded, but currentUser is null")
                return false
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Login failed: ${e.message}", e)
            return false
        }
    }

    override suspend fun register(email: String,
                                  username: String,
                                  password: String,
                                  isUsernameTaken: MutableState<Boolean>,
                                  isEmailAlreadyUsed: MutableState<Boolean>
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {

                val normalizedEmail = email.trim().lowercase()
                val normalizedUsername = username.trim().lowercase()


                Log.d("UsernameDebug", "Checking if username '$normalizedUsername' is taken...")
                if (userRepository.isUsernameTaken(normalizedUsername)) {
                    Log.d("AuthRepository", "Username '$normalizedUsername' is already taken")
                    isUsernameTaken.value = true
                    return@withContext false
                }

                Log.d("UsernameDebug", "Username is available. Continuing registration...")

                val authResult = auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    Log.d("FirebaseAuth", "User ${firebaseUser.uid} registered successfully")

                    // firestore update runs in a separate coroutine

                    try {
                        val userFromFirestore = userRepository.getUserById(firebaseUser.uid)
                        if (userFromFirestore == null) {
                            // User doesn't exist in Firestore, so add them
                            val newUser = User(
                                uid = firebaseUser.uid,
                                username = normalizedUsername,
                                email = normalizedEmail,
                                loggedIn = true
                            )

                            // reserveUsername only triggers if user was successfully added

                            val added = userRepository.addUser(newUser)
                            if (added) {

                                try {
                                    userRepository.reserveUsername(normalizedUsername, firebaseUser.uid)
                                } catch (e: Exception) {
                                    Log.e("AuthRepository", "Failed to reserve username", e)
                                    return@withContext false

                                }
                                try {
                                    userRepository.initializeUserSettings(firebaseUser.uid)
                                } catch (e: Exception) {
                                    Log.e("AuthRepository", "Failed to initialize user settings", e)
                                    return@withContext false
                                }

                            } else {
                                Log.e("AuthRepository", "Failed to add user to Firestore, skipping username + settings setup")
                                return@withContext false
                            }

                            Log.d(
                                "Firestore",
                                "User ${firebaseUser.uid} added to Firestore successfully"
                            )
                        } else {
                            Log.d(
                                "Firestore",
                                "User ${firebaseUser.uid} already exists in Firestore"
                            )
                        }

                        return@withContext true

                    } catch (e: Exception) {
                        Log.e("Firestore", "Failed to add user to Firestore: ${e.message}")
                        return@withContext false
                    }

                } else {
                    Log.d("FirebaseAuth", "Registration succeeded, but currentUser is null")
                    return@withContext false
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                Log.e("AuthRepository", "Email already in use: ${e.message}")
                isEmailAlreadyUsed.value = true
                return@withContext false
            } catch (e: Exception) {
                Log.e("FirebaseAuth", "Registration failed: ${e.message}", e)
                return@withContext false
            }
        }
    }

    override suspend fun logout(): Boolean {
        try {
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            firebaseUser?.let { user ->
                val userId = user.uid
                firestore.collection("users").document(userId)
                    .update("loggedIn", false)
                    .await()  // wait for Firestore update before signing out
                Log.d("AuthRepository", "User $userId logged out and Firestore updated")
            }

            FirebaseAuth.getInstance().signOut()
            Log.d("AuthRepository", "User signed out successfully")
            return true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error logging out user", e)
            return false
        }
    }

    override suspend fun deleteAccount(userPassword: String): DeleteAccountResult {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, userPassword)
                currentUser.reauthenticate(credential).await()

                val userId = currentUser.uid

                // clear user's firestore data first
                val clearDataSuccess = userRepository.clearUserDataById(userId)
                if (!clearDataSuccess) {
                    Log.e("AuthRepository", "Failed to delete Firestore data for user: $userId")
                    return DeleteAccountResult.Error
                }

                // only delete the user after the firestore data is successfully deleted
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

    override suspend fun getLoginState(): Boolean {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userDocument =
                    firestore.collection("users").document(currentUser.uid).get().await()
                return userDocument.getBoolean("loggedIn") ?: false
            } else {
                return false
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error getting login state", e)
            return false
        }
    }

}
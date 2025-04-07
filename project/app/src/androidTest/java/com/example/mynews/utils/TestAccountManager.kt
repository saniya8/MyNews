package com.example.mynews.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class TestAccountManager(
    private val email: String,
    private val password: String
) {
    private val auth = FirebaseAuth.getInstance()

    suspend fun loginOrRegister(): FirebaseUser {
        return try {
            auth.signInWithEmailAndPassword(email, password).await().user!!
        } catch (e: FirebaseAuthInvalidUserException) {
            auth.createUserWithEmailAndPassword(email, password).await().user!!
        }
    }

    fun currentUser(): FirebaseUser? = auth.currentUser
}


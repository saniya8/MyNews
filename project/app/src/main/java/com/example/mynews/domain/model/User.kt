package com.example.mynews.domain.model

data class User (
    val email: String = "",
    val uid: String = "",
    val username: String = "",
    val loggedIn: Boolean = false,
)


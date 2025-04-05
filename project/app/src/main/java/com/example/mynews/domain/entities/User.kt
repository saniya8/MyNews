package com.example.mynews.domain.entities

data class User (
    val email: String = "",
    val uid: String = "",
    val username: String = "",
    val loggedIn: Boolean = false,
)


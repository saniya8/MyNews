package com.example.mynews.domain.model

data class User (
    val email: String = "",
    val uid: String = "",
    val username: String = "",
    var groupCode: String? = null, // nullable since when a user joins they are not in a gorup
    val loggedIn: Boolean = false
)


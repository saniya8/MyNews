package com.example.mynews.domain.entities

data class Reaction(
    val userID: String,
    val article: Article,
    val reaction: String,
    val timestamp: Long
)
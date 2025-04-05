package com.example.mynews.domain.entities

data class Streak(
    val count: Int, // streak number
    val lastReadDate: String // last time article was read
)
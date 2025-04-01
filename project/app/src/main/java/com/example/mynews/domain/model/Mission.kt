package com.example.mynews.domain.model

data class Mission(
    val id: String,
    val name: String,
    val description: String,
    val targetCount: Int,
    val currentCount: Int,
    val isCompleted: Boolean,
    val type: String
)


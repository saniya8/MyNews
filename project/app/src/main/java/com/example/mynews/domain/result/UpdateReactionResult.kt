package com.example.mynews.domain.result

data class UpdateReactionResult(
    val isFirstReaction: Boolean,
    val wasDeleted: Boolean,
    val wasSwitched: Boolean
)
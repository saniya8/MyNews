package com.example.mynews.data.newsbias
import kotlinx.serialization.Serializable

@Serializable
data class NewsBias(
    val source_name: String,
    val media_bias_rating: String
)
package com.example.mynews.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class NewsBiasResponse(
    val allsides_media_bias_ratings: List<AllSidesData> = emptyList()
)

@Serializable
data class AllSidesData(
    val publication: Publication = Publication()
)

@Serializable
data class Publication(
    val allsides_url: String = "",
    val media_bias_rating: String = "Center",
    val source_name: String = "",
    val source_type: String = "",
    val source_url: String = ""
)


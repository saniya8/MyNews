package com.example.mynews.data.api.news

import kotlinx.serialization.Serializable

@Serializable
data class NewsResponse(
    val articles: List<Article> = emptyList(),
    val status: String = "",
    val totalResults: Int = 0
)

@Serializable
data class Article(
    val author: String = "Unknown",
    val content: String = "",
    val description: String = "",
    val publishedAt: String = "",
    val source: Source = Source(),
    val title: String = "",
    val url: String = "",
    val urlToImage: String = ""
)

@Serializable
data class Source(
    val id: String = "",
    val name: String = "Unknown"
)


package com.example.mynews.service.news

import kotlinx.serialization.Serializable

// for /v2/top-headlines and /v2/everything
@Serializable
data class NewsResponse(
    val status: String = "",
    val totalResults: Int = 0,
    val articles: List<Article> = emptyList(),
)

// for /v2/top-headlines/sources
@Serializable
data class SourcesResponse(
    val status: String = "",
    val sources: List<Source> = emptyList()
)

@Serializable
data class Article(
    val source: Source = Source(),
    val author: String = "Unknown",
    val title: String = "",
    val description: String = "",
    val url: String = "",
    val urlToImage: String = "",
    val publishedAt: String = "",
    val content: String = "",
)

@Serializable
data class Source(
    val id: String = "",
    val name: String = "Unknown",
    val description: String = "",
    val url: String = "",
    val category: String = "",
    val language: String = "",
    val country: String = ""
)


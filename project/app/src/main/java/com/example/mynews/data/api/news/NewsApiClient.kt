package com.example.mynews.data.api.news

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class NewsApiClient {

    private val baseUrl = "https://newsapi.org/v2/"
    private val userAgent = "Mozilla/5.0"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    suspend fun getTopHeadlines(apiKey: String, language: String = "en"): NewsResponse {
        return client.get("${baseUrl}top-headlines") {
            headers {
                append("X-Api-Key", apiKey)
                append("User-Agent", userAgent)
            }
            url {
                parameters.append("language", language)
            }
        }.body()
    }

    suspend fun getTopHeadlinesByCategory(
        apiKey: String,
        category: String,
        language: String = "en"
    ): NewsResponse {
        return client.get("${baseUrl}top-headlines") {
            headers {
                append("X-Api-Key", apiKey)
                append("User-Agent", userAgent)
            }
            url {
                parameters.append("language", language)
                parameters.append("category", category)
            }
        }.body()
    }

    suspend fun getEverythingBySearch(
        apiKey: String,
        query: String,
        language: String = "en"
    ): NewsResponse {
        return client.get("${baseUrl}everything") {
            headers {
                append("X-Api-Key", apiKey)
                append("User-Agent", userAgent)
            }
            url {
                parameters.append("language", language)
                parameters.append("q", query)
            }
        }.body()
    }


}
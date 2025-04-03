package com.example.mynews.data.api.news

import com.example.mynews.data.Constant
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class NewsApiClient {

    private val baseUrl = Constant.NEWS_API_BASE_URL
    private val userAgent = Constant.USER_AGENT

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

    suspend fun getSourcesByCountry(
        apiKey: String,
        country: String,
        language: String = "en"
    ): SourcesResponse {
        return client.get("${baseUrl}top-headlines/sources") {
            headers {
                append("X-Api-Key", apiKey)
                append("User-Agent", userAgent)
            }
            url {
                parameters.append("language", language)
                parameters.append("country", country)

            }
        }.body()
    }

    suspend fun getTopHeadlinesBySources(
        apiKey: String,
        sources: String, // comma-separated string of source IDs per API documentation
        language: String = "en"
    ): NewsResponse {
        return client.get("${baseUrl}top-headlines") {
            headers {
                append("X-Api-Key", apiKey)
                append("User-Agent", userAgent)
            }
            url {
                parameters.append("language", language)
                parameters.append("sources", sources)
            }
        }.body()
    }

    // TEST: to fetch all sources
    suspend fun getAllSources(
        apiKey: String,
        language: String = "en"
    ): SourcesResponse {
        return client.get("${baseUrl}top-headlines/sources") {
            headers {
                append("X-Api-Key", apiKey)
                append("User-Agent", userAgent)
            }
            url {
                parameters.append("language", language)
            }
        }.body()
    }




}
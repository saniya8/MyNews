package com.example.mynews.data.api.newsbias

import com.example.mynews.data.api.newsbias.NewsBiasResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class NewsBiasApiClient {

    private val baseUrl = "https://www.allsides.com/"
    private val userAgent = "Mozilla/5.0"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    suspend fun getBiasRatings(): NewsBiasResponse {
        return client.get("${baseUrl}media-bias/json/noncommercial/publications") {
            headers {
                append("User-Agent", userAgent) // ⬅️ Required to prevent 403
            }
        }.body()
    }
}

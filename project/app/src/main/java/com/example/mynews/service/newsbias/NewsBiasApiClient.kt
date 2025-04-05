package com.example.mynews.service.newsbias

import com.example.mynews.model.Constant
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.*

// Custom plugin to remove the Accept-Charset header
val RemoveAcceptCharset = createClientPlugin("RemoveAcceptCharset") {
    on(Send) { request ->
        request.headers.remove(HttpHeaders.AcceptCharset)
        proceed(request)
    }
}


class NewsBiasApiClient {

    private val baseUrl = Constant.NEWS_BIAS_API_BASE_URL
    private val userAgent = Constant.USER_AGENT

    /*private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }*/

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            // register both content types
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }, contentType = ContentType("text", "json"))

            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }, contentType = ContentType.Application.Json)
        }

        install(DefaultRequest) {
            header(HttpHeaders.UserAgent, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            header(HttpHeaders.Accept, "application/json, text/javascript, */*; q=0.01")
            header(HttpHeaders.AcceptLanguage, "en-US,en;q=0.9")
            header(HttpHeaders.AcceptEncoding, "gzip, deflate, br")
            header("Referer", "https://www.allsides.com/")
            header(HttpHeaders.Connection, "keep-alive")
            header("Upgrade-Insecure-Requests", "1")
        }

        install(RemoveAcceptCharset)
    }




    suspend fun getBiasRatings(): NewsBiasResponse {
        return client.get("${baseUrl}media-bias/json/noncommercial/publications") {
            headers {
                append("User-Agent", userAgent) // required to prevent 403
            }
        }.body()
    }
}

package com.example.mynews.service.condensednewsarticle

import android.util.Log
import com.example.mynews.service.repositories.Constant
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.json.JSONArray
import org.json.JSONObject

class CondensedNewsArticleApiClient {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }

        install(ContentEncoding) { // Enable gzip decompression
            gzip()
        }

    }

    suspend fun getArticleText(url: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val apiUrl =
                    "https://api.diffbot.com/v3/article?token=${Constant.DIFFBOT_API_KEY}&url=$url"

                //  GET request using Ktor and fetch the response body as text
                val response: HttpResponse = client.get(apiUrl) {
                    headers {
                        append(HttpHeaders.Accept, "application/json")
                        append(HttpHeaders.UserAgent, Constant.USER_AGENT)
                    }
                }

                Log.d("CondensedNewsApi", "Response Status: ${response.status}")

                val responseText = response.bodyAsText()
                Log.d("CondensedNewsApi", "Raw Response Body: $responseText")

                if (response.status != HttpStatusCode.OK) {
                    return@withContext "Error: API returned ${response.status}"
                }

                val jsonResponse = JSONObject(responseText)
                Log.d("CondensedNewsApi", "Json response $jsonResponse")
                val htmlContent = jsonResponse.getJSONArray("objects").getJSONObject(0).getString("html")
                    ?: return@withContext "No article content found in Diffbot response"

                Log.d("CondensedNewsApi", "Extracted text: $htmlContent")

                return@withContext htmlContent


            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext "Error: ${e.message}"
            }
        }

    }


    suspend fun summarizeText(text: String, wordLimit: Int): String = withContext(Dispatchers.IO) {
        val url = "https://router.huggingface.co/hf-inference/models/facebook/bart-large-cnn"


        if (text.startsWith("Error:")) {
            return@withContext "Error: Unable to summarize text."
        }

        try {

            val requestBody = JsonObject(
                mapOf(
                    "inputs" to JsonPrimitive(text),
                    "parameters" to JsonObject(
                        mapOf(
                            "max_length" to JsonPrimitive(wordLimit),
                            "min_length" to JsonPrimitive(wordLimit / 2),
                            "do_sample" to JsonPrimitive(false)
                        )
                    )
                )
            )

            // POST request using Ktor
            val response: HttpResponse = client.post(url) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${Constant.HUGGINGFACE_API_KEY}")
                    append( HttpHeaders.ContentType, "application/json" /*ContentType.Application.Json.toString()*/)
                    append(HttpHeaders.UserAgent, Constant.USER_AGENT)
                }
                setBody(requestBody)
            }

            Log.d("CondensedNewsApi", "Hugging Face Status: ${response.status}")
            val responseText = response.bodyAsText()
            Log.d("CondensedNewsApi", "Hugging Face Raw Response: $responseText")

            if (response.status != HttpStatusCode.OK) {
                return@withContext "Error: API returned ${response.status}"
            }

            // handle the response
            if (responseText.isNotEmpty()) {

                val jsonResponse = JSONArray(responseText)
                val summaryText = jsonResponse.optJSONObject(0)?.optString("summary_text")

                // check if summaryText is valid and return it
                if (!summaryText.isNullOrEmpty() && !summaryText.contains("CNN", ignoreCase = true)
                ) {

                    // simulate error
                    //return@withContext "Error: ..."

                    return@withContext summaryText

                } else {

                    // Loading message due to weird Hugging Face behaviour

                    if (summaryText.isNullOrEmpty()) {
                        return@withContext "No summary generated from Huggingface"
                    }
                    return@withContext "Invalid summary containing 'CNN'"
                }

            } else {
                return@withContext "Error: API request failed."
            }
        } catch (e: Exception) {
            Log.e("CondensedNewsApi", "Error summarizing text: ${e.message}", e)
            return@withContext "Error: Unable to summarize text."
        }
    }
}
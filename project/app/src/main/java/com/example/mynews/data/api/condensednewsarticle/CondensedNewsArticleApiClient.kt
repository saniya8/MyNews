package com.example.mynews.data.api.condensednewsarticle

import android.util.Log
import com.example.mynews.data.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import io.ktor.client.*
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class CondensedNewsArticleApiClient {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        //install(HttpRequestRetry) {
        //    retryOnServerErrors(maxRetries = 3)
        //    exponentialDelay()
        //}
        install(ContentEncoding) { // Enable gzip decompression
            gzip()
        }
        //install(DefaultRequest) {
        //    header(HttpHeaders.Accept, "application/json")
        //    header(HttpHeaders.UserAgent, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        //}
    }


    suspend fun getArticleText(url: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val apiUrl = "https://api.diffbot.com/v3/article?token=${Constant.DIFFBOT_API_KEY}&url=$url"

                // using ktor

                //  GET request using Ktor and fetch the response body as text
                val response: HttpResponse = client.get(apiUrl) {
                    headers {
                        append(HttpHeaders.Accept, "application/json")
                    }
                }

                Log.d("CondensedNewsApi", "Response Status: ${response.status}")
                val responseBody = response.bodyAsText()
                Log.d("CondensedNewsApi", "Raw Response Body: $responseBody")

                if (response.status != HttpStatusCode.OK) {
                    return@withContext "Error: API returned ${response.status}"
                }

                val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject
                Log.d("CondensedNewsApi", "Json response $jsonResponse")
                val htmlContent = jsonResponse["objects"]?.jsonArray?.get(0)?.jsonObject?.get("html")?.jsonPrimitive?.content

//             println("Diffbot Response: ${response}") // This line can be used to see what is being returned by Diffbot

                return@withContext htmlContent ?: "No article content found in Diffbot response"

            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext "Error: ${e.message}"
            }
        }

    }


    suspend fun summarizeText(text: String, wordLimit: Int): String = withContext(Dispatchers.IO) {
        val url = "https://router.huggingface.co/hf-inference/models/facebook/bart-large-cnn"


        if (text.startsWith("Error:")){
            return@withContext "Error: Unable to summarize text."
        }

        try {

            /*val requestBody = JSONObject().apply {
                put("inputs", text)
                put("parameters", JSONObject().apply {
                    put("max_length", wordLimit)
                    put("min_length", wordLimit / 2)
                    put("do_sample", false)
                })
            }*/

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
                    append(HttpHeaders.ContentType,"application/json" /*ContentType.Application.Json.toString()*/)
                }
                setBody(requestBody)  // Set the body of the request
            } // Get the response body as a String

            Log.d("CondensedNewsApi", "Hugging Face Status: ${response.status}")
            val responseText = response.bodyAsText()
            Log.d("CondensedNewsApi", "Hugging Face Raw Response: $responseText")

            if (response.status != HttpStatusCode.OK) {
                return@withContext "Error: API returned ${response.status}"
            }

            // handle the response
            if (responseText.isNotEmpty()) {
                //val jsonResponse = JSONArray(response)
                //val summaryText = jsonResponse.optJSONObject(0)?.optString("summary_text")

                val jsonResponse = Json.parseToJsonElement(responseText).jsonArray
                val summaryText = jsonResponse.getOrNull(0)?.jsonObject?.get("summary_text")?.jsonPrimitive?.content

                // check if summaryText is valid and return it
                if (!summaryText.isNullOrEmpty() && !summaryText.contains("CNN", ignoreCase = true)) {

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
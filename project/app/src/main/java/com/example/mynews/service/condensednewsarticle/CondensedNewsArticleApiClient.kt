package com.example.mynews.service.condensednewsarticle

import android.util.Log
import com.example.mynews.model.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.ktor.client.*
import io.ktor.client.engine.android.Android
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.serialization.kotlinx.json.*
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

                // using ktor

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

                // additional error-checking

               /* val title = jsonResponse.getJSONArray("objects")
                    .getJSONObject(0)
                    .optString("title", "Untitled") // Fallback to "Untitled" if missing

                val images = jsonResponse.getJSONArray("objects")
                    .getJSONObject(0)
                    .optJSONArray("images") ?: JSONArray()

                Log.d("CondensedNewsApi", "Fetched HTML content: $htmlContent")
                Log.d("CondensedNewsApi", "Title: $title")

                // check if html is too problematic
                if (isProblematicHtml(htmlContent, title, images)) {
                    Log.d("CondensedNewsApi", "Detected problematic HTML - returning error")
                    return@withContext "Error: Unable to extract meaningful article text"
                }*/

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

    private fun isProblematicHtml(html: String, title: String, images: JSONArray): Boolean {
        // extract meaningful text, exclude carousel/sidebars
        val plainText = html
            .replace(
                Regex("<ul.*?>.*?</ul>", RegexOption.DOT_MATCHES_ALL),
                " "
            ) // Remove carousel lists
            .replace(Regex("<h2.*?>.*?</h2>", RegexOption.DOT_MATCHES_ALL), " ") // Remove headings
            .replace(Regex("<[^>]+>"), " ") // Strip remaining tags
            .replace(Regex("\\s+"), " ")
            .trim()

        val problematicMarkers = listOf(
            "<li data-carousel-id",
            "<h2>Top Stories</h2>",
            "<h2>ABC News Live Presents</h2>"
        )
        val hasEarlyProblematic = problematicMarkers.any { marker ->
            val index = html.indexOf(marker)
            index >= 0 && index < 400 // Wider net for carousel
        }

        val isTooShort = plainText.length < 20

        val isVideoPageLike =
            (images.length() > 1 || html.contains("<figure>")) && plainText.length < 100

        val titleWords = title.split(" ").filter { it.length > 3 }.map { it.lowercase() }
        val textWords = plainText.lowercase()
        val titleMatch = titleWords.any { it in textWords }

        Log.d("CondensedNewsApi", "Plain text: '$plainText' (length: ${plainText.length})")
        Log.d("CondensedNewsApi", "Has early problematic: $hasEarlyProblematic")
        Log.d("CondensedNewsApi", "Title match: $titleMatch")
        Log.d("CondensedNewsApi", "Is too short: $isTooShort")
        Log.d("CondensedNewsApi", "Is video page like: $isVideoPageLike")

        return (hasEarlyProblematic && !titleMatch) || (isTooShort && isVideoPageLike)
    }
}
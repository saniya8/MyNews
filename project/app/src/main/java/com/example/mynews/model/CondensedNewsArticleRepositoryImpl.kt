package com.example.mynews.model

import android.util.Log
import com.example.mynews.service.condensednewsarticle.CondensedNewsArticleApiClient
import com.example.mynews.domain.repositories.CondensedNewsArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

// Implementation of the CondensedNewsArticleRepository interface in .com.example.mynews/domain/repositories


class CondensedNewsArticleRepositoryImpl @Inject constructor(
) : CondensedNewsArticleRepository {

    private val condensedNewsArticleApiClient = CondensedNewsArticleApiClient()

    override suspend fun getArticleText(url: String): String {
        return withContext(Dispatchers.IO) {
            try {

                Log.d("CondensedNewsApi", "Fetching article from URL: $url")

                val htmlContent = condensedNewsArticleApiClient.getArticleText(url)

                Log.d("CondensedNewsApi", "Fetched HTML content: $htmlContent")


                if (htmlContent.isNotEmpty()) {
                    val document = Jsoup.parse(htmlContent)
                    val text = document.text()


                    if (text.isNotEmpty()) {
                        Log.d("CondensedNewsApi", "Extracted text: $text")
                        return@withContext truncateText(text)

                    } else {
                        return@withContext "Failed to extract meaningful content from the article"
                    }
                } else {
                    return@withContext "No article content found in Diffbot response"
                }
            } catch (e: Exception) {
                Log.e("CondensedNewsApi", "Error fetching article: ${e.message}", e)
                e.printStackTrace()
                return@withContext "Error: ${e.message}"
            }
        }
    }

    override suspend fun summarizeText(text: String, wordLimit: Int): String = withContext(Dispatchers.IO) {

        if (text.startsWith("Error:")){
            return@withContext "Error: Unable to summarize text."
        }

        try {

            Log.d("CondensedNewsApi", "Summarizing text with word limit: $wordLimit")
            val summary = condensedNewsArticleApiClient.summarizeText(text, wordLimit)
            Log.d("CondensedNewsApi", "Summary result: $summary")

            return@withContext summary
        } catch (e: Exception) {
            Log.e("CondensedNewsApi", "Error summarizing text: ${e.message}", e)
            return@withContext "Error: Unable to summarize text."
        }
    }

    private fun truncateText(text: String, maxLength: Int = 1020): String {
        if (text.length <= maxLength) return text

        val truncated = text.substring(0, maxLength)
        val lastSpace = truncated.lastIndexOf(' ')

        return if (lastSpace > 0) truncated.substring(0, lastSpace) else truncated
    }

}






/*
// jason's version (no ktor)

class CondensedNewsArticleRepositoryImpl @Inject constructor(
) : CondensedNewsArticleRepository {

    override suspend fun getArticleText(url: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val apiUrl = "https://api.diffbot.com/v3/article?token=${Constant.DIFFBOT_API_KEY}&url=$url"
                val connection = URL(apiUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                val responseCode = connection.responseCode

                val response = StringBuilder()
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                } else {
                    val errorReader = BufferedReader(InputStreamReader(connection.errorStream))
                    var errorLine: String?
                    while (errorReader.readLine().also { errorLine = it } != null) {
                        response.append(errorLine)
                    }
                    errorReader.close()
                }
//            println("Diffbot Response: ${response}") // This line can be used to see what is being returned by Diffbot

                val jsonResponse = JSONObject(response.toString())
                val htmlContent = jsonResponse.getJSONArray("objects").getJSONObject(0).getString("html")

                if (htmlContent.isNotEmpty()) {
                    val document = Jsoup.parse(htmlContent)
                    val text = document.text()

                    if (text.isNotEmpty()) {
                        return@withContext truncateText(text)
                    } else {
                        return@withContext "Failed to extract meaningful content from the article."
                    }
                } else {
                    return@withContext "No article content found in Diffbot response"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext "Error: ${e.message}"
            }
        }
    }

    override suspend fun summarizeText(text: String, numWordsToSummarize: Int): String = withContext(Dispatchers.IO) {
        val url = "https://router.huggingface.co/hf-inference/models/facebook/bart-large-cnn"


        if (text.startsWith("Error:")){
            return@withContext "Error: Unable to summarize text."
        }

        try {
            val requestBody = JSONObject().apply {
                put("inputs", text)
                put("parameters", JSONObject().apply {
                    put("max_length", numWordsToSummarize)
                    put("min_length", numWordsToSummarize / 2)
                    put("do_sample", false)
                })
            }

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer ${Constant.HUGGINGFACE_API_KEY}")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            connection.outputStream.use { it.write(requestBody.toString().toByteArray()) }

            val responseCode = connection.responseCode
            val inputStream = if (responseCode >= 400) {
                connection.errorStream
            } else {
                connection.inputStream
            }
            val responseText = inputStream.bufferedReader().use { it.readText() }
//            println("Hugging Face Response: $responseText")


            if (responseCode == 200) {
                val jsonResponse = JSONArray(responseText)
                val summaryText = jsonResponse.optJSONObject(0)?.optString("summary_text")

                if (summaryText != null && summaryText.isNotEmpty() && !summaryText.contains("CNN", ignoreCase = true)) {
                    // simulate error
                    //return@withContext "Error: ..."

                    return@withContext summaryText

                } else {
                    // Loading message due to weird Hugging Face behaviour
                    return@withContext "Loading..."
                }
            } else {
                return@withContext "Error: API request failed."
            }
        } catch (e: Exception) {
            return@withContext "Error: Unable to summarize text."
        }
    }

    fun truncateText(text: String, maxLength: Int = 1020): String {
        if (text.length <= maxLength) return text

        val truncated = text.substring(0, maxLength)
        val lastSpace = truncated.lastIndexOf(' ')

        return if (lastSpace > 0) truncated.substring(0, lastSpace) else truncated
    }

}

 */



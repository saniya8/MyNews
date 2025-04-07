package com.example.mynews.service.repositories.home

import com.example.mynews.domain.repositories.home.CondensedNewsArticleRepository
import com.example.mynews.service.condensednewsarticle.CondensedNewsArticleApiClient
import com.example.mynews.utils.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

class CondensedNewsArticleRepositoryImpl @Inject constructor(
    private val condensedNewsArticleApiClient: CondensedNewsArticleApiClient,
    private val logger: Logger,
) : CondensedNewsArticleRepository {

    //private val condensedNewsArticleApiClient = CondensedNewsArticleApiClient()

    override suspend fun getArticleText(url: String): String {
        return withContext(Dispatchers.IO) {
            try {

                logger.d("CondensedNewsApi", "Fetching article from URL: $url")

                val htmlContent = condensedNewsArticleApiClient.getArticleText(url)

                logger.d("CondensedNewsApi", "Fetched HTML content: $htmlContent")

                if (htmlContent.isNotEmpty()) {
                    val document = Jsoup.parse(htmlContent)
                    val text = document.text()

                    if (text.isNotEmpty()) {
                        logger.d("CondensedNewsApi", "Extracted text: $text")
                        return@withContext truncateText(text)

                    } else {
                        return@withContext "Failed to extract meaningful content from the article"
                    }
                } else {
                    return@withContext "No article content found in Diffbot response"
                }
            } catch (e: Exception) {
                logger.e("CondensedNewsApi", "Error fetching article: ${e.message}", e)
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

            logger.d("CondensedNewsApi", "Summarizing text with word limit: $wordLimit")
            val summary = condensedNewsArticleApiClient.summarizeText(text, wordLimit)
            logger.d("CondensedNewsApi", "Summary result: $summary")

            return@withContext summary
        } catch (e: Exception) {
            logger.e("CondensedNewsApi", "Error summarizing text: ${e.message}", e)
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



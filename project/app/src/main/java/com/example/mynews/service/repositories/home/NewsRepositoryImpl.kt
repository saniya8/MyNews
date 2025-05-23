package com.example.mynews.service.repositories.home

import android.content.Context
import com.example.mynews.domain.entities.NewsResponse
import com.example.mynews.domain.repositories.home.NewsRepository
import com.example.mynews.service.news.NewsApiClient
import com.example.mynews.service.newsbias.NewsBiasProvider
import com.example.mynews.service.repositories.Constant
import com.example.mynews.utils.logger.Logger
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val context: Context,
    private val newsApiClient: NewsApiClient,
    private val newsBiasProvider: NewsBiasProvider,
    private val logger: Logger,
) : NewsRepository {

    //private val newsBiasProvider = NewsBiasProvider(context)
    //private val newsApiClient = NewsApiClient()

    private val language = "en"

    override suspend fun getTopHeadlines(): NewsResponse {
        return newsApiClient.getTopHeadlines(
            language = language,
            apiKey = Constant.NEWS_API_KEY
        )
    }

    override suspend fun getEverythingBySearch(searchQuery: String): NewsResponse {
        return newsApiClient.getEverythingBySearch(
            language = language,
            query = searchQuery,
            apiKey = Constant.NEWS_API_KEY
        )
    }

    override suspend fun getTopHeadlinesByCategory(category: String): NewsResponse {
        return newsApiClient.getTopHeadlinesByCategory(
            language = language,
            category = category,
            apiKey = Constant.NEWS_API_KEY
        )
    }

    override suspend fun getTopHeadlinesByCountry(country: String): NewsResponse {

        // step 1: fetch sources for the given country
        val sourcesResponse = newsApiClient.getSourcesByCountry(
            apiKey = Constant.NEWS_API_KEY,
            country = country,
            language = language
        )

        // step 2: check if the response is successful and contains sources
        if (sourcesResponse.status != "ok" || sourcesResponse.sources.isEmpty()) {
            return NewsResponse(
                status = "error",
                articles = emptyList(),
                totalResults = 0
            )
        }

        // step 3: extract ids of each source and join them into a comma-separated string
        val sourceIds = sourcesResponse.sources
            .map { it.id }
            .filterNot { it.contains("google-news") }
            .joinToString(",")

        logger.d("CountryDebug", "Country $country has ids: $sourceIds" )

        // step 4: fetch top headlines using the source IDs
        return newsApiClient.getTopHeadlinesBySources(
            apiKey = Constant.NEWS_API_KEY,
            sources = sourceIds,
            language = language
        )
    }

    override suspend fun getEverythingByDateRange(dateRange: String): NewsResponse {

        logger.d("CountryDebug", "In NewsRepositoryImpl's getEverythingByDateRange")

        // step 1: calculate the "from" date based on the selected range
        val today = LocalDate.now()
        val latestAvailableDate = today.minusDays(1) // due to free api plan, account for 24-hour delay

        logger.d("CountryDebug", "Today is $today")
        logger.d("CountryDebug", "Yesterday is $latestAvailableDate")

        val daysToSubtract = when (dateRange) {
            "Last 7 Days" -> 7
            "Last 14 Days" -> 14
            "Last 30 Days" -> 30
            else -> 30 // Default to 30 days
        }

        val fromDate = latestAvailableDate.minusDays(daysToSubtract.toLong())
        val from = fromDate.toString() // ISO 8601 format (e.g., "2025-03-26")

        logger.d("CountryDebug", "From is $from")

        return newsApiClient.getEverythingByDateRange(
            apiKey = Constant.NEWS_API_KEY,
            from = from,
            language = language
        )
    }

    override suspend fun startFetchingBiasData() {
        newsBiasProvider.startFetchingBiasData()
    }

    override fun getAllBiasMappings() : StateFlow<Map<String, String>> {
        return newsBiasProvider.getAllBiasMappings()
    }


    override suspend fun getBiasForSource(sourceName: String) : String {
        return newsBiasProvider.getBiasForSource(sourceName)
    }

}


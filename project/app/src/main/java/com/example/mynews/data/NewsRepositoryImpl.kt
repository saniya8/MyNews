package com.example.mynews.data

import com.example.mynews.domain.repositories.NewsRepository
import com.example.mynews.data.api.news.NewsResponse
import javax.inject.Inject
import android.content.Context
import android.util.Log
import com.example.mynews.data.api.news.NewsApiClient
import com.example.mynews.data.newsbias.NewsBiasProvider
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

// Implementation of the NewsRepository interface in .com.example.mynews/domain/repositories


class NewsRepositoryImpl @Inject constructor(
    private val context: Context
) : NewsRepository {

    private val newsBiasProvider = NewsBiasProvider(context)

    //private val newsApi = NewsRetrofitInstance.newsApi
    private val newsApiClient = NewsApiClient()

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

        Log.d("CountryDebug", "Country $country has ids: $sourceIds" )
        // step 4: fetch top headlines using the source IDs
        return newsApiClient.getTopHeadlinesBySources(
            apiKey = Constant.NEWS_API_KEY,
            sources = sourceIds,
            language = language
        )
    }

    override suspend fun getEverythingByDateRange(dateRange: String): NewsResponse {

        Log.d("CountryDebug", "In NewsRepositoryImpl's getEverythingByDateRange")
        // step 1: calculate the "from" date based on the selected range
        val today = LocalDate.now()
        val latestAvailableDate = today.minusDays(1) // due to free api plan, account for 24-hour delay

        Log.d("CountryDebug", "Today is $today")
        Log.d("CountryDebug", "Yesterday is $latestAvailableDate")

        val daysToSubtract = when (dateRange) {
            "Last 7 Days" -> 7
            "Last 14 Days" -> 14
            "Last 30 Days" -> 30
            else -> 30 // Default to 30 days
        }

        val fromDate = latestAvailableDate.minusDays(daysToSubtract.toLong())
        val from = fromDate.toString() // ISO 8601 format (e.g., "2025-03-26")

        Log.d("CountryDebug", "From is $from")

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


    /*
    // TESTING: filtering by country
    override suspend fun testCountriesForSources() {
        try {
            val sourcesResponse = newsApiClient.getAllSources(
                apiKey = Constant.NEWS_API_KEY,
                language = "en"
            )

            if (sourcesResponse.status != "ok") {
                Log.e("CountrySources", "Failed to fetch sources: ${sourcesResponse.status}")
                return
            }

            // Group sources by country and filter out "google-news"
            val sourcesByCountry = sourcesResponse.sources
                .filterNot { it.id.contains("google-news") } // Filter out "google-news" sources
                .groupBy { it.country } // Group by country code

            // Log countries with non-empty source lists
            sourcesByCountry.forEach { (country, sources) ->
                if (sources.isNotEmpty()) {
                    Log.i("CountrySources", "Country: $country, Sources: ${sources.map { it.id }}")
                }
            }

            // Log countries with no sources after filtering
            val allCountries = listOf(
                "ae", "ar", "at", "au", "be", "bg", "br", "ca", "ch", "cn", "co", "cu", "cz", "de", "eg", "fr",
                "gb", "gr", "hk", "hu", "id", "ie", "il", "in", "it", "jp", "kr", "lt", "lv", "ma", "mx", "my",
                "ng", "nl", "no", "nz", "ph", "pl", "pt", "ro", "rs", "ru", "sa", "se", "sg", "si", "sk", "th",
                "tr", "tw", "ua", "us", "ve", "za"
            )
            val countriesWithSources = sourcesByCountry.keys
            val countriesWithoutSources = allCountries.filterNot { it in countriesWithSources }
            Log.i("CountrySources", "Countries with no sources after filtering: $countriesWithoutSources")
        } catch (e: Exception) {
            Log.e("CountrySources", "Error fetching sources: ${e.message}", e)
        }
    }

     */


}


package com.example.mynews.domain.repositories

import com.example.mynews.data.api.news.NewsResponse
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response

interface NewsRepository {
    suspend fun getTopHeadlines(): NewsResponse
    suspend fun getEverythingBySearch(searchQuery: String): NewsResponse
    suspend fun getTopHeadlinesByCategory(category: String): NewsResponse
    suspend fun getTopHeadlinesByCountry(country: String): NewsResponse
    suspend fun startFetchingBiasData()
    fun getAllBiasMappings() : StateFlow<Map<String, String>>
    suspend fun getBiasForSource(sourceName: String) : String
    //suspend fun testCountriesForSources() // TESTING: filtering by country
}
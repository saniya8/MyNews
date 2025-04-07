package com.example.mynews.domain.repositories.home

import com.example.mynews.domain.entities.NewsResponse
import kotlinx.coroutines.flow.StateFlow

interface NewsRepository {
    suspend fun getTopHeadlines(): NewsResponse
    suspend fun getEverythingBySearch(searchQuery: String): NewsResponse
    suspend fun getTopHeadlinesByCategory(category: String): NewsResponse
    suspend fun getTopHeadlinesByCountry(country: String): NewsResponse
    suspend fun getEverythingByDateRange(dateRange: String): NewsResponse
    suspend fun startFetchingBiasData()
    fun getAllBiasMappings() : StateFlow<Map<String, String>>
    suspend fun getBiasForSource(sourceName: String) : String
}
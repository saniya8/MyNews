package com.example.mynews.domain.repositories

import com.example.mynews.data.api.news.NewsResponse
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response

interface NewsRepository {
    suspend fun getTopHeadlines(): Response<NewsResponse>
    suspend fun getTopHeadlinesByCategory(category: String): Response<NewsResponse>
    suspend fun getEverythingBySearch(searchQuery: String): Response<NewsResponse>
    suspend fun startFetchingBiasData()
    fun getAllBiasMappings() : StateFlow<Map<String, String>>
    suspend fun getBiasForSource(sourceName: String) : String
}
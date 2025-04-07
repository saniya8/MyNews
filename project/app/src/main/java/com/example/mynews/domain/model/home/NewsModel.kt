package com.example.mynews.domain.model.home

import androidx.lifecycle.LiveData
import com.example.mynews.domain.entities.Article

interface NewsModel {
    val articles: LiveData<List<Article>>
    suspend fun fetchTopHeadlines()
    suspend fun fetchEverythingBySearch(searchQuery: String)
    suspend fun fetchTopHeadlinesByCategory(category: String)
    suspend fun fetchTopHeadlinesByCountry(country: String)
    suspend fun fetchEverythingByDateRange(dateRange: String)
    fun fetchBiasForSource(sourceName: String, onResult: (String) -> Unit)
    suspend fun startFetchingBiasData()
    fun getAllBiasMappings()
}
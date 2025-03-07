package com.example.mynews.domain.repositories

import com.example.mynews.data.api.NewsResponse
import retrofit2.Response

interface NewsRepository {
    suspend fun getTopHeadlines(): Response<NewsResponse>
    suspend fun getTopHeadlinesByCategory(category: String): Response<NewsResponse>
    suspend fun getEverythingBySearch(searchQuery: String): Response<NewsResponse>
}
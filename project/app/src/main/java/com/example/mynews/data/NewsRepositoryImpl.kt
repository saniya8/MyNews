package com.example.mynews.data


import com.example.mynews.domain.repositories.NewsRepository
import com.example.mynews.data.api.NewsResponse
import com.example.mynews.data.api.RetrofitInstance
import retrofit2.Response
import javax.inject.Inject

// Implementation of the NewsRepository interface in .com.example.mynews/domain/repositories


class NewsRepositoryImpl @Inject constructor() : NewsRepository {

    private val newsApi = RetrofitInstance.newsApi
    private val language = "en"

    override suspend fun getTopHeadlines(): Response<NewsResponse> {
        return newsApi.getTopHeadlines(
            language = language,
            apiKey = Constant.apiKey
        )
    }

    override suspend fun getTopHeadlinesByCategory(category: String): Response<NewsResponse> {
        return newsApi.getTopHeadlinesByCategory(
            language = language,
            category = category,
            apiKey = Constant.apiKey
        )
    }

    override suspend fun getEverythingBySearch(searchQuery: String): Response<NewsResponse> {
        return newsApi.getEverythingBySearch(
            language = language,
            q = searchQuery,
            apiKey = Constant.apiKey
        )
    }

}
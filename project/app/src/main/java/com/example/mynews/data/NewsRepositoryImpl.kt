package com.example.mynews.data

import com.example.mynews.domain.repositories.NewsRepository
import com.example.mynews.data.api.news.NewsResponse
import javax.inject.Inject
import android.content.Context
import com.example.mynews.data.api.news.NewsApiClient
import com.example.mynews.data.newsbias.NewsBiasProvider
import kotlinx.coroutines.flow.StateFlow

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

    override suspend fun getTopHeadlinesByCategory(category: String): NewsResponse {
        return newsApiClient.getTopHeadlinesByCategory(
            language = language,
            category = category,
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


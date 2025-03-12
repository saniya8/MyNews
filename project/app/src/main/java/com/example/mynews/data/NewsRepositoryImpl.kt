package com.example.mynews.data

import com.example.mynews.domain.repositories.NewsRepository
import com.example.mynews.data.api.news.NewsResponse
import com.example.mynews.data.api.news.NewsRetrofitInstance
import retrofit2.Response
import javax.inject.Inject
import android.content.Context
import com.example.mynews.data.newsbias.NewsBiasProvider
import kotlinx.coroutines.flow.StateFlow

// Implementation of the NewsRepository interface in .com.example.mynews/domain/repositories


class NewsRepositoryImpl @Inject constructor(
    private val context: Context
) : NewsRepository {

    private val newsBiasProvider = NewsBiasProvider(context)

    private val newsApi = NewsRetrofitInstance.newsApi
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


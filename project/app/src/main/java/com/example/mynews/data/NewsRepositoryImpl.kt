package com.example.mynews.data

import com.example.mynews.domain.repositories.NewsRepository
import com.example.mynews.data.api.NewsResponse
import com.example.mynews.data.api.RetrofitInstance
import retrofit2.Response
import javax.inject.Inject
import android.content.Context
import com.example.mynews.data.newsbias.NewsBiasProvider

// Implementation of the NewsRepository interface in .com.example.mynews/domain/repositories


class NewsRepositoryImpl @Inject constructor(
    private val context: Context
) : NewsRepository {

    private val newsBiasProvider = NewsBiasProvider(context)

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

    override fun getAllBiasMappings() : Map<String, String> {
        return newsBiasProvider.getAllBiasMappings()
    }

    // don't need this per newsviewmodel's fetchBiasForSource so don't need
    // newsBiasProvider.getBiasForSource(sourceName) either but keeping in case
    override fun getBiasForSource(sourceName: String) : String {
        return newsBiasProvider.getBiasForSource(sourceName)
    }

}


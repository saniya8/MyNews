package com.example.mynews.model.home


import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mynews.domain.entities.Article
import com.example.mynews.domain.entities.NewsResponse
import com.example.mynews.domain.model.home.NewsModel
import com.example.mynews.domain.repositories.home.NewsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class NewsModelImpl @Inject constructor(
    private val newsRepository: NewsRepository
) : NewsModel {

    private val _articles = MutableLiveData<List<Article>>()
    override val articles: LiveData<List<Article>> = _articles

    @VisibleForTesting
    internal val _newsBiasMappings = MutableStateFlow<Map<String, String>>(emptyMap())

    override suspend fun startFetchingBiasData() {
        newsRepository.startFetchingBiasData()
    }

    override fun getAllBiasMappings() {
        CoroutineScope(Dispatchers.IO).launch {
            newsRepository.getAllBiasMappings().collect { biasData ->
                _newsBiasMappings.value = biasData
            }
        }
    }

    override suspend fun fetchTopHeadlines() {
        try {
            val response = newsRepository.getTopHeadlines()
            handleNewsResponse(response)
        } catch (e: Exception) {
            Log.e("NewsModel", "Failed to fetch top headlines", e)
            _articles.postValue(emptyList())
        }
    }

    override suspend fun fetchEverythingBySearch(searchQuery: String) {
        try {
            val response = newsRepository.getEverythingBySearch(searchQuery)
            handleNewsResponse(response)
        } catch (e: Exception) {
            Log.e("NewsModel", "Failed to fetch everything by search", e)
            _articles.postValue(emptyList())
        }
    }

    override suspend fun fetchTopHeadlinesByCategory(category: String) {
        try {
            val response = newsRepository.getTopHeadlinesByCategory(category)
            handleNewsResponse(response)
        } catch (e: Exception) {
            Log.e("NewsModel", "Failed to fetch by category", e)
            _articles.postValue(emptyList())
        }
    }

    override suspend fun fetchTopHeadlinesByCountry(country: String) {
        try {
            val response = newsRepository.getTopHeadlinesByCountry(country)
            handleNewsResponse(response)
        } catch (e: Exception) {
            Log.e("NewsModel", "Failed to fetch by country", e)
            _articles.postValue(emptyList())
        }
    }

    override suspend fun fetchEverythingByDateRange(dateRange: String) {
        try {
            val response = newsRepository.getEverythingByDateRange(dateRange)
            handleNewsResponse(response)
        } catch (e: Exception) {
            Log.e("NewsModel", "Failed to fetch by date range", e)
            _articles.postValue(emptyList())
        }
    }

    override fun fetchBiasForSource(sourceName: String, onResult: (String) -> Unit) {

        // first check cached mappings
        _newsBiasMappings.value[sourceName]?.let {
            onResult(it)
            return
        }

        // otherwise fetch from repository asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            val bias = newsRepository.getBiasForSource(sourceName)
            onResult(bias)
        }
    }


    // helper function to handle the NewsResponse and post results
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun handleNewsResponse(
        newsResponse: NewsResponse,
    ) {
        if (newsResponse.status == "ok" && newsResponse.articles.isNotEmpty()) {
            _articles.postValue(newsResponse.articles)
        } else {
            _articles.postValue(emptyList())
        }
    }

}
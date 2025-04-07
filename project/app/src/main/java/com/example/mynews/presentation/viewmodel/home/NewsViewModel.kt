package com.example.mynews.presentation.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.domain.entities.Article
import com.example.mynews.domain.model.home.NewsModel
import com.example.mynews.utils.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsModel: NewsModel,
    private val logger: Logger,
) : ViewModel() {

    val articles: LiveData<List<Article>> = newsModel.articles

    private var hasFetchedNews = false // tracks if API call was made

    private val _isFiltering = MutableStateFlow(false)
    val isFiltering: StateFlow<Boolean> = _isFiltering

    init {
        // load the bias mappings
        viewModelScope.launch {
            val fetchJob = launch { newsModel.startFetchingBiasData() }
            fetchJob.join() // wait for fetching to complete before collecting below
            newsModel.getAllBiasMappings()
        }
    }

    // for initial news display
    fun fetchTopHeadlines(forceFetch: Boolean = false) {

        if (hasFetchedNews && !forceFetch) return // prevents duplicate API requests
        hasFetchedNews = true

        viewModelScope.launch {
            try {
                _isFiltering.value = false
                newsModel.fetchTopHeadlines()

            } catch (e: Exception) {
                _isFiltering.value = false
                logger.e("NewsAPI Error", "Failed to fetch top headlines: ${e.message}", e)
            }
        }
    }

    // for searching
    fun fetchEverythingBySearch(searchQuery: String) {

        viewModelScope.launch {
            try {
                _isFiltering.value = true
                newsModel.fetchEverythingBySearch(searchQuery)
            } catch (e: Exception) {
                _isFiltering.value = false
                logger.e("NewsAPI Error", "Failed to fetch search results: ${e.message}", e)
            }
        }

    }


    // for filtering by category
    fun fetchTopHeadlinesByCategory(category: String) {

        viewModelScope.launch {

            try {
                _isFiltering.value = true
                newsModel.fetchTopHeadlinesByCategory(category)
            } catch (e: Exception) {
                _isFiltering.value = false
                logger.e("NewsAPI Error", "Failed to fetch headlines by category: ${e.message}", e)
            }
        }
    }


    fun fetchTopHeadlinesByCountry(country: String) {

        viewModelScope.launch {

            try {
                _isFiltering.value = true
                newsModel.fetchTopHeadlinesByCountry(country)
            } catch (e: Exception) {
                _isFiltering.value = false
                logger.e("NewsAPI Error", "Failed to fetch headlines by country: ${e.message}", e)
            }
        }
    }

    fun fetchEverythingByDateRange(dateRange: String) {

        viewModelScope.launch {

            try {
                _isFiltering.value = true
                newsModel.fetchEverythingByDateRange(dateRange)
            } catch (e: Exception) {
                _isFiltering.value = false
                logger.e("NewsAPI Error", "Failed to fetch headlines by date range: ${e.message}", e)

            }
        }


    }

    fun fetchBiasForSource(sourceName: String, onResult: (String) -> Unit) {
        newsModel.fetchBiasForSource(sourceName, onResult)
    }

}
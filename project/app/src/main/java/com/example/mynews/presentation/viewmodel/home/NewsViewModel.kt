package com.example.mynews.presentation.viewmodel.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.data.api.news.Article
import com.example.mynews.data.api.news.NewsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.mynews.domain.repositories.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _articles = MutableLiveData<List<Article>>()

    // Expose the private _articles as articles to the UI so we can observe
    // articles live data in the UI
    val articles: LiveData<List<Article>> = _articles


    private val _newsBiasMappings = MutableStateFlow<Map<String, String>>(emptyMap())
    val newsBiasMappings: StateFlow<Map<String, String>> = _newsBiasMappings

    private var hasFetchedNews = false // tracks if API call was made

    // Based on: hasFetchedNews usage and LaunchedEffect in MainScreen,
    // fetchNewsTopHeadlines will trigger (ie do a new request) if:
    // - Logging in
    // - Force close app, swipe it out of memory, and then relaunch
    // fetchNewsTopHeadlines will not trigger (ie not do a new request) if:
    // - Swiping between tabs like Home, Social, Goals, and Settings
    // - Close app but do not swipe to clear it from memory

    private val _isFiltering = MutableStateFlow(false)
    val isFiltering: StateFlow<Boolean> = _isFiltering

    init {
        // load the bias mappings
        viewModelScope.launch {
            val fetchJob = launch { newsRepository.startFetchingBiasData() }
            fetchJob.join() // wait for fetching to complete before collecting below
            newsRepository.getAllBiasMappings().collect { biasData ->
                _newsBiasMappings.value = biasData // update when data changes
            }
        }
        //Log.d("NewsBiasDebug", "NewsViewMode's _newsBiasMappings is: ${newsBiasMappings.value}")
    }

    // Helper function to handle the NewsResponse and post results
    private fun handleNewsResponse(
        newsResponse: NewsResponse,
    ) {
        if (newsResponse.status == "ok" && newsResponse.articles.isNotEmpty()) {
            _articles.postValue(newsResponse.articles)
        } else {
            _articles.postValue(emptyList())
        }
    }


    // for initial news display
    fun fetchTopHeadlines(forceFetch: Boolean = false) {

        if (hasFetchedNews && !forceFetch) return // Prevents duplicate API requests
        hasFetchedNews = true

        viewModelScope.launch {
            // getTopHeadlines is a suspend function, therefore will take time
            // to load, therefore wrap it in a coroutine using viewModelScope.launch
            try {
                _isFiltering.value = false
                val newsResponse = newsRepository.getTopHeadlines() // ktor gives parsed data directly
                handleNewsResponse(newsResponse) // this will post to _articles
                //_articles.postValue(newsResponse.articles)

            } catch (e: Exception) {
                _articles.postValue(emptyList())
                _isFiltering.value = false
                Log.e("NewsAPI Error", "Failed to fetch top headlines: ${e.message}", e)
            }
        }
    }

    // for searching
    fun fetchEverythingBySearch(searchQuery: String) {

        viewModelScope.launch {
            try {
                _isFiltering.value = true
                val newsResponse = newsRepository.getEverythingBySearch(searchQuery)
                //_articles.postValue(newsResponse.articles)
                handleNewsResponse(newsResponse) // this will post to _articles
            } catch (e: Exception) {
                _articles.postValue(emptyList())
                _isFiltering.value = false
                Log.e("NewsAPI Error", "Failed to fetch search results: ${e.message}", e)
            }
        }

    }


    // for filtering by category
    fun fetchTopHeadlinesByCategory(category: String) {

        // if category is null, then requires fetching top headlines (since category
        // being null means category was deselected or never selected)
        // that is handled in LaunchedEffect(selectedCategory.value) in HomeScreen.kt
        // so if this function is called, category cannot be null


        viewModelScope.launch {

            try {
                _isFiltering.value = true
                val newsResponse = newsRepository.getTopHeadlinesByCategory(category)
                //_articles.postValue(newsResponse.articles)
                handleNewsResponse(newsResponse) // this will post to _articles
            } catch (e: Exception) {
                _articles.postValue(emptyList())
                _isFiltering.value = false
                Log.e("NewsAPI Error", "Failed to fetch headlines by category: ${e.message}", e)
            }
        }
    }


    fun fetchTopHeadlinesByCountry(country: String) {

        // if country is null, then requires fetching top headlines (since category
        // being null means category was deselected or never selected)
        // that is handled in LaunchedEffect(selectedCountry.value) in HomeScreen.kt
        // so if this function is called, category cannot be null

        viewModelScope.launch {

            try {
                _isFiltering.value = true
                val newsResponse = newsRepository.getTopHeadlinesByCountry(country)
                //_articles.postValue(newsResponse.articles)
                handleNewsResponse(newsResponse) // this will post to _articles
            } catch (e: Exception) {
                _articles.postValue(emptyList())
                _isFiltering.value = false
                Log.e("NewsAPI Error", "Failed to fetch headlines by country: ${e.message}", e)
            }
        }
    }

    fun fetchEverythingByDateRange(dateRange: String) {

        viewModelScope.launch {

            try {
                _isFiltering.value = true
                val newsResponse = newsRepository.getEverythingByDateRange(dateRange)
                //_articles.postValue(newsResponse.articles)
                handleNewsResponse(newsResponse) // this will post to _articles
            } catch (e: Exception) {
                _articles.postValue(emptyList())
                _isFiltering.value = false
                Log.e("NewsAPI Error", "Failed to fetch headlines by date range: ${e.message}", e)

            }
        }


    }


    fun fetchBiasForSource(sourceName: String, onResult: (String) -> Unit) {

        // first heck in cached bias mappings
        _newsBiasMappings.value[sourceName]?.let {
            onResult(it)
        }

        // Otherwise, fetch bias asynchronously
        viewModelScope.launch {
            val bias = newsRepository.getBiasForSource(sourceName)
            onResult(bias) // pass the result back
        }
    }

}
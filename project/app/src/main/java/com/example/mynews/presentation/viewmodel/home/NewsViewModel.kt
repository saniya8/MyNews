package com.example.mynews.presentation.viewmodel.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.data.api.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.mynews.domain.repositories.NewsRepository
import com.example.mynews.domain.repositories.UserRepository
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


    init {
        // load the bias mappings
        viewModelScope.launch {
            _newsBiasMappings.value = newsRepository.getAllBiasMappings()
        }
        //Log.d("NewsBiasDebug", "NewsViewMode's _newsBiasMappings is: ${newsBiasMappings.value}")
    }


    // for initial news display
    fun fetchTopHeadlines(forceFetch: Boolean = false) {

        if (hasFetchedNews && !forceFetch) return // Prevents duplicate API requests
        hasFetchedNews = true

        viewModelScope.launch {
            // getTopHeadlines is a suspend function, therefore will take time
            // to load, therefore wrap it in a coroutine using viewModelScope.launch
            val response = newsRepository.getTopHeadlines()

            // response represents entire HTTP response:
            // response.code() - status code
            // response.headers() - headers
            // response.errorBody() - error body
            // response.body() - body - the actual NewsResponse entity

            //Log.i("NewsAPI Response", "Response Code: ${response.code()}")

            if (response.isSuccessful) {

                //Log.i("NewsAPI Response: ", response.body().toString())

                val newsResponse = response.body()


                // Print the titles
                //newsResponse?.articles?.forEach { article ->
                //    Log.i("NewsAPI Response", "Title: ${article.title}")
                //}


                // Assign articles to the mutable live data _articles
                newsResponse?.articles?.let {
                    _articles.postValue(it)
                }


            } else {
                //response.errorBody()?.string()?.let { Log.i("NewsAPI Response Failed: ", it) }
                Log.i("NewsAPI Response Failure: ", response.message())
            }
        }
    }


    // for filtering
    fun fetchTopHeadlinesByCategory(category: String) {

        // if category is null, then requires fetching top headlines (since category
        // being null means category was deselected or never selected)
        // that is handled in LaunchedEffect(selectedCategory.value) in HomeScreen.kt
        // so if this function is called, category cannot be null


        viewModelScope.launch {
            // getTopHeadlinesByCategory is a suspend function, therefore will take time
            // to load, therefore wrap it in a coroutine using viewModelScope.launch

            /*

            // this was code when handling category == null
            // now handling that in HomeScreen.kt

            // value of response depends on the category value
            val response =
                if(category == null) {
                    newsApi.getTopHeadlines(language = language,
                                            apiKey = Constant.apiKey)
                } else { // category is not null
                    newsApi.getTopHeadlinesByCategory(language = language,
                                                      category = category,
                                                      apiKey = Constant.apiKey)
                }

             */


            val response = newsRepository.getTopHeadlinesByCategory(category)

            if (response.isSuccessful) {
                val newsResponse = response.body()
                // Assign articles to the mutable live data _articles
                newsResponse?.articles?.let {
                    _articles.postValue(it)
                }

            } else {
                Log.i("NewsAPI Response Failure By Category: ", response.message())
            }


        }

    }

    // for searching
    fun fetchEverythingBySearch(searchQuery: String) {


        viewModelScope.launch {

            // getEverythingBySearch is a suspend function, therefore will take time
            // to load, therefore wrap it in a coroutine using viewModelScope.launch

            val response = newsRepository.getEverythingBySearch(searchQuery)

            if (response.isSuccessful) {
                val newsResponse = response.body()
                // Assign articles to the mutable live data _articles
                newsResponse?.articles?.let {
                    _articles.postValue(it)
                }

            } else {
                Log.i("NewsAPI Response Failure By Category: ", response.message())
            }
        }

    }

    fun fetchBiasForSource(sourceName: String): String {
        //return newsRepository.getBiasForSource(sourceName) // we already preloaded the data into _newsBiasMappings in init so we can just get it from there
        //return _newsBiasMappings.value[sourceName] ?: "Neutral"

        // first check in cached bias mappings
        _newsBiasMappings.value[sourceName]?.let { return it }

        // otherwise call getBiasForSource which will do the fuzzy matching
        return newsRepository.getBiasForSource(sourceName)
    }

}
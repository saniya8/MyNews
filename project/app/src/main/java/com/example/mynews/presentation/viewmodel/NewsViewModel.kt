package com.example.mynews.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.data.Constant
import com.example.mynews.data.api.Article
import com.example.mynews.data.api.RetrofitInstance
import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.repositories.UserRepository
//import com.kwabenaberko.newsapilib.NewsApiClient
//import com.kwabenaberko.newsapilib.models.request.TopHeadlinesRequest
//import com.kwabenaberko.newsapilib.models.response.ArticleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(): ViewModel() {

    private val _articles = MutableLiveData<List<Article>>()
    // Expose the private _articles as articles to the UI so we can observe
    // articles live data in the UI
    val articles: LiveData<List<Article>> = _articles

    // use Retrofit here since NewsApiClient not working

    private var hasFetchedNews = false // tracks if API call was made
    private val newsApi = RetrofitInstance.newsApi

    // Based on: hasFetchedNews usage and LaunchedEffect in MainScreen,
    // fetchNewsTopHeadlines will trigger (ie do a new request) if:
    // - Logging in
    // - Force close app, swipe it out of memory, and then relaunch
    // fetchNewsTopHeadlines will not trigger (ie not do a new request) if:
    // - Swiping between tabs like Home, Navigation, Goals, and Settings
    // - Close app but do not swipe to clear it from memory


    fun fetchNewsTopHeadlines() {

        if (hasFetchedNews) return // Prevents duplicate API requests
        hasFetchedNews = true
        val language = "en"

        viewModelScope.launch {
            // getTopHeadlines is a suspend function, therefore will take time
            // to load, therefore wrap it in a coroutine using viewModelScope.launch
            val response = newsApi.getTopHeadlines(language, Constant.apiKey)

            // response represents entire HTTP response:
            // response.code() - status code
            // response.headers() - headers
            // response.errorBody() - error body
            // response.body() - body - the actual NewsResponse entity

            Log.i("NewsAPI Debug", "Response Code: ${response.code()}")

            if(response.isSuccessful) {

                //Log.i("NewsAPI Response: ", response.body().toString())

                val newsResponse = response.body()

                /*
                // Print the titles
                newsResponse?.articles?.forEach { article ->
                    Log.i("NewsAPI Response", "Title: ${article.title}")
                }

                 */

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

    /*

   // This calls fetchNewsTopHeadlines each time the NewsViewModel is initialized
   // Initially: had NewsViewModel initialized in HomeScreen parameters, so each
   // time HomeScreen was clicked on, NewsViewModel was instantiated again, which called
   // fetchNewsTopHeadlines again, which grew to too many API calls
   // Now: changed it so that NewsViewModel is initialized in the MainScreen parameter
   // and passed to HomeScreen passed to NewsScreen so technically the init below
   // should work now but already found another solution so leaving this commented out
   init { // whenever NewsViewModel initialized, it will call fetchNewsTopHeadlines
       fetchNewsTopHeadlines()
   }

    */



    /*
    fun fetchNewsTopHeadlines() {
        val newsAPIClient = NewsApiClient(Constant.APIKey)

        // Top Headline Request
        val topHeadlinesRequest = TopHeadlinesRequest.Builder().language("en").build()

        newsAPIClient.getTopHeadlines(topHeadlinesRequest, object : NewsApiClient.ArticlesResponseCallback {
            override fun onSuccess(response: ArticleResponse?) {
                response?.articles?.forEach{
                    Log.i("NewsAPI Response", it.title)
                }
            }

            override fun onFailure(throwable: Throwable?) {
                if (throwable != null) {
                    Log.i("NewsAPI Response Failed", "Error: ${throwable.message}")
                }
            }

        })
    }

     */




}
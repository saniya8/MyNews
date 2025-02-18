package com.example.mynews.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.data.Constant
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

    // use Retrofit here since NewsApiClient not working

    private val newsApi = RetrofitInstance.newsApi

    init { // whenever NewsViewModel initialized, it will call fetchNewsTopHeadlines
        fetchNewsTopHeadlines()
    }

    fun fetchNewsTopHeadlines() {
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
                // Print the titles
                newsResponse?.articles?.forEach { article ->
                    Log.i("NewsAPI Response", "Title: ${article.title}")
                }

            } else {
                //response.errorBody()?.string()?.let { Log.i("NewsAPI Response Failed: ", it) }
                Log.i("NewsAPI Response Failure: ", response.message())
            }
        }
    }



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
package com.example.mynews.data


import com.example.mynews.domain.repositories.NewsRepository
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.data.Constant
import com.example.mynews.data.api.Article
import com.example.mynews.data.api.NewsResponse
import com.example.mynews.data.api.RetrofitInstance
import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.repositories.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

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
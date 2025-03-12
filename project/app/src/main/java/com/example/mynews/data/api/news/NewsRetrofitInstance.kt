package com.example.mynews.data.api.news

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NewsRetrofitInstance {

    private const val BASE_URL = "https://newsapi.org/v2/";

    private fun getInstance() : Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Gson for Json parsing
            .build()
    }

    val newsApi : NewsApi = getInstance().create(NewsApi::class.java)


}
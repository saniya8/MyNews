package com.example.mynews.data.api.newsbias

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NewsBiasRetrofitInstance {

    private const val BASE_URL = "https://www.allsides.com/"

    private fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Gson for Json parsing
            .build()
    }

    val newsBiasApi: NewsBiasApi = getInstance().create(NewsBiasApi::class.java)
}
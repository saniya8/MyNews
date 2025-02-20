package com.example.mynews.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NewsApi {

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("language") language : String,
        @Header("X-Api-Key") apiKey : String,
        @Header("User-Agent") userAgent: String = "Mozilla/5.0" // DO NOT DELETE - to trick API that request is coming from browser
    ) : Response<NewsResponse>

    @GET("top-headlines")
    suspend fun getTopHeadlinesByCategory(
        @Query("language") language : String,
        @Query("category") category : String,
        @Header("X-Api-Key") apiKey : String,
        @Header("User-Agent") userAgent: String = "Mozilla/5.0" // DO NOT DELETE - to trick API that request is coming from browser
    ) : Response<NewsResponse>


}
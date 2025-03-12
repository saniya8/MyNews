package com.example.mynews.data.api.newsbias

import retrofit2.Response
import retrofit2.http.GET

interface NewsBiasApi {

    @GET("media-bias/json/noncommercial/publications")
    suspend fun getBiasRatings(): Response<NewsBiasResponse>

}
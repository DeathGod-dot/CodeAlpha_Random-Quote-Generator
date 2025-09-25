package com.example.yumelines.network

import com.example.yumelines.data.Quote
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface QuotesApiService {
    @GET("api/quotes")
    suspend fun getRandomQuote(@Query("random") random: Int = 1): Response<List<Quote>>
}

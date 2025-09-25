package com.example.yumelines.repository

import com.example.yumelines.data.Quote
import com.example.yumelines.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteRepository {
    private val apiService = RetrofitInstance.api
    
    suspend fun getRandomQuote(): Result<Quote> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getRandomQuote()
                val quoteList = response.body()
                if (response.isSuccessful && !quoteList.isNullOrEmpty()) {
                    Result.success(quoteList.first())
                } else {
                    Result.failure(Exception("Failed to fetch quote: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

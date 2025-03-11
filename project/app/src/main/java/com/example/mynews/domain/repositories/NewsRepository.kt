package com.example.mynews.domain.repositories

import com.example.mynews.data.api.Article
import com.example.mynews.data.api.NewsResponse
import retrofit2.Response

interface NewsRepository {
    suspend fun getTopHeadlines(): Response<NewsResponse>
    suspend fun getTopHeadlinesByCategory(category: String): Response<NewsResponse>
    suspend fun getEverythingBySearch(searchQuery: String): Response<NewsResponse>
    suspend fun getReaction(userID: String, article: Article) : String?
    suspend fun setReaction(userID: String, article: Article, reaction: String?)
    suspend fun trackReactions(userID: String, onReactionChanged: (Map<String, String?>) -> Unit)

}
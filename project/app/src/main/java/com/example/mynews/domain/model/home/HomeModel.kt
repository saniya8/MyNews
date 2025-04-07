package com.example.mynews.domain.model.home

import androidx.lifecycle.LiveData
import com.example.mynews.domain.entities.Article

interface HomeModel {
    val articleReactions: LiveData<Map<String, String?>>
    suspend fun getReaction(userID: String, article: Article) : String?
    suspend fun setReaction(userID: String, article: Article, reaction: String?)
    fun trackReactions(userID: String)

}
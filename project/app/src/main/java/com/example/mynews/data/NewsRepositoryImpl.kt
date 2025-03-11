package com.example.mynews.data


import android.net.Uri
import android.util.Log
import com.example.mynews.data.api.Article
import com.example.mynews.domain.repositories.NewsRepository
import com.example.mynews.data.api.NewsResponse
import com.example.mynews.data.api.RetrofitInstance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

// Implementation of the NewsRepository interface in .com.example.mynews/domain/repositories


class NewsRepositoryImpl @Inject constructor(
    private val firestore : FirebaseFirestore
) : NewsRepository {

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



    // getReaction: returns the reaction the user set for a particular article (try block), or null if
    // user never selected a reaction for the article or user deselected their reaction (catch block)
    override suspend fun getReaction(userID: String, article: Article): String? {

        val safeArticleURL = Uri.encode(article.url)

        return try {
            val reactionLocation = firestore.collection("reactions")
                .document(userID)
                .collection("users_reactions")
                .document(safeArticleURL)
                .get()
                .await()

            reactionLocation.getString("reaction")

        } catch (e: Exception) {
            Log.e(
                "GetReaction",
                "Error getting reaction for article ${article.url}: ${e.message}",
                e
            )
            return null
        }

    }

    override suspend fun setReaction(userID: String, article: Article, reaction: String?) {

        try {
            val safeArticleURL = Uri.encode(article.url)

            val reactionLocation = firestore.collection("reactions")
                .document(userID)
                .collection("users_reactions")
                .document(safeArticleURL)

            if (reaction == null) { // user deselected reaction
                reactionLocation.delete().await()
                Log.d(
                    "SetReaction",
                    "Removed reaction from firestore for article: ${article.title}"
                )

            } else { // reaction is one of the emojis in the reactions bar

                val reactionData = mapOf(
                    "article" to articleToMap(article),
                    "reaction" to reaction,
                    "timestamp" to System.currentTimeMillis(),
                )

                reactionLocation.set(reactionData).await()
                Log.d(
                    "SetReaction",
                    "Updated reaction for article: ${article.title} and set reaction to: ${reaction}"
                )

            }

        } catch (e: Exception) {
            Log.e(
                "SetReaction",
                "Error setting reaction for article ${article.url}: ${e.message}",
                e
            )
        }

    }


    private fun articleToMap(article: Article): Map<String, Any?> {
        return mapOf(
            "author" to article.author,
            "content" to article.content,
            "description" to article.description,
            "publishedAt" to article.publishedAt,
            "source" to mapOf(
                "id" to article.source.id,
                "name" to article.source.name
            ),
            "title" to article.title,
            "url" to article.url,
            "urlToImage" to article.urlToImage
        )
    }

    override suspend fun trackReactions(
        userID: String,
        onReactionChanged: (Map<String, String?>) -> Unit
    ) {
        firestore.collection("reactions")
            .document(userID)
            .collection("users_reactions")
            .addSnapshotListener { snapshot, error ->
                Log.d("ReactionDebug", "Snapshot listener triggered for userID: $userID")
                if (error != null) {
                    Log.e("ReactionDebug", "Error tracking reactions: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("ReactionDebug", "Snapshot received: ${snapshot.documents.size} documents")
                    // map article ID (safeArticleURL) → reaction
                    val reactionMap = snapshot.documents.associate { document ->
                        val articleID = document.id
                        val reaction = document.getString("reaction")
                        articleID to reaction
                    }

                    Log.d("ReactionDebug", "Real-time reactions updated: $reactionMap")

                    // pass to viewmodel
                    onReactionChanged(reactionMap)
                }
            }
    }







}


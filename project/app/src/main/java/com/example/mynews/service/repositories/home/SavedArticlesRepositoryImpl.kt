package com.example.mynews.service.repositories.home

import android.net.Uri
import android.util.Log
import com.example.mynews.domain.entities.Article
import com.example.mynews.domain.entities.Source
import com.example.mynews.domain.repositories.home.SavedArticlesRepository
import com.example.mynews.utils.articleToMap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SavedArticlesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SavedArticlesRepository {

    override suspend fun saveArticle(userID: String, article: Article): Boolean {

        // navigate to where article should be stored in firestore

        // firestore does not allow slashes in document IDs so need to encode URL
        val safeArticleURL = Uri.encode(article.url)

        val articleLocation = firestore.collection("saved_articles")
            .document(userID)
            .collection("articles")
            .document(safeArticleURL) // use URL as unique document identifier to avoid saving duplicate articles


        // In Firestore...
        // By using article.url as the document ID, an article can only be saved once per used
        // If same article is saved again, firestore overwrites the existing document which is fine
        // since this prevents duplicates


        try {

            // Though Firestore prevents duplicate documents since article.url is document ID,
            // prevent unnecessary Firestore overwrites if the article that the user saved already
            // exists in firestore (was already saved)

            val existingSavedArticle = articleLocation.get().await()

            // article user is trying to save already exists in firestore since it was
            // saved previously
            if (existingSavedArticle.exists()) {
                Log.d("SavedArticlesRepositoryImpl", "Already saved article: ${article.title}")
                return false
            }

            // at this point, the article the user is saving hasn't been saved before

            val savedArticleData = mapOf(
                "article" to articleToMap(article),
                "timestamp" to System.currentTimeMillis()
            )
            articleLocation.set(savedArticleData).await()


            Log.d("SavedArticlesRepositoryImpl", "Article saved successfully: ${article.title}")
            return true

        } catch (e: Exception) {
            Log.e("SavedArticlesRepositoryImpl", "Error saving article: ", e)
            return false
        }

    }



    override suspend fun deleteSavedArticle(userID: String, article: Article): Boolean {

        // navigate to where article should be stored in firestore
        // Firestore does not allow slashes in document IDs so need to encode URL
        val safeArticleURL = Uri.encode(article.url)

        val articleLocation = firestore.collection("saved_articles")
            .document(userID)
            .collection("articles")
            .document(safeArticleURL)


        try {

            val existingSavedArticle = articleLocation.get().await()

            if (!existingSavedArticle.exists()) {
                Log.d("SavedArticlesRepositoryImpl", "Article not found, nothing to delete")
                return false
            }

            articleLocation.delete().await() // delete article
            Log.d("SavedArticlesRepositoryImpl", "Article deleted successfully: ${article.title}")
            return true
        } catch (e: Exception) {
            Log.e("SavedArticlesRepositoryImpl", "Error deleting article: ", e)
            return false
        }


    }

    override fun getSavedArticles(userID: String, onResult: (List<Article>) -> Unit) {

        firestore.collection("saved_articles")
            .document(userID)
            .collection("articles")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            // add event listener
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SavedArticlesViewModel", "Error fetching user's saved articles: ", error)
                    onResult(emptyList()) // return empty list
                    return@addSnapshotListener
                }

                // firestore returned data
                if (snapshot != null) {
                    // snapshot contains all documents in the articles subcollection

                    // map each document into Article object
                    val userSavedArticles = snapshot.documents.mapNotNull { document ->

                        val articleMap = document.get("article") as? Map<*, *> ?: return@mapNotNull null
                        val sourceMap = articleMap["source"] as? Map<*, *>
                        Article(
                            author = articleMap["author"] as? String ?: "",
                            content = articleMap["content"] as? String ?: "",
                            description = articleMap["description"] as? String ?: "",
                            publishedAt = articleMap["publishedAt"] as? String ?: "",
                            source = Source(
                                id = sourceMap?.get("id") as? String ?: "",
                                name = sourceMap?.get("name") as? String ?: ""
                            ),
                            title = articleMap["title"] as? String ?: "",
                            url = articleMap["url"] as? String ?: "",
                            urlToImage = articleMap["urlToImage"] as? String ?: ""
                        )
                    }

                    onResult(userSavedArticles)

                } else {
                    Log.d("SavedArticlesViewModel", "Firestore snapshot is null")
                    onResult(emptyList())
                }

            }

    }

}


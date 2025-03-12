package com.example.mynews.data

import android.net.Uri
import android.util.Log
import com.example.mynews.data.api.news.Article
import com.example.mynews.data.api.news.Source
import com.example.mynews.domain.repositories.SavedArticlesRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Implementation of the SavedArticlesRepository interface in .com.example.mynews/domain/repositories

class SavedArticlesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SavedArticlesRepository {

    override suspend fun saveArticle(userID: String, article: Article): Boolean {

        // navigate to where article should be stored in firestore

        // location will be:
        /*
        saved_articles (Collection)
        ├── userID (Document)
        │   ├── articles (Sub-collection)
        │   │   ├── URL 1 (Document) -> unique identifier for article
        │   │   │   ├── article info 1
                ├── URL 2 (Document)
        │       │   ├── article info 2
         */

        // firestore does not allow slashes in document IDs so need to encode URL
        val safeArticleURL = Uri.encode(article.url)

        val articleLocation = firestore.collection("saved_articles")
            .document(userID)
            .collection("articles")
            .document(safeArticleURL) // use URL as unique document identifier to avoid saving duplicate articles

        /*
        In firestore
        - Documents have unique IDs
        - By using article.url as the document ID, an article can only be saved once per used
        - If same article is saved again, firestore overwrites the existing document which is fine
          since this prevents duplicates
         */


        try {
            // although firestore prevents duplicate documents since article.url is document ID,
            // prevent unnecessary Firestore overwrites if the article that the user saved already
            // exists in firestore, ie was already saved previously

            // fetch document at the article location, using await since firestore is async
            val existingSavedArticle = articleLocation.get().await()

            // article user is trying to save already exists in firestore collection since it was
            // saved previously
            // ie in firstore, in articles subcollection, at document ID being the URL, there
            // already exists an article there
            if (existingSavedArticle.exists()) {
                Log.d("SavedArticlesRepositoryImpl", "Already saved article: ${article.title}")
                return false
            }

            // at this point, the article the user is saving hasn't been saved before
            // articleLocation already specifies the documentID as the article URL, so article
            // is stored there
            articleLocation.set(article).await() // save entire article
            Log.d("SavedArticlesRepositoryImpl", "Article saved successfully: ${article.title}")
            return true

        } catch (e: Exception) {
            Log.e("SavedArticlesRepositoryImpl", "Error saving article: ", e)
            return false
        }

    }

    override suspend fun deleteSavedArticle(userID: String, article: Article): Boolean {

        // navigate to where article should be stored in firestore

        // location will be:
        /*
        saved_articles (Collection)
        ├── userID (Document)
        │   ├── articles (Sub-collection)
        │   │   ├── URL 1 (Document) -> unique identifier for article
        │   │   │   ├── article info 1
                ├── URL 2 (Document)
        │       │   ├── article info 2
         */

        // Firestore does not allow slashes in document IDs so need to encode URL
        val safeArticleURL = Uri.encode(article.url)

        val articleLocation = firestore.collection("saved_articles")
            .document(userID)
            .collection("articles")
            .document(safeArticleURL) // use URL as unique document identifier to avoid saving duplicate articles


        try {


            // fetch document at the article location, using await since firestore is async
            val existingSavedArticle = articleLocation.get().await()

            // article user is trying to delete article that doesn't exists in firestore collection since it was
            // ie in firstore, in articles subcollection, at document ID being the URL, there
            // is no article there
            if (!existingSavedArticle.exists()) {
                Log.d("SavedArticlesRepositoryImpl", "Article not found, nothing to delete")
                return false
            }

            // at this point, the article the user is deleting is in firestore
            // articleLocation already specifies the documentID as the article URL, so article
            // is stored there
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
                        //document.toObject(Article::class.java)

                        // cannot just do document.toObject(Article::class.java)
                        // need the below - manual data mapping to extract each field from
                        // firestore, where the field name is in the parentheses
                        val sourceMap = document.get("source") as? Map<*, *>
                        Article(
                            author = document.getString("author") ?: "",
                            content = document.getString("content") ?: "",
                            description = document.getString("description") ?: "",
                            publishedAt = document.getString("publishedAt") ?: "",
                            source = Source(
                                id = sourceMap?.get("id") as? String ?: "",
                                name = sourceMap?.get("name") as? String ?: ""
                            ),
                            title = document.getString("title") ?: "",
                            url = document.getString("url") ?: "",
                            urlToImage = document.getString("urlToImage")
                        )
                    }
                    /*
                    saved_articles (Collection)
                    ├── userID (Document)
                    │   ├── articles (Sub-collection)

                     */

                    onResult(userSavedArticles)

                } else {
                    Log.d("SavedArticlesViewModel", "Firestore snapshot is null")
                    onResult(emptyList())
                }

            }

    }

}


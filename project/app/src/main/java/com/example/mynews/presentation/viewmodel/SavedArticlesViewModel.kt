package com.example.mynews.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.data.Constant
import com.example.mynews.data.api.Article
import com.example.mynews.data.api.RetrofitInstance
import com.example.mynews.data.api.Source
import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.repositories.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
//import com.kwabenaberko.newsapilib.NewsApiClient
//import com.kwabenaberko.newsapilib.models.request.TopHeadlinesRequest
//import com.kwabenaberko.newsapilib.models.response.ArticleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SavedArticlesViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {


    private val _savedArticles = MutableLiveData<List<Article>>(emptyList())
    val savedArticles: LiveData<List<Article>> = _savedArticles

    // saving article in firestore collection
    fun saveArticle(article: Article) {

        viewModelScope.launch { // firestore operations are async so need this

            // get current user
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                Log.e("NewsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            // at this point, successfully retrieved current user

            val firestore = FirebaseFirestore.getInstance() // get instance to interact with firestore database

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
                    Log.d("NewsViewModel", "Already saved article: ${article.title}")
                    return@launch
                }

                // at this point, the article the user is saving hasn't been saved before
                // articleLocation already specifies the documentID as the article URL, so article
                // is stored there
                articleLocation.set(article).await() // save entire article
                Log.d("NewsViewModel", "Article saved successfully: ${article.title}")

            } catch (e: Exception) {
                Log.e("NewsViewModel", "Error saving article: ", e)
            }

        }

    }


    // deleting article in firestore collection
    fun deleteSavedArticle(article: Article) {

        viewModelScope.launch { // firestore operations are async so need this

            // get current user
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                Log.e("NewsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            // at this point, successfully retrieved current user

            val firestore = FirebaseFirestore.getInstance() // get instance to interact with firestore database

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
                    Log.d("SavedArticlesViewModel", "Article not found, nothing to delete")
                    return@launch
                }

                // at this point, the article the user is deleting is in firestore
                // articleLocation already specifies the documentID as the article URL, so article
                // is stored there
                articleLocation.delete().await() // delete article
                Log.d("SavedArticlesViewModel", "Article deleted successfully: ${article.title}")
                _savedArticles.postValue(_savedArticles.value?.filter { it.url != article.url })
            } catch (e: Exception) {
                Log.e("SavedArticlesViewModel", "Error deleting article: ", e)
            }

        }

    }

    // retrieve user's saved articles from firestore

    fun fetchSavedArticles() {

        viewModelScope.launch{

            // get current user
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                Log.e("NewsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            // at this point, successfully retrieved current user

            val firestore = FirebaseFirestore.getInstance() // get instance to interact with firestore database

            // navigate to user's collection of saved articles in firestore

            /*
            saved_articles (Collection)
            ├── userID (Document)
            │   ├── articles (Sub-collection)

             */

            firestore.collection("saved_articles")
                .document(userID)
                .collection("articles")
                // add event listener
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("SavedArticlesViewModel", "Error fetching user's saved articles: ", error)
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

                        _savedArticles.postValue(userSavedArticles)

                    } else {
                        Log.d("SavedArticlesViewModel", "Firestore snapshot is null")
                    }



                }



        }

    }

}
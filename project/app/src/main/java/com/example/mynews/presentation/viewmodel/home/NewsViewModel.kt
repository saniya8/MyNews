package com.example.mynews.presentation.viewmodel.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.data.api.Article
//import com.kwabenaberko.newsapilib.NewsApiClient
//import com.kwabenaberko.newsapilib.models.request.TopHeadlinesRequest
//import com.kwabenaberko.newsapilib.models.response.ArticleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.mynews.domain.repositories.NewsRepository

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
): ViewModel() {

    private val _articles = MutableLiveData<List<Article>>()
    // Expose the private _articles as articles to the UI so we can observe
    // articles live data in the UI
    val articles: LiveData<List<Article>> = _articles

    // use Retrofit here since NewsApiClient not working

    private var hasFetchedNews = false // tracks if API call was made

    // Based on: hasFetchedNews usage and LaunchedEffect in MainScreen,
    // fetchNewsTopHeadlines will trigger (ie do a new request) if:
    // - Logging in
    // - Force close app, swipe it out of memory, and then relaunch
    // fetchNewsTopHeadlines will not trigger (ie not do a new request) if:
    // - Swiping between tabs like Home, Social, Goals, and Settings
    // - Close app but do not swipe to clear it from memory


    // for initial news display
    fun fetchTopHeadlines(forceFetch: Boolean = false) {

        if (hasFetchedNews && !forceFetch) return // Prevents duplicate API requests
        hasFetchedNews = true

        viewModelScope.launch {
            // getTopHeadlines is a suspend function, therefore will take time
            // to load, therefore wrap it in a coroutine using viewModelScope.launch
            val response = newsRepository.getTopHeadlines()

            // response represents entire HTTP response:
            // response.code() - status code
            // response.headers() - headers
            // response.errorBody() - error body
            // response.body() - body - the actual NewsResponse entity

            //Log.i("NewsAPI Response", "Response Code: ${response.code()}")

            if(response.isSuccessful) {

                //Log.i("NewsAPI Response: ", response.body().toString())

                val newsResponse = response.body()


                // Print the titles
                //newsResponse?.articles?.forEach { article ->
                //    Log.i("NewsAPI Response", "Title: ${article.title}")
                //}



                // Assign articles to the mutable live data _articles
                newsResponse?.articles?.let {
                    _articles.postValue(it)
                }


            } else {
                //response.errorBody()?.string()?.let { Log.i("NewsAPI Response Failed: ", it) }
                Log.i("NewsAPI Response Failure: ", response.message())
            }
        }
    }


    // for filtering
    fun fetchTopHeadlinesByCategory(category: String) {

        // if category is null, then requires fetching top headlines (since category
        // being null means category was deselected or never selected)
        // that is handled in LaunchedEffect(selectedCategory.value) in HomeScreen.kt
        // so if this function is called, category cannot be null


        viewModelScope.launch {
            // getTopHeadlinesByCategory is a suspend function, therefore will take time
            // to load, therefore wrap it in a coroutine using viewModelScope.launch

            /*

            // this was code when handling category == null
            // now handling that in HomeScreen.kt

            // value of response depends on the category value
            val response =
                if(category == null) {
                    newsApi.getTopHeadlines(language = language,
                                            apiKey = Constant.apiKey)
                } else { // category is not null
                    newsApi.getTopHeadlinesByCategory(language = language,
                                                      category = category,
                                                      apiKey = Constant.apiKey)
                }

             */


            val response = newsRepository.getTopHeadlinesByCategory(category)

            if(response.isSuccessful) {
                val newsResponse = response.body()
                // Assign articles to the mutable live data _articles
                newsResponse?.articles?.let {
                    _articles.postValue(it)
                }

            } else {
                Log.i("NewsAPI Response Failure By Category: ", response.message())
            }



        }

    }

    // for searching
    fun fetchEverythingBySearch(searchQuery : String) {


        viewModelScope.launch {

            // getEverythingBySearch is a suspend function, therefore will take time
            // to load, therefore wrap it in a coroutine using viewModelScope.launch

            val response = newsRepository.getEverythingBySearch(searchQuery)

            if(response.isSuccessful) {
                val newsResponse = response.body()
                // Assign articles to the mutable live data _articles
                newsResponse?.articles?.let {
                    _articles.postValue(it)
                }

            } else {
                Log.i("NewsAPI Response Failure By Category: ", response.message())
            }
        }

    }

    // saving article in firestore collection

    /*

     // moved to SavedArticlesViewModel
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

            // navigate to where article should be stored in firebase

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

     */

}
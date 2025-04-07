package com.example.mynews.service.repositories.home


import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mynews.domain.entities.Article
import com.example.mynews.utils.TestAccountManager
import com.example.mynews.utils.TestDataFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SavedArticlesRepositoryImplTest {

    private lateinit var savedArticlesRepository: SavedArticlesRepositoryImpl
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    private val testEmail = "unittest1@gmail.com"
    private val testPassword = "UnitTest1!"

    @Before
    fun setup(): Unit = runBlocking {
        firestore = FirebaseFirestore.getInstance()
        val accountManager = TestAccountManager(testEmail, testPassword)
        userId = accountManager.loginOrRegister().uid
        savedArticlesRepository = SavedArticlesRepositoryImpl(firestore)
    }

    // --------------------------
    // Save Article
    // --------------------------

    @Test
    fun saveArticle_returnsTrue_whenNewArticleIsSaved(): Unit = runBlocking {
        val article: Article = TestDataFactory.createIndexedArticle(1)

        val result = savedArticlesRepository.saveArticle(userId, article)

        val docRef = firestore.collection("saved_articles")
            .document(userId)
            .collection("articles")
            .document(Uri.encode(article.url))

        val savedDoc = docRef.get().await()

        assertTrue(savedDoc.exists())
        assertTrue(result)

        // Clean up to allow re-running the test safely
        deleteTestArticle(article.url)
    }

    @Test
    fun saveArticle_returnsFalse_whenArticleAlreadySaved(): Unit = runBlocking {
        val article: Article = TestDataFactory.createIndexedArticle(2)

        // Save once
        savedArticlesRepository.saveArticle(userId, article)

        // Try saving again
        val result = savedArticlesRepository.saveArticle(userId, article)

        val docRef = firestore.collection("saved_articles")
            .document(userId)
            .collection("articles")
            .document(Uri.encode(article.url))

        val savedDoc = docRef.get().await()

        assertTrue(savedDoc.exists())
        assertFalse(result)
    }

    // --------------------------
    // Delete Saved Article
    // --------------------------

    @Test
    fun deleteSavedArticle_returnsTrue_whenArticleExists(): Unit = runBlocking {
        val article: Article = TestDataFactory.createIndexedArticle(3)

        // Save the article first so it exists in Firestore
        savedArticlesRepository.saveArticle(userId, article)

        // Attempt to delete the article
        val result = savedArticlesRepository.deleteSavedArticle(userId, article)

        val docRef = firestore.collection("saved_articles")
            .document(userId)
            .collection("articles")
            .document(Uri.encode(article.url))

        val deletedDoc = docRef.get().await()

        assertFalse(deletedDoc.exists())
        assertTrue(result)

    }

    @Test
    fun deleteSavedArticle_returnsFalse_whenArticleDoesNotExist(): Unit = runBlocking {
        val article: Article = TestDataFactory.createIndexedArticle(1000000)

        // Ensure article is not in Firestore before deletion
        deleteTestArticle(article.url)

        // Try deleting non-existent article
        val result = savedArticlesRepository.deleteSavedArticle(userId, article)

        val docRef = firestore.collection("saved_articles")
            .document(userId)
            .collection("articles")
            .document(Uri.encode(article.url))

        val deletedDoc = docRef.get().await()

        assertFalse(deletedDoc.exists())
        assertFalse(result)
    }

    // --------------------------
    // Test Helpers
    // --------------------------


    private suspend fun deleteTestArticle(articleUrl: String) {
        val safeUrl = Uri.encode(articleUrl)
        firestore.collection("saved_articles")
            .document(userId)
            .collection("articles")
            .document(safeUrl)
            .delete()
            .await()
    }
}



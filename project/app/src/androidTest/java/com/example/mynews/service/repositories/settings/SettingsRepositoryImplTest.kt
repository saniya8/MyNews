package com.example.mynews.service.repositories.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mynews.utils.TestAccountManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsRepositoryImplTest {

    private lateinit var repository: SettingsRepositoryImpl
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    private val testEmail = "unittest1@gmail.com"
    private val testPassword = "UnitTest1!"

    @Before
    fun setup(): Unit = runBlocking {
        firestore = FirebaseFirestore.getInstance()
        val accountManager = TestAccountManager(testEmail, testPassword)
        userId = accountManager.loginOrRegister().uid
        repository = SettingsRepositoryImpl(firestore)
    }

    @Test
    fun updateNumWordsToSummarize_writesCorrectValueToFirestore(): Unit = runBlocking {
        val newNumWords = 150

        // Write new value
        repository.updateNumWordsToSummarize(userId, newNumWords)

        // Read back manually from Firestore to confirm
        val doc = firestore.collection("settings").document(userId).get().await()
        val storedValue = doc.getLong("numWordsToSummarize")?.toInt()

        assertEquals(newNumWords, storedValue)
    }

    @Test
    fun getNumWordsToSummarize_returnsCorrectValue(): Unit = runBlocking {
        val expectedNumWords = 200

        // Write directly to Firestore before test
        firestore.collection("settings")
            .document(userId)
            .set(mapOf("numWordsToSummarize" to expectedNumWords), SetOptions.merge())
            .await()

        val result = repository.getNumWordsToSummarize(userId)

        assertEquals(expectedNumWords, result)
    }

}

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mynews.domain.entities.Article
import com.example.mynews.service.repositories.home.HomeRepositoryImpl
import com.example.mynews.utils.TestAccountManager
import com.example.mynews.utils.TestDataFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeRepositoryImplTest {

    private lateinit var homeRepository: HomeRepositoryImpl
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    private val testEmail = "unittest1@gmail.com"
    private val testPassword = "UnitTest1!"

    @Before
    fun setup(): Unit = runBlocking {
        firestore = FirebaseFirestore.getInstance()
        val accountManager = TestAccountManager(testEmail, testPassword)
        userId = accountManager.loginOrRegister().uid
        homeRepository = HomeRepositoryImpl(firestore)
    }

    // --------------------------
    // Set Reaction
    // --------------------------

    @Test
    fun setReaction_returnsIsFirstReactionTrue_whenNewReactionIsAdded(): Unit = runBlocking {
        val article = TestDataFactory.createIndexedArticle(100)
        val reaction = "❤️"

        deleteReaction(userId, article) // clean start

        val result = homeRepository.setReaction(userId, article, reaction)

        val docRef = firestore.collection("reactions")
            .document(userId)
            .collection("users_reactions")
            .document(Uri.encode(article.url))

        val savedDoc = docRef.get().await()
        assertTrue(savedDoc.exists())
        assertEquals(reaction, savedDoc.getString("reaction"))

        assertTrue(result.isFirstReaction)
        assertFalse(result.wasSwitched)
        assertFalse(result.wasDeleted)

        deleteReaction(userId, article) // clean end
    }

    @Test
    fun setReaction_returnsWasSwitchedTrue_whenReactionIsChanged(): Unit = runBlocking {
        val article = TestDataFactory.createIndexedArticle(101)

        deleteReaction(userId, article)

        homeRepository.setReaction(userId, article, "👍")
        val result = homeRepository.setReaction(userId, article, "😂")

        val docRef = firestore.collection("reactions")
            .document(userId)
            .collection("users_reactions")
            .document(Uri.encode(article.url))

        val savedDoc = docRef.get().await()
        assertEquals("😂", savedDoc.getString("reaction"))


        assertFalse(result.isFirstReaction)
        assertTrue(result.wasSwitched)
        assertFalse(result.wasDeleted)

        deleteReaction(userId, article)
    }

    @Test
    fun setReaction_deletesReactionFromFirestore_whenReactionIsRemoved(): Unit = runBlocking {
        val article = TestDataFactory.createIndexedArticle(102)

        deleteReaction(userId, article)

        // Step 1: Set the reaction
        homeRepository.setReaction(userId, article, "😢")

        // Step 2: Wait until the reaction appears
        val docRef = firestore.collection("reactions")
            .document(userId)
            .collection("users_reactions")
            .document(Uri.encode(article.url))

        for (i in 1..20) {
            val doc = docRef.get().await()
            if (doc.exists() && doc.getString("reaction") == "😢") break
            delay(200)
        }

        // Step 3: Delete the reaction
        homeRepository.setReaction(userId, article, null)

        // Step 4: Confirm the document was actually deleted
        for (i in 1..20) {
            val doc = docRef.get().await()
            if (!doc.exists()) {
                // Firestore doc is gone = UI will be updated
                return@runBlocking
            }
            delay(200)
        }

        fail("Reaction was not deleted from Firestore")
    }





    // --------------------------
    // Get Reaction
    // --------------------------

    @Test
    fun getReaction_returnsCorrectReaction_whenReactionExists(): Unit = runBlocking {
        val article = TestDataFactory.createIndexedArticle(103)
        val expectedReaction = "🤯"

        deleteReaction(userId, article)

        homeRepository.setReaction(userId, article, expectedReaction)

        val result = homeRepository.getReaction(userId, article)

        assertEquals(expectedReaction, result)

        // keep the reaction if needed for future reads
    }

    @Test
    fun getReaction_returnsNull_whenNoReactionExists(): Unit = runBlocking {
        val article = TestDataFactory.createIndexedArticle(104)

        deleteReaction(userId, article) // ensure clean state

        val result = homeRepository.getReaction(userId, article)

        assertNull(result)
    }

    // --------------------------
    // Test Helpers
    // --------------------------

    private suspend fun deleteReaction(userId: String, article: Article) {
        val safeUrl = Uri.encode(article.url)
        firestore.collection("reactions")
            .document(userId)
            .collection("users_reactions")
            .document(safeUrl)
            .delete()
            .await()
    }

}


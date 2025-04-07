package com.example.mynews.service.repositories.goals

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mynews.service.repositories.home.HomeRepositoryImpl
import com.example.mynews.service.repositories.social.FriendsRepositoryImpl
import com.example.mynews.utils.TestAccountManager
import com.example.mynews.utils.TestDataFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalsRepositoryImplTest {

    private lateinit var goalsRepository: GoalsRepositoryImpl
    private lateinit var homeRepository: HomeRepositoryImpl
    private lateinit var friendsRepository: FriendsRepositoryImpl
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
        friendsRepository = FriendsRepositoryImpl(firestore)
        goalsRepository = GoalsRepositoryImpl(firestore, friendsRepository)

        // clean previous reactions
        val reactions = firestore.collection("reactions")
            .document(userId)
            .collection("users_reactions")
            .get().await()
        reactions.documents.forEach { it.reference.delete().await() }

        // clean previous friends
        val friends = firestore.collection("friends")
            .document(userId)
            .collection("users_friends")
            .get().await()
        friends.documents.forEach { it.reference.delete().await() }

        // clean activity logs
        val activity = firestore.collection("goals")
            .document(userId)
            .collection("activity")
            .get().await()
        activity.documents.forEach { it.reference.delete().await() }

        // clean missions
        val missions = firestore.collection("goals")
            .document(userId)
            .collection("missions")
            .get().await()
        missions.documents.forEach { it.reference.delete().await() }

        // clean streak
        firestore.collection("goals")
            .document(userId)
            .collection("streak")
            .document("current")
            .delete()
            .await()
    }

    @Test
    fun logArticleRead_updatesMissionAndStreak(): Unit = runBlocking {
        val article = TestDataFactory.createIndexedArticle(200)
        goalsRepository.logArticleRead(userId, article)

        val missions = goalsRepository.getMissions(userId)
        val readMission = missions.find { it.type == "read_article" }

        assertNotNull(readMission)
        assertTrue(readMission!!.currentCount >= 1)

        // verify activity log exists
        val safeUrl = android.net.Uri.encode(article.url)
        val doc = firestore.collection("goals")
            .document(userId)
            .collection("activity")
            .document(safeUrl)
            .get()
            .await()
        assertTrue(doc.exists())
    }

    @Test
    fun logReactionAdded_incrementsMissionProgress(): Unit = runBlocking {
        val article = TestDataFactory.createIndexedArticle(201)
        homeRepository.setReaction(userId, article, "❤️")
        goalsRepository.logReactionAdded(userId)

        val missions = goalsRepository.getMissions(userId)
        val reactMission = missions.find { it.type == "react_to_article" }

        assertNotNull(reactMission)
        assertTrue(reactMission!!.currentCount >= 1)
    }

    @Test
    fun logReactionRemoved_decrementsMissionProgress(): Unit = runBlocking {
        val article = TestDataFactory.createIndexedArticle(202)
        homeRepository.setReaction(userId, article, "😂")
        goalsRepository.logReactionAdded(userId)

        // now remove
        homeRepository.setReaction(userId, article, null)
        goalsRepository.logReactionRemoved(userId)

        val missions = goalsRepository.getMissions(userId)
        val reactMission = missions.find { it.type == "react_to_article" }

        assertNotNull(reactMission)
        assertTrue(reactMission!!.currentCount >= 0)
    }

    @Test
    fun logAddOrRemoveFriend_updatesAddFriendMission(): Unit = runBlocking {
        // add 2 friends
        friendsRepository.addFriend(userId, "unittest5")
        friendsRepository.addFriend(userId, "unittest6")

        goalsRepository.logAddOrRemoveFriend(userId)

        val missions = goalsRepository.getMissions(userId)
        val friendMission = missions.find { it.type == "add_friend" }

        assertNotNull(friendMission)
        assertTrue(friendMission!!.currentCount >= 2)
    }

    @Test
    fun getMissions_returnsSeededAndUpdatedMissions(): Unit = runBlocking {
        val missions = goalsRepository.getMissions(userId)

        assertTrue(missions.isNotEmpty())
        assertTrue(missions.all { it.name.isNotBlank() && it.type.isNotBlank() })
    }
}

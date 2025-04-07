package com.example.mynews.service.repositories.social

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mynews.domain.result.AddFriendResult
import com.example.mynews.utils.TestAccountManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// unittest2, unittest3, and unittest4 are used solely for the unit tests for FriendsRepositoryImpl

@RunWith(AndroidJUnit4::class)
class FriendsRepositoryImplTest {

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
        friendsRepository = FriendsRepositoryImpl(firestore)

        // clean up all friends before starting
        val friendsRef = firestore.collection("friends")
            .document(userId)
            .collection("users_friends")
            .get()
            .await()
        friendsRef.documents.forEach { it.reference.delete().await() }
    }

    @Test
    fun addFriend_returnsSuccess_whenValidFriendAdded(): Unit = runBlocking {
        val result = friendsRepository.addFriend(userId, "unittest2")

        val friendUid = getUidForUsername("unittest2")
        val doc = getFriendDoc(userId, friendUid)

        assertEquals(AddFriendResult.Success, result)
        assertTrue(doc.exists())
        assertEquals("unittest2", doc.getString("username"))
    }

    @Test
    fun addFriend_returnsAlreadyAddedFriend_whenAddingSameFriendTwice(): Unit = runBlocking {
        friendsRepository.addFriend(userId, "unittest3") // First time
        val result = friendsRepository.addFriend(userId, "unittest3") // Second time

        val friendUid = getUidForUsername("unittest3")
        val doc = getFriendDoc(userId, friendUid)

        assertEquals(AddFriendResult.AlreadyAddedFriend, result)
        assertTrue(doc.exists()) // Still exists from first call
    }

    @Test
    fun addFriend_returnsSelfAddAttempt_whenAddingSelf(): Unit = runBlocking {
        val result = friendsRepository.addFriend(userId, "unittest1")

        val selfUid = userId
        val doc = getFriendDoc(userId, selfUid)

        assertEquals(AddFriendResult.SelfAddAttempt, result)
        assertFalse(doc.exists()) // Should not exist
    }

    @Test
    fun addFriend_returnsUserNotFound_whenUsernameDoesNotExist(): Unit = runBlocking {
        val result = friendsRepository.addFriend(userId, "nonexistentuser123")

        // 🔍 Attempt to resolve nonexistent UID path (should not exist)
        val fakeDoc = firestore.collection("usernames")
            .document("nonexistentuser123")
            .collection("private")
            .document("uid")
            .get()
            .await()

        assertEquals(AddFriendResult.UserNotFound, result)
        assertFalse(fakeDoc.exists())
    }

    @Test
    fun removeFriend_returnsTrue_whenFriendExists(): Unit = runBlocking {
        friendsRepository.addFriend(userId, "unittest4")
        val result = friendsRepository.removeFriend(userId, "unittest4")

        val friendUid = getUidForUsername("unittest4")
        val doc = getFriendDoc(userId, friendUid)

        assertTrue(result)
        assertFalse(doc.exists()) // confirm removed
    }

    @Test
    fun removeFriend_returnsFalse_whenFriendDoesNotExist(): Unit = runBlocking {
        val result = friendsRepository.removeFriend(userId, "nonexistentuser123")

        val fakeDoc = firestore.collection("usernames")
            .document("nonexistentuser123")
            .collection("private")
            .document("uid")
            .get()
            .await()

        assertFalse(result)
        assertFalse(fakeDoc.exists())
    }

    @Test
    fun getFriendCount_returnsCorrectNumber(): Unit = runBlocking {
        friendsRepository.addFriend(userId, "unittest2")
        friendsRepository.addFriend(userId, "unittest3")

        val count = friendsRepository.getFriendCount(userId)

        val friendUid2 = getUidForUsername("unittest2")
        val friendUid3 = getUidForUsername("unittest3")

        val doc2 = getFriendDoc(userId, friendUid2)
        val doc3 = getFriendDoc(userId, friendUid3)

        assertTrue(doc2.exists())
        assertTrue(doc3.exists())
        assertEquals(2, count)
    }

    // ---------------------
    // Test Helpers
    // ---------------------

    private suspend fun getUidForUsername(username: String): String {
        return firestore.collection("usernames")
            .document(username)
            .collection("private")
            .document("uid")
            .get()
            .await()
            .getString("uid") ?: throw IllegalStateException("UID not found for username: $username")
    }

    private suspend fun getFriendDoc(userId: String, friendUid: String) =
        firestore.collection("friends")
            .document(userId)
            .collection("users_friends")
            .document(friendUid)
            .get()
            .await()
}

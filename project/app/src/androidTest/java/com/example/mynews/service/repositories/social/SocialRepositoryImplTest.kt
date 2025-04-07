package com.example.mynews.service.repositories.social

// For separation of concerns purposes:
// - Adding and removing friends already tested in FriendsRepositoryImplTest
// (using unittest2, unittest3, unittest4 and adding/removing to unittest1)
// - Reacting to articles was already tested in HomeRepositoryImplTest
// (using unittest1)
// - Here, unittest5, unittest6, and unittest7 have already been added
//  to unittest1's friends list, and they have each reacted to articles.
//  This works per the tests above.
// - This test class will solely test the socialRepositoryImpl, and not
// the adding/removing of friends or reacting to articles for separation
// of concerns purposes, as these have already been tested in other test cases


import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mynews.service.repositories.social.SocialRepositoryImpl
import com.example.mynews.utils.TestAccountManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SocialRepositoryImplTest {

    private lateinit var socialRepository: SocialRepositoryImpl
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    private val testEmail = "unittest1@gmail.com"
    private val testPassword = "UnitTest1!"

    @Before
    fun setup(): Unit = runBlocking {
        firestore = FirebaseFirestore.getInstance()
        val accountManager = TestAccountManager(testEmail, testPassword)
        userId = accountManager.loginOrRegister().uid
        socialRepository = SocialRepositoryImpl(firestore)
    }

    @Test
    fun getFriends_returnsCorrectFriendUsernames(): Unit = runBlocking {
        // set up a deferred to capture the async callback
        val resultDeferred = CompletableDeferred<Map<String, String>>()

        // listen to friend map
        socialRepository.getFriends(userId) { result ->
            if (!resultDeferred.isCompleted) {
                resultDeferred.complete(result)
            }
        }

        // wait for snapshot listener to emit (max timeout is 5s)
        val friendMap = resultDeferred.await()

        // expected usernames (from manual setup) ->
        // since adding friends was tested in FriendsRepositoryImplTest
        val expectedUsernames = setOf("unittest5", "unittest6", "unittest7")

        // check the returned map
        val returnedUsernames = friendMap.values.toSet()
        assertEquals(expectedUsernames, returnedUsernames)

        // check in Firestore that the friends collection actually has the docs
        val friendsCollection = firestore.collection("friends")
            .document(userId)
            .collection("users_friends")
            .get()
            .await()

        val firestoreUsernames = friendsCollection.documents.mapNotNull {
            it.getString("username")
        }.toSet()

        assertEquals(expectedUsernames, firestoreUsernames)
    }
}

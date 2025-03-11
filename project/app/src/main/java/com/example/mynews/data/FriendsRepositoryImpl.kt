package com.example.mynews.data
import android.util.Log
import androidx.compose.runtime.MutableState
import com.example.mynews.domain.repositories.FriendsRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Implementation of the FriendsRepository interface in .com.example.mynews/domain/repositories

class FriendsRepositoryImpl (
    private val firestore : FirebaseFirestore
): FriendsRepository {

    /*

    // we store the friend user IDs as the document IDs so we can manage friendships and in the
    // front end, display the usernames (e.g., on social screen or friend activity screen)
    // and in the backend, retrieve their reactions etc by:
    // a) for a user, retrieving their friend's username from the friends collection and
    // b) using the friend's username to access their reactions/other info from the reactions
    // collection

        friends (Collection)
        ├── userID (Document)
        │   ├── users_friends (Sub-collection)
        │   │   ├── friend1 user ID (Document)
        │   │   │   ├── username
        │   │   │   ├── timestamp
        │   │   ├── friend2 user ID (Document)
        │   │   │   ├── username
        │   │   │   ├── timestamp

     */


    // addFriend: adds friend in firestore and returns true if it was successful, and false
    // otherwise
    override suspend fun addFriend(currentUserID: String, friendUsername: String,
                                   isFriendNotFound: MutableState<Boolean>
    ): Boolean {
        return try {

            // check if friend exists

            val friendLocation = firestore.collection("usernames")
                                          .document(friendUsername).get().await()
            if (!friendLocation.exists()) {
                Log.e("Add Friend", "Username '${friendUsername}' does not exist in Firestore")
                isFriendNotFound.value = true
                return false
            }

            // get friend's UID from usernames collection in firestore

            val friendUserIDLocation = firestore.collection("usernames")
                                            .document(friendUsername)
                                            .collection("private")
                                            .document("uid").get().await()

            val friendUserID = friendUserIDLocation.getString("uid") ?: return false // should never return false here since uid should always be in document

            // add to friends collection

            val friendData = mapOf("username" to friendUsername,
                                   "timestamp" to System.currentTimeMillis(),
                                  )

            // add friend to user's friends in firestore

            firestore.collection("friends")
                .document(currentUserID)
                .collection("users_friends")
                .document(friendUserID).set(friendData).await()

            Log.d("Add Friend", "Successfully added friend: $friendUsername ($friendUserID)")

            true

        } catch (e: Exception) {
            Log.e("Add Friend", "Error adding friend: ${e.message}", e)
            false

        }


    }


    // removeFriend: removes friend from firestore and returns true if it was successful, and
    // returns false otherwise
    override suspend fun removeFriend(currentUserID: String, friendUsername: String): Boolean {

        return try {

            // check if friend exists (should always be true because in the UI, user can only click
            // delete for friends that they see

            val friendLocation = firestore.collection("usernames")
                .document(friendUsername).get().await()
            if (!friendLocation.exists()) {
                Log.e("Remove Friend", "Username '${friendUsername}' does not exist in Firestore")
                return false
            }

            // get friend's UID from usernames collection in firestore

            val friendUserIDLocation = firestore.collection("usernames")
                .document(friendUsername)
                .collection("private")
                .document("uid").get().await()

            val friendUserID = friendUserIDLocation.getString("uid") ?: return false // should never return false here since uid should always be in document

            // remove from friends collection

            firestore.collection("friends")
                .document(currentUserID)
                .collection("users_friends")
                .document(friendUserID).delete().await()

            Log.d("Remove Friend", "Successfully removed friend: $friendUsername ($friendUserID)")

            true

        } catch (e: Exception) {
            Log.e("Remove Friend", "Error adding friend: ${e.message}", e)
            false

        }

    }


    // getFriends: returns a list of strings where each item in the list is the username of
    // a friend
    override suspend fun getFriends(currentUserID: String, onResult: (List<String>) -> Unit) {
        firestore.collection("friends")
            .document(currentUserID)
            .collection("users_friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Get Friends", "Error fetching friends: ${error.message}", error)
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val friendUsernames = snapshot.documents.map { it.id }
                    Log.d("Get Friends", "Successfully retrieved friends: $friendUsernames")
                    onResult(friendUsernames) // Updates UI via ViewModel
                } else {
                    Log.d("Get Friends", "Firestore snapshot is null")
                    onResult(emptyList())
                }
            }
    }

}


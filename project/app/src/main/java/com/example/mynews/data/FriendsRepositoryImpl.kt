package com.example.mynews.data
import android.util.Log
import androidx.compose.runtime.MutableState
import com.example.mynews.domain.repositories.FriendsRepository
import com.example.mynews.presentation.state.AddFriendState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query


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
    ): AddFriendState {
        try {

            val normalizedFriendUsername = friendUsername.trim().lowercase()

            val currentUserUsername = firestore.collection("users").document(currentUserID).get().await().getString("username")
            val friendLocation = firestore.collection("usernames")
                                          .document(normalizedFriendUsername).get().await()

            // check if valid username or if the username is the user (cant add yourself)

            if (currentUserUsername == normalizedFriendUsername) {
                Log.e("Add Friend", "Username '${normalizedFriendUsername}' does not exist in Firestore")
                isFriendNotFound.value = true
                return AddFriendState.SelfAddAttempt
            }

            if (!friendLocation.exists()) {
                Log.e("Add Friend", "Username '${normalizedFriendUsername}' does not exist in Firestore")
                isFriendNotFound.value = true
                return AddFriendState.UserNotFound
            }



            val friendUserIDLocation = firestore.collection("usernames")
                                            .document(normalizedFriendUsername)
                                            .collection("private")
                                            .document("uid").get().await()

            val friendUserID = friendUserIDLocation.getString("uid") ?: return AddFriendState.UserNotFound

            // check if friend already added as a friend

            val alreadyFriendSnapshot = firestore.collection("friends")
                .document(currentUserID)
                .collection("users_friends")
                .document(friendUserID)
                .get()
                .await()

            if (alreadyFriendSnapshot.exists()) {
                Log.d("Add Friend", "User $normalizedFriendUsername is already a friend")
                return AddFriendState.AlreadyAddedFriend
            }

            val friendData = mapOf("username" to normalizedFriendUsername,
                                   "timestamp" to System.currentTimeMillis(),
                                  )

            // add friend to user's friends in firestore
            firestore.collection("friends")
                .document(currentUserID)
                .collection("users_friends")
                .document(friendUserID).set(friendData).await()

            Log.d("Add Friend", "Successfully added friend: $normalizedFriendUsername ($friendUserID)")

            return AddFriendState.Success

        } catch (e: Exception) {
            Log.e("Add Friend", "Error adding friend: ${e.message}", e)
            return AddFriendState.Error(e.message ?: "Unknown error occurred")

        }
    }

    // removeFriend: removes friend from firestore and returns true if it was successful, and
    // returns false otherwise
    override suspend fun removeFriend(currentUserID: String, friendUsername: String): Boolean {

        try {

            val normalizedFriendUsername = friendUsername.trim().lowercase()

            // check if friend exists (should always be true because in the UI, user can only click
            // delete for friends that they see
            val friendLocation = firestore.collection("usernames")
                .document(normalizedFriendUsername).get().await()
            if (!friendLocation.exists()) {
                Log.e("Remove Friend", "Username '${normalizedFriendUsername}' does not exist in Firestore")
                return false
            }

            val friendUserIDLocation = firestore.collection("usernames")
                .document(normalizedFriendUsername)
                .collection("private")
                .document("uid").get().await()

            val friendUserID = friendUserIDLocation.getString("uid") ?: return false

            firestore.collection("friends")
                .document(currentUserID)
                .collection("users_friends")
                .document(friendUserID).delete().await()

            Log.d("Remove Friend", "Successfully removed friend: $normalizedFriendUsername ($friendUserID)")
            return true
        } catch (e: Exception) {
            Log.e("Remove Friend", "Error adding friend: ${e.message}", e)
            return false

        }

    }

    // Get user's friend's Usernames
    override suspend fun getFriendUsernames(currentUserID: String, onResult: (List<String>) -> Unit) {
        firestore.collection("friends")
            .document(currentUserID)
            .collection("users_friends")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    val friendUsernames = snapshot.documents.mapNotNull { it.getString("username") }
                    Log.d("Get Friend Usernames", "Successfully retrieved friend usernames: $friendUsernames")
                    onResult(friendUsernames)
                } else {
                    Log.d("Get Friend Usernames", "Firestore snapshot is null")
                    onResult(emptyList())
                }
            }
            .addOnFailureListener { e ->
                Log.e("Get Friend Usernames", "Error fetching friend usernames: ${e.message}", e)
                onResult(emptyList())
            }
    }


}


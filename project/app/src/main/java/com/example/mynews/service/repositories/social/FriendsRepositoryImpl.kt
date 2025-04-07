package com.example.mynews.service.repositories.social
import android.util.Log
import com.example.mynews.domain.repositories.social.FriendsRepository
import com.example.mynews.domain.result.AddFriendResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FriendsRepositoryImpl (
    private val firestore : FirebaseFirestore
): FriendsRepository {

    // friendships are one-way
    // if test1 adds test2 as a friend, that does not mean test2 has test1 as friend
    // so, test1 can see test2's reactions in their friend activity, but test2 cannot see test1's

    // addFriend: adds friend in firestore and returns true if it was successful, and false otherwise
    override suspend fun addFriend(currentUserID: String, friendUsername: String): AddFriendResult {
        try {

            val normalizedFriendUsername = friendUsername.trim().lowercase()

            val currentUserUsername = firestore.collection("users").document(currentUserID).get().await().getString("username")
            val friendLocation = firestore.collection("usernames")
                                          .document(normalizedFriendUsername).get().await()

            // check if valid username or if the username is invalid

            if (currentUserUsername == normalizedFriendUsername) {
                Log.e("Add Friend", "Username '${normalizedFriendUsername}' does not exist in Firestore")
                return AddFriendResult.SelfAddAttempt
            }

            if (!friendLocation.exists()) {
                Log.e("Add Friend", "Username '${normalizedFriendUsername}' does not exist in Firestore")
                return AddFriendResult.UserNotFound
            }

            val friendUserIDLocation = firestore.collection("usernames")
                                            .document(normalizedFriendUsername)
                                            .collection("private")
                                            .document("uid").get().await()

            val friendUserID = friendUserIDLocation.getString("uid") ?: return AddFriendResult.UserNotFound

            // check if friend already added as a friend

            val alreadyFriendSnapshot = firestore.collection("friends")
                .document(currentUserID)
                .collection("users_friends")
                .document(friendUserID)
                .get()
                .await()

            if (alreadyFriendSnapshot.exists()) {
                Log.d("Add Friend", "User $normalizedFriendUsername is already a friend")
                return AddFriendResult.AlreadyAddedFriend
            }

            val friendData = mapOf("username" to normalizedFriendUsername,
                                   "timestamp" to System.currentTimeMillis(),
                                  )

            // add uid field (for delete account's collection query to work)
            firestore.collection("friends")
                .document(currentUserID)
                .set(mapOf("uid" to currentUserID))
                .await()

            // add friend to user's friends in firestore
            firestore.collection("friends")
                .document(currentUserID)
                .collection("users_friends")
                .document(friendUserID).set(friendData).await()

            Log.d("Add Friend", "Successfully added friend: $normalizedFriendUsername ($friendUserID)")

            return AddFriendResult.Success

        } catch (e: Exception) {
            Log.e("Add Friend", "Error adding friend: ${e.message}", e)
            return AddFriendResult.Error(e.message ?: "Unknown error occurred")

        }
    }

    // removeFriend: removes friend from firestore and returns true if it was successful, and
    // returns false otherwise
    override suspend fun removeFriend(currentUserID: String, friendUsername: String): Boolean {

        try {

            val normalizedFriendUsername = friendUsername.trim().lowercase()

            // check if friend exists (should always be true because in the UI, user can only click
            // delete for friends that they see)
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

    // get user's friend's Usernames
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

    override suspend fun getFriendCount(currentUserID: String): Int {
        val friendsSnapshot = firestore.collection("friends")
            .document(currentUserID)
            .collection("users_friends")
            .get()
            .await()
        return friendsSnapshot.size()
    }


}


package com.example.mynews.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.mynews.domain.model.User
import com.example.mynews.domain.repositories.UserRepository
import com.google.firebase.firestore.FirebaseFirestoreException

// Implementation of the UserRepository interface in .com.example.mynews/domain/repositories

class UserRepositoryImpl (
    private val firestore : FirebaseFirestore
): UserRepository {

    override suspend fun addUser(user: User): Boolean {
        try {
            // user's username and email already normalized in AuthRepository's register,
            // where this function is called from
            firestore.collection("users").document(user.uid).set(user).await()
            return true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding user", e)
            return false
        }
    }

    // isUsernameTaken: returns true if username is already taken by another user, false otherwise
    override suspend fun isUsernameTaken(username: String): Boolean {
        Log.d("UsernameDebug", "In isUsernameTaken")
        try {
            val normalizedUsername = username.trim().lowercase()
            val doc = firestore.collection("usernames").document(normalizedUsername).get().await()
            Log.d("UsernameDebug", "Username taken? : ${doc.exists()}")
            return doc.exists() // doc.exists() is true if username is taken, false otherwise,
        } catch (e: Exception) {
            Log.e("UsernameDebug", "Error checking username: ${e.message}", e)
            return true // Assume taken if Firestore query fails
        }
    }

    // reserveUsername: reserves username for the user so no one else can take it
    override suspend fun reserveUsername(username: String, uid: String) {
        try {

            val normalizedUsername = username.trim().lowercase()

            // create the username document
            firestore.collection("usernames").document(normalizedUsername).set(mapOf<String, Any>()).await()

            // store the UID in the private subcollection
            firestore.collection("usernames").document(normalizedUsername)
                .collection("private").document("uid")
                .set(mapOf("uid" to uid)).await()
            Log.d("UsernameDebug", "Username '$normalizedUsername' reserved for UID: $uid")
        } catch (e: Exception) {
            Log.e("UsernameDebug", "Error reserving username: ${e.message}", e)
        }
    }

    override suspend fun initializeUserSettings(userId: String) {
        try {
            val settingsDoc = firestore.collection("settings").document(userId)
            val data = mapOf("numWordsToSummarize" to 100)
            settingsDoc.set(data).await()
        } catch (e: Exception) {
            Log.e("SettingsDebug", "Failed to initialize settings for user $userId: ${e.message}", e)
        }
    }

    override suspend fun getUserById(userId: String): User? {
        try {
            val document = firestore.collection("users").document(userId).get().await()
            return document.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user", e)
            return null
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    override suspend fun clearUserDataById(userId: String): Boolean {
        try {

            val currentUser = getUserById(userId)?: throw IllegalStateException("User not found for userId: $userId")
            val username = currentUser.username

            Log.d("UserRepository", "Starting deletion for userId: $userId, username: $username")

            // WORKS
            // delete from username collection - delete by username (since usernames are unique)
            Log.d("UserRepository", "Deleting usernames/$username")
            deleteDocumentWithSubcollections(
                collection = "usernames",
                documentId = username,
                subcollections = listOf("private")
            )
            Log.d("UserRepository", "Deleted usernames/$username")


            // WORKS
            // delete from saved articles collection
            Log.d("UserRepository", "Deleting saved_articles/$userId")
            deleteDocumentWithSubcollections(
                collection = "saved_articles",
                documentId = userId,
                subcollections = listOf("articles")
            )
            Log.d("UserRepository", "Deleted saved_articles/$userId")


            // WORKS
            // delete from reactions collection

            Log.d("UserRepository", "Deleting reactions/$userId")
            deleteDocumentWithSubcollections(
                collection = "reactions",
                documentId = userId,
                subcollections = listOf("users_reactions")
            )
            Log.d("UserRepository", "Deleted reactions/$userId")


            // WORKS
            // delete from goals collection
            Log.d("UserRepository", "Deleting goals/$userId")
            deleteDocumentWithSubcollections(
                collection = "goals",
                documentId = userId,
                subcollections = listOf("streak", "activity", "missions") // preemptively added missions
            )
            Log.d("UserRepository", "Deleted goals/$userId")

            Log.d("UserRepository", "Removing $userId from friends lists")


            // WORKS
            // delete from friends collection
            removeUserFromAllFriendsLists(userId)
            Log.d("UserRepository", "Removed $userId from friends lists")

            // SHOULD WORK
            // preemptively adding settings
            // delete from settings collection
            Log.d("UserRepository", "Deleting settings/$userId")
            firestore.collection("settings").document(userId).delete().await()
            Log.d("UserRepository", "Deleted settings/$userId")


            // WORKS
            // delete from users collection
            Log.d("UserRepository", "Deleting users/$userId")
            firestore.collection("users").document(userId).delete().await()
            Log.d("UserRepository", "Deleted users/$userId")

            Log.d("UserRepository", "Deletion complete for userId: $userId")
            return true
        } catch (e: FirebaseFirestoreException) {
            Log.e("UserRepository", "Firestore permission error: ${e.message}", e)
            return false
        } catch (e: Exception) {
            Log.e("UserRepository", "Error deleting user from firestore", e)
            return false
        }
    }


    private suspend fun deleteDocumentWithSubcollections(
        collection: String,
        documentId: String,
        subcollections: List<String>
    ) {
        val doc = firestore.collection(collection).document(documentId)

        for (sc in subcollections) {
            val current_sc = doc.collection(sc)
            val subdocuments = current_sc.get().await()
            for (doc in subdocuments) {
                doc.reference.delete().await()
            }
        }

        // delete the parent document
        doc.delete().await()
    }


    private suspend fun removeUserFromAllFriendsLists(userIdToRemove: String) {


        Log.d("UserRepository", "Current user: ${FirebaseAuth.getInstance().currentUser?.uid}")
        val friendsCollection = firestore.collection("friends")
        Log.d("UserRepository", "Querying collection: ${friendsCollection.path}")

        try {

            // Get all users
            val allUsers = friendsCollection.get().await()
            Log.d("UserRepository", "Fetched ${allUsers.size()} friends documents")

            for (userDoc in allUsers.documents) {
                val ownerUserID = userDoc.id
                Log.d("UserRepository", "Checking user: $ownerUserID")

                // skip own friends doc, we’ll delete that later
                if (ownerUserID == userIdToRemove) continue

                // reference to friends/{ownerUserId}/users_friends/{userIdToRemove}

                val friendRef = friendsCollection
                    .document(ownerUserID)
                    .collection("users_friends")
                    .document(userIdToRemove)

                try {
                    // Just try deleting directly
                    // if not found since user to remove is not in users_friends for a user, thats okay
                    // it wont go to catch block since there's no permission issue, it's just user
                    // merely doesn't exist
                    friendRef.delete().await()
                    Log.d("UserRepository", "Tried deleting $userIdToRemove from ${friendRef.path}")
                } catch (e: FirebaseFirestoreException) {
                    Log.w("UserRepository", "Firestore permission error in removeUserFromAllFriendsLists: ${e.message}", e)
                } catch (e: Exception) {
                    Log.w("UserRepository", "Error in removeUserFromAllFriendsLists: ${e.message}", e)

                }
            }

            // Delete your own entire friends/{userIdToRemove} doc and subcollection
            deleteDocumentWithSubcollections(
                collection = "friends",
                documentId = userIdToRemove,
                subcollections = listOf("users_friends")
            )

            Log.d("UserRepository", "Deleted friends/$userIdToRemove")

        } catch (e: FirebaseFirestoreException) {
            Log.e("UserRepository", "Firestore permission error in removeUserFromAllFriendsLists: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error cleaning up friends lists", e)
        }
    }

}
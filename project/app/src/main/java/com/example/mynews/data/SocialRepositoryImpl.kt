package com.example.mynews.data

import android.util.Log
import com.example.mynews.data.api.news.Article
import com.example.mynews.domain.model.Reaction
import com.example.mynews.data.api.news.Source
import com.example.mynews.domain.repositories.SocialRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class SocialRepositoryImpl (
    private val firestore : FirebaseFirestore
): SocialRepository {

    private val reactionListeners = mutableMapOf<String, ListenerRegistration>()
    // track each friend's current reaction snapshot
    private val reactionsMap = mutableMapOf<String, List<Reaction>>()

    // getFriendIds: returns a list of strings where each item in the list is the id of
    // a friend
    override suspend fun getFriendIds(currentUserID: String, onResult: (List<String>) -> Unit) {
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
                    val friendIDs = snapshot.documents.map { it.id }
                    Log.d("Get Friends", "Successfully retrieved friends: $friendIDs")
                    onResult(friendIDs) // Updates UI via ViewModel
                } else {
                    Log.d("Get Friends", "Firestore snapshot is null")
                    onResult(emptyList())
                }
            }
    }

    override suspend fun getFriendIdsAndUsernames(currentUserID: String, onResult: (Map<String, String>) -> Unit) { // String String
        firestore.collection("friends")
            .document(currentUserID)
            .collection("users_friends")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    // unsafe type casting
                    //val friendMap = snapshot.documents.associate {
                    //    ((it.id to it.getString("username")) ?: "") as Pair<*, *>
                    //}

                    // safe type casting
                    val friendMap = snapshot.documents.associate { doc ->
                        val friendID = doc.id
                        val username = doc.getString("username") ?: "Unknown"
                        friendID to username
                    }


                    Log.d("GetFriendIdsAndUsernames", "Successfully retrieved friend map: $friendMap")
                    onResult(friendMap)
                } else {
                    Log.d("GetFriendIdsAndUsernames", "Firestore snapshot is null")
                    onResult(emptyMap())
                }
            }
            .addOnFailureListener { e ->
                Log.e("GetFriendIdsAndUsernames", "Error fetching friend IDs and usernames: ${e.message}", e)
                onResult(emptyMap())
            }
    }




    override suspend fun getFriends(
        currentUserID: String,
        onResult: (Map<String, String>) -> Unit // friend id to username map
    ) {
        firestore.collection("friends")
            .document(currentUserID)
            .collection("users_friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("GetFriendsRealtime", "error fetching friends: ${error.message}", error)
                    onResult(emptyMap()) // eeturn empty if there's an error
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val friendMap = snapshot.documents.associate { doc ->
                        val friendID = doc.id
                        val friendUsername = doc.getString("username") ?: "Unknown" // for safe null handling in case
                        friendID to friendUsername
                    }

                    Log.d("GetFriendsRealtime", "Successfully retrieved friends: $friendMap")
                    onResult(friendMap)
                } else {
                    Log.d("GetFriendsRealtime", "Firestore snapshot is null")
                    onResult(emptyMap())
                }
            }
    }


    // same as original but with snapshot listener for automatic ui updates
    override fun getFriendsReactions(
        friendIDs: List<String>,
        onResult: (List<Reaction>) -> Unit
    ) {
        // track each friend's current reaction snapshot
        //val reactionsMap = mutableMapOf<String, List<Reaction>>()

        // Detach listeners for removed friends
        val removedFriends = reactionListeners.keys - friendIDs
        removedFriends.forEach { friendId ->
            reactionListeners[friendId]?.remove()
            reactionListeners.remove(friendId)
            reactionsMap.remove(friendId)

        }

        //onResult(reactionsMap.values.flatten())

        // immediately update the UI now that removed friends reactions are gone
        val currentReactions = reactionsMap.values
            .flatten()
            .sortedByDescending { it.timestamp }
        onResult(currentReactions)



        //reactionsMap.keys.retainAll(friendIDs) // to only retain friendids and not removed friends

        // Return early if no friends
        if (friendIDs.isEmpty()) {
            onResult(emptyList())
            return
        }

        try {
            for (friendId in friendIDs) {

                if (!reactionListeners.containsKey(friendId)) {


                    val listener = firestore.collection("reactions")
                        .document(friendId)
                        .collection("users_reactions")
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e(
                                    "getFriendsReactions",
                                    "Error fetching reactions: ${error.message}",
                                    error
                                )
                                return@addSnapshotListener
                            }

                            if (snapshot != null) {
                                val updatedReactions = snapshot.documents.mapNotNull { document ->


                                    val articleData = document.get("article") as? Map<*, *>
                                        ?: return@mapNotNull null
                                    val sourceData = articleData["source"] as? Map<*, *>
                                        ?: return@mapNotNull null

                                    val source = Source(
                                        id = sourceData["id"] as? String ?: "",
                                        // id = "test",//sourceData["id"] as String, // get NULL in an example setting this to default to test
                                        name = sourceData["name"] as? String ?: "" // get
                                    )
                                    // Create an Article object directly from the Firestore data
                                    val article = Article(
                                        author = articleData["author"] as? String
                                            ?: "",//articleData["author"] as String, // can be null replacing with test
                                        content = articleData["content"] as? String ?: "",
                                        description = articleData["description"] as? String ?: "",
                                        publishedAt = articleData["publishedAt"] as? String ?: "",
                                        source = source,
                                        title = articleData["title"] as? String ?: "",
                                        url = articleData["url"] as? String ?: "",
                                        urlToImage = articleData["urlToImage"] as? String ?: ""
                                    )


                                    val reaction =
                                        document.getString("reaction") ?: return@mapNotNull null
                                    val timestamp =
                                        document.getLong("timestamp") ?: return@mapNotNull null

                                    Reaction(
                                        userID = friendId,
                                        article = article,
                                        reaction = reaction,
                                        timestamp = timestamp
                                    )
                                }

                                // remove old reactions for friends no longer in the list
                                //reactionsMap.keys.retainAll(friendIDs)

                                // update this friend's reactions
                                reactionsMap[friendId] = updatedReactions

                                // flatten all friends' reactions and send to ViewModel
                                val allReactions = reactionsMap.values
                                    .flatten()
                                    .sortedByDescending { it.timestamp } // reverse chronological order
                                onResult(allReactions)
                            }
                        }

                    reactionListeners[friendId] = listener


                }
            }
        } catch (e: Exception) {
            Log.e("getFriendsReactions", "Exception: ${e.message}", e)
        }
    }



    /*
    // original - did not update in real time
    override suspend fun getFriendsReactions(
        friendIDs: List<String>
    ): List<Reaction> {
        val reactions = mutableListOf<Reaction>()
        try {
            for (friendId in friendIDs) {
                val reactionSnapshot = firestore.collection("reactions")
                    .document(friendId)
                    .collection("users_reactions")
                    .get()
                    .await()

                for (document in reactionSnapshot.documents) {
                    val articleData = document.get("article") as? Map<*, *>
                    if (articleData == null) {
                        Log.w("GetFriendsReactions", "Article data is null for document: ${document.id}")
                        continue
                    }

                    val sourceData = articleData["source"] as Map<*, *>
                    val source = Source(
                        id = sourceData["id"] as? String ?: "",
                        // id = "test",//sourceData["id"] as String, // get NULL in an example setting this to default to test
                        name = sourceData["name"] as? String ?: "" // get
                    )
                    // Create an Article object directly from the Firestore data
                    val article = Article(
                        author = articleData["author"] as? String ?: "",//articleData["author"] as String, // can be null replacing with test
                        content = articleData["content"] as? String ?: "",
                        description = articleData["description"] as? String ?: "",
                        publishedAt = articleData["publishedAt"] as? String ?: "",
                        source = source,
                        title = articleData["title"] as? String ?: "",
                        url = articleData["url"] as? String ?: "",
                        urlToImage = articleData["urlToImage"] as? String ?: ""
                    )
                    val reaction = document.getString("reaction") ?: continue
                    val timestamp = document.getLong("timestamp") ?: continue
                    reactions.add(
                        Reaction(
                            userID = friendId,
                            article = article,
                            reaction = reaction,
                            timestamp = timestamp
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(
                "GetFriendsReactions",
                "Error fetching friends' reactions: ${e.message}",
                e
            )
        }
        return reactions
    }

     */

}
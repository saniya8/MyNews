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

}
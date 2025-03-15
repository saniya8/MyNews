package com.example.mynews.data

import android.net.Uri
import android.util.Log
import com.example.mynews.data.api.news.Article
import com.example.mynews.data.api.news.Reaction
import com.example.mynews.domain.repositories.HomeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.example.mynews.data.api.news.Source


class HomeRepositoryImpl @Inject constructor(
    private val firestore : FirebaseFirestore
) : HomeRepository {
    // getReaction: returns the reaction the user set for a particular article (try block), or null if
    // user never selected a reaction for the article or user deselected their reaction (catch block)
    override suspend fun getReaction(userID: String, article: Article): String? {
        val safeArticleURL = Uri.encode(article.url)
        try {
            val reactionLocation = firestore.collection("reactions")
                .document(userID)
                .collection("users_reactions")
                .document(safeArticleURL)
                .get()
                .await()

            return reactionLocation.getString("reaction")
        } catch (e: Exception) {
            Log.e(
                "GetReaction",
                "Error getting reaction for article ${article.url}: ${e.message}",
                e
            )
            return null
        }
    }

    override suspend fun setReaction(userID: String, article: Article, reaction: String?) {

        try {
            val safeArticleURL = Uri.encode(article.url)
            val reactionLocation = firestore.collection("reactions")
                .document(userID)
                .collection("users_reactions")
                .document(safeArticleURL)

            if (reaction == null) { // user deselected reaction
                reactionLocation.delete().await()
                Log.d(
                    "SetReaction",
                    "Removed reaction from firestore for article: ${article.title}"
                )
            } else { // reaction is one of the emojis in the reactions bar
                val reactionData = mapOf(
                    "article" to articleToMap(article),
                    "reaction" to reaction,
                    "timestamp" to System.currentTimeMillis(),
                )
                reactionLocation.set(reactionData).await()
                Log.d(
                    "SetReaction",
                    "Updated reaction for article: ${article.title} and set reaction to: ${reaction}"
                )
            }
        } catch (e: Exception) {
            Log.e(
                "SetReaction",
                "Error setting reaction for article ${article.url}: ${e.message}",
                e
            )
        }
    }

    private fun articleToMap(article: Article): Map<String, Any?> {
        return mapOf(
            "author" to article.author,
            "content" to article.content,
            "description" to article.description,
            "publishedAt" to article.publishedAt,
            "source" to mapOf(
                "id" to article.source.id,
                "name" to article.source.name
            ),
            "title" to article.title,
            "url" to article.url,
            "urlToImage" to article.urlToImage
        )
    }

    override suspend fun trackReactions(
        userID: String,
        onReactionChanged: (Map<String, String?>) -> Unit
    ) {
        firestore.collection("reactions")
            .document(userID)
            .collection("users_reactions")
            .addSnapshotListener { snapshot, error ->
                Log.d("ReactionDebug", "Snapshot listener triggered for userID: $userID")
                if (error != null) {
                    Log.e("ReactionDebug", "Error tracking reactions: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d(
                        "ReactionDebug",
                        "Snapshot received: ${snapshot.documents.size} documents"
                    )
                    // map article ID (safeArticleURL) → reaction
                    val reactionMap = snapshot.documents.associate { document ->
                        val articleID = document.id
                        val reaction = document.getString("reaction")
                        articleID to reaction
                    }

                    Log.d("ReactionDebug", "Real-time reactions updated: $reactionMap")

                    // pass to viewmodel
                    onReactionChanged(reactionMap)
                }
            }
    }

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
                            userId = friendId,
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
}

package com.example.mynews.service.repositories.home

import android.net.Uri
import android.util.Log
import com.example.mynews.domain.entities.Article
import com.example.mynews.domain.repositories.home.HomeRepository
import com.example.mynews.domain.result.UpdateReactionResult
import com.example.mynews.utils.articleToMap
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

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

    // setReaction: returns UpdateReactionResult, which is helpful for contributing reactions to missions
    // either way, updates the reaction in firestore
    override suspend fun setReaction(userID: String, article: Article, reaction: String?): UpdateReactionResult {
        try {
            val safeArticleURL = Uri.encode(article.url)
            val reactionLocation = firestore.collection("reactions")
                .document(userID)
                .collection("users_reactions")
                .document(safeArticleURL)


            // for missions

            // check to see if this is a new reaction or an updated reaction - relevant for missions
            val existingDoc = reactionLocation.get().await()
            val previousReaction = if (existingDoc.exists()) {
                existingDoc.getString("reaction")
            } else null

            // setting the three entries in UpdateReactionResult - by the logic, there can only be one true one
            val isFirstReaction = !existingDoc.exists()
            val wasDeleted = reaction == null
            val wasSwitched = existingDoc.exists() && previousReaction != null && previousReaction != reaction
            val result = UpdateReactionResult(
                isFirstReaction = isFirstReaction,
                wasDeleted = wasDeleted,
                wasSwitched = wasSwitched
            )

            // for reactions in firestore
            if (reaction == null) {
                reactionLocation.delete().await()
                Log.d(
                    "SetReaction",
                    "Removed reaction from firestore for article: ${article.title}"
                )
                return result

            } else { // reaction is one of the emojis in the reactions bar

                // either new reaction or updated reaction

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

            return result

        } catch (e: Exception) {
            Log.e(
                "SetReaction",
                "Error setting reaction for article ${article.url}: ${e.message}",
                e
            )
            return UpdateReactionResult(
                isFirstReaction = false,
                wasDeleted = false,
                wasSwitched = false
            )
        }
    }

    override fun trackReactions(
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

}

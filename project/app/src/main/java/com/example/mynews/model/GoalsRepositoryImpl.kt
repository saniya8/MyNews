package com.example.mynews.model

import android.net.Uri
import com.example.mynews.service.news.Article
import com.example.mynews.domain.repositories.GoalsRepository
import com.example.mynews.domain.repositories.FriendsRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import com.example.mynews.domain.entities.Streak
import com.example.mynews.domain.entities.Mission

class GoalsRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val friendsRepository: FriendsRepository // Dependency on FriendsRepository
) : GoalsRepository {



    override suspend fun logArticleRead(userId: String, article: Article) {

        // firestore does not allow slashes in document IDs so need to encode URL
        val safeArticleURL = Uri.encode(article.url)

        // check if this article was already read
        val activityCollection = firestore.collection("goals")
            .document(userId)
            .collection("activity")
        val articleDoc = activityCollection.document(safeArticleURL)

        val existingDoc = articleDoc.get().await()
        val isFirstReadForArticle = !existingDoc.exists()

        // update timestamp regardless for streaks purposes
        val timestamp = System.currentTimeMillis()
        firestore.collection("goals")
            .document(userId)
            .collection("activity")
            .document(safeArticleURL)
            .set(mapOf("timestamp" to timestamp))
            .await()
        updateStreak(userId)

        // only count the read towards missions if it's an article that hasn't been read before
        if (!isFirstReadForArticle) return

        // at this point, this is the first time the user has read the passed article

        // Update missions of type "read_article"
        val missions = getMissions(userId)
        missions.filter { it.type == "read_article" && !it.isCompleted }.forEach { mission ->
            val newCount = mission.currentCount + 1
            updateMissionProgress(userId, mission.id, newCount)
            if (newCount >= mission.targetCount) {
                markMissionComplete(userId, mission.id)
            }
        }
    }

    override suspend fun logReaction(userId: String) {
        // Update missions of type "react_to_article"
        val missions = getMissions(userId)
        missions.filter { it.type == "react_to_article" && !it.isCompleted }.forEach { mission ->
            val newCount = mission.currentCount + 1
            updateMissionProgress(userId, mission.id, newCount)
            if (newCount >= mission.targetCount) {
                markMissionComplete(userId, mission.id)
            }
        }
    }








    private fun getStartOfDayTimestamp(date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = sdf.parse(date) ?: return 0L
        return parsedDate.time
    }

    private suspend fun updateStreak(userId: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
        )

        val activitySnapshot = firestore.collection("goals")
            .document(userId)
            .collection("activity")
            .whereGreaterThan("timestamp", getStartOfDayTimestamp(today))
            .get()
            .await()
        if (activitySnapshot.isEmpty) return

        val streakDoc = firestore.collection("goals")
            .document(userId)
            .collection("streak")
            .document("current")
            .get()
            .await()
        val lastReadDate = streakDoc.getString("lastReadDate") ?: ""
        val currentCount = streakDoc.getLong("count")?.toInt() ?: 0

        val newStreak = when {
            lastReadDate == today -> Streak(currentCount, today)
            lastReadDate == yesterday -> Streak(currentCount + 1, today)
            lastReadDate.isEmpty() -> Streak(1, today)
            else -> Streak(1, today)
        }

        firestore.collection("goals")
            .document(userId)
            .collection("streak")
            .document("current")
            .set(mapOf("count" to newStreak.count, "lastReadDate" to today))
            .await()
    }

    override fun getStreakFlow(userId: String): Flow<Pair<Int, String>> = callbackFlow {
        val listener = firestore.collection("goals")
            .document(userId)
            .collection("streak")
            .document("current")
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    trySend(Pair(0, ""))
                    return@addSnapshotListener
                }
                val streak = if (doc != null && doc.exists()) {
                    Pair(
                        doc.getLong("count")?.toInt() ?: 0,
                        doc.getString("lastReadDate") ?: ""
                    )
                } else {
                    Pair(0, "")
                }
                trySend(streak)
            }
        awaitClose {listener.remove()}
    }


    // mission related functions
    override suspend fun getMissions(userId: String): List<Mission> {
        initializeMissions(userId)
        val snapshot = firestore.collection("goals")
            .document(userId)
            .collection("missions")
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            val id = doc.id
            val name = doc.getString("name") ?: return@mapNotNull null
            val description = doc.getString("description") ?: return@mapNotNull null
            val targetCount = doc.getLong("targetCount")?.toInt() ?: return@mapNotNull null
            val currentCount = doc.getLong("currentCount")?.toInt() ?: 0
            val isCompleted = doc.getBoolean("isCompleted") ?: false
            val type = doc.getString("type") ?: return@mapNotNull null
            Mission(id, name, description, targetCount, currentCount, isCompleted, type)
        }
    }

    override fun getMissionsFlow(userId: String): Flow<List<Mission>> = callbackFlow {
        initializeMissions(userId)
        updateAddFriendsMission(userId)

        val listener = firestore.collection("goals")
            .document(userId)
            .collection("missions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val missions = snapshot?.documents?.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val description = doc.getString("description") ?: return@mapNotNull null
                    val targetCount = doc.getLong("targetCount")?.toInt() ?: return@mapNotNull null
                    val currentCount = doc.getLong("currentCount")?.toInt() ?: 0
                    val isCompleted = doc.getBoolean("isCompleted") ?: false
                    val type = doc.getString("type") ?: return@mapNotNull null
                    Mission(id, name, description, targetCount, currentCount, isCompleted, type)
                } ?: emptyList()
                trySend(missions)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateMissionProgress(userId: String, missionId: String, newCount: Int) {
        firestore.collection("goals")
            .document(userId)
            .collection("missions")
            .document(missionId)
            .update("currentCount", newCount)
            .await()
    }

    override suspend fun markMissionComplete(userId: String, missionId: String) {
        firestore.collection("goals")
            .document(userId)
            .collection("missions")
            .document(missionId)
            .update("isCompleted", true)
            .await()
    }

    private suspend fun getFriendCount(userId: String): Int {
        return friendsRepository.getFriendCount(userId)
    }

    private suspend fun updateAddFriendsMission(userId: String) {
        val friendCount = getFriendCount(userId)
        val missions = getMissions(userId)
        missions.filter { it.type == "add_friend" && !it.isCompleted }.forEach { mission ->
            val newCount = friendCount.coerceAtMost(mission.targetCount) // Don't exceed targetCount
            updateMissionProgress(userId, mission.id, newCount)
            if (newCount >= mission.targetCount) {
                markMissionComplete(userId, mission.id)
            }
        }
    }

    private suspend fun initializeMissions(userId: String) {
        val missionsSnapshot = firestore.collection("goals")
            .document(userId)
            .collection("missions")
            .get()
            .await()

        // Get the IDs of existing missions
        val existingMissionIds = missionsSnapshot.documents.map { it.id }

        // Add any default missions that don't already exist
        Mission.defaultMissions.forEach { defaultMission ->
            if (defaultMission.id !in existingMissionIds) {
                firestore.collection("goals")
                    .document(userId)
                    .collection("missions")
                    .document(defaultMission.id)
                    .set(
                        mapOf(
                            "name" to defaultMission.name,
                            "description" to defaultMission.description,
                            "targetCount" to defaultMission.targetCount,
                            "currentCount" to defaultMission.currentCount,
                            "isCompleted" to defaultMission.isCompleted,
                            "type" to defaultMission.type
                        )
                    )
                    .await()
            }
        }
    }
}
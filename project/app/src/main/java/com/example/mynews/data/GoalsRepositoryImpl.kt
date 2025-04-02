package com.example.mynews.data

import android.net.Uri
import com.example.mynews.data.api.news.Article
import com.example.mynews.domain.repositories.GoalsRepository
import com.example.mynews.domain.repositories.FriendsRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import com.example.mynews.domain.model.Mission

class GoalsRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val friendsRepository: FriendsRepository // Dependency on FriendsRepository
) : GoalsRepository {

    private data class Streak(
        val count: Int,
        val lastReadDate: String
    )

    override suspend fun logArticleRead(userId: String, article: Article) {

        // firestore does not allow slashes in document IDs so need to encode URL
        val safeArticleURL = Uri.encode(article.url)


        val timestamp = System.currentTimeMillis()
        firestore.collection("goals")
            .document(userId)
            .collection("activity")
            .document(safeArticleURL)
            .set(mapOf("timestamp" to timestamp))
            .await()
        updateStreak(userId)

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

    override suspend fun getStreak(userId: String): Int {
        val doc = firestore.collection("goals")
            .document(userId)
            .collection("streak")
            .document("current")
            .get()
            .await()
        return if (doc.exists()) {
            doc.getLong("count")?.toInt() ?: 0
        } else {
            0
        }
    }

    private fun getStartOfDayTimestamp(date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = sdf.parse(date) ?: return 0L
        return parsedDate.time
    }

    override suspend fun updateStreak(userId: String) {
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

    override suspend fun getFriendCount(userId: String): Int {
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



    override suspend fun initializeMissions(userId: String) {
        val missionsSnapshot = firestore.collection("goals")
            .document(userId)
            .collection("missions")
            .get()
            .await()

        // If the user has no missions, initialize with default missions
        if (missionsSnapshot.isEmpty) {
            Mission.defaultMissions.forEach { mission ->
                firestore.collection("goals")
                    .document(userId)
                    .collection("missions")
                    .document(mission.id)
                    .set(
                        mapOf(
                            "name" to mission.name,
                            "description" to mission.description,
                            "targetCount" to mission.targetCount,
                            "currentCount" to mission.currentCount,
                            "isCompleted" to mission.isCompleted,
                            "type" to mission.type
                        )
                    )
                    .await()
            }
        }
    }




}
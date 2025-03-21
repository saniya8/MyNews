package com.example.mynews.data

import com.example.mynews.domain.repositories.GoalsRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class GoalsRepositoryImpl(
    private val firestore: FirebaseFirestore
) : GoalsRepository {

    private data class Streak(
        val count: Int,
        val lastReadDate: String
    )

    override suspend fun logArticleRead(userId: String, articleId: String) {
        val timestamp = System.currentTimeMillis()
        firestore.collection("goals")
            .document(userId)
            .collection("activity")
            .document(articleId)
            .set(mapOf("timestamp" to timestamp))
            .await()
        updateStreak(userId)
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
}
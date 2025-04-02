package com.example.mynews.data

import android.util.Log
import com.example.mynews.domain.repositories.SettingsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class SettingsRepositoryImpl (
    private val firestore : FirebaseFirestore
): SettingsRepository {

    override suspend fun getNumWordsToSummarize(userId: String): Int? {
        try {
            val doc = firestore.collection("settings").document(userId).get().await()
            return doc.getLong("numWordsToSummarize")?.toInt()
        } catch (e: Exception) {
            Log.e("SettingsDebug", "Error fetching numWordsToSummarize: ${e.message}", e)
            return null // fallback will be handled in ViewModel
        }
    }

    override suspend fun updateNumWordsToSummarize(userId: String, newNumWords: Int) {
        try {
            val settingsDoc = firestore.collection("settings").document(userId)
            settingsDoc.set(mapOf("numWordsToSummarize" to newNumWords), SetOptions.merge()).await()
            Log.d("SettingsDebug", "Num words to summarize updated in Firestore to $newNumWords")
        } catch (e: Exception) {
            Log.e("SettingsDebug", "Failed to update num words to summarize: ${e.message}", e)
        }
    }


}
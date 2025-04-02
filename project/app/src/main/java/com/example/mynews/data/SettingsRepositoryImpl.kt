package com.example.mynews.data

import android.util.Log
import com.example.mynews.domain.repositories.SettingsRepository
import com.example.mynews.domain.repositories.SocialRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class SettingsRepositoryImpl (
    private val firestore : FirebaseFirestore
): SettingsRepository {

    override suspend fun getWordLimit(userId: String): Int? {
        try {
            val doc = firestore.collection("settings").document(userId).get().await()
            return doc.getLong("wordLimit")?.toInt()
        } catch (e: Exception) {
            Log.e("SettingsDebug", "Error fetching word limit: ${e.message}", e)
            return null // fallback will be handled in ViewModel
        }
    }

    override suspend fun updateWordLimit(userId: String, newLimit: Int) {
        try {
            val settingsDoc = firestore.collection("settings").document(userId)
            settingsDoc.set(mapOf("wordLimit" to newLimit), SetOptions.merge()).await()
            Log.d("SettingsDebug", "Word limit updated in Firestore to $newLimit")
        } catch (e: Exception) {
            Log.e("SettingsDebug", "Failed to update word limit: ${e.message}", e)
        }
    }


}
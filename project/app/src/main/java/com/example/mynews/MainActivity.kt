package com.example.mynews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.*
import com.example.mynews.ui.theme.*
import com.example.mynews.utils.*
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.auth.FirebaseAuth
import android.util.Log


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "Starting MainActivity")
        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.d("MainActivity", "Current user: ${currentUser?.email ?: "No user logged in"}")

        val isUserLoggedIn = currentUser != null

        setContent {
            MyNewsTheme {
                val navController = rememberNavController()
                    RootNavigationGraph(navController)
            }
        }
    }
}


/*
data class NewsArticle(
    val id: String,                   // Unique identifier
    val title: String,                // Article title
    val author: String?,              // Author name (nullable in case it's unknown)
    val source: String,               // News source (e.g., "BBC", "TechCrunch")
    val publishedAt: String,          // Publication date as a string (ISO 8601 format)
    val content: String,              // Full article content or a summary
    val url: String,                  // Link to the full article
    val imageUrl: String?,            // URL of the article image (nullable)
    val categories: List<String>?,    // List of categories or tags
    val isBookmarked: Boolean = false // Flag for user preferences

 */


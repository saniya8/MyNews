package com.example.mynews

import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.mynews.ui.theme.MyNewsTheme
import com.example.mynews.utils.*
import com.example.mynews.ui.theme.*
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
                /* PROBLEM:
                - false: registration works but when logged in only see home screen
                - true: registration bypassed but see the nav bar
                 */
                if (false/*isUserLoggedIn*/) { // TODO: I think the goal is to eventually have it function this way. Waiting on navbar component
                    HomeNavGraph(navController)
                } else {
                    RootNavigationGraph(navController)
                }
            }
        }
    }
}

/* Pre-creating login screen: this showed the nav bar of Social, Home, Goals, and clicking on
//the buttons changed the text on the screen to the corresponding button

//data class NewsArticle(
//    val id: String,                   // Unique identifier
//    val title: String,                // Article title
//    val author: String?,              // Author name (nullable in case it's unknown)
//    val source: String,               // News source (e.g., "BBC", "TechCrunch")
//    val publishedAt: String,          // Publication date as a string (ISO 8601 format)
//    val content: String,              // Full article content or a summary
//    val url: String,                  // Link to the full article
//    val imageUrl: String?,            // URL of the article image (nullable)
//    val categories: List<String>?,    // List of categories or tags
//    val isBookmarked: Boolean = false // Flag for user preferences
*/


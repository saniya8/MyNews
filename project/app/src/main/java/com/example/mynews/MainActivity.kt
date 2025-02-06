package com.example.mynews

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
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyNewsTheme {
                val navController = rememberNavController() // Create NavController

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomButtonBar(navController) } // Pass NavController
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen() }
                        composable("social") { SocialScreen() }
                        composable("goals") { GoalsScreen() }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomButtonBar(navController: NavHostController) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        actions = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = { navController.navigate("social") }) { Text("Social") }
                OutlinedButton(onClick = { navController.navigate("home") }) { Text("Home") }
                OutlinedButton(onClick = { navController.navigate("goals") }) { Text("Goals") }
            }
        }
    )
}

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Home Screen")
    }
}

@Composable
fun SocialScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Social")
    }
}

@Composable
fun GoalsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Goals")
    }
}

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Settings")
    }
}
// old
@Composable
fun GreetingPreview() {
    MyNewsTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomButtonBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("social") { SocialScreen() }
            composable("goals") { GoalsScreen() }
        }
    }
}
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

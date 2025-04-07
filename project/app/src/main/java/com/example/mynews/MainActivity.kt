package com.example.mynews

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.mynews.presentation.theme.MyNewsTheme
import com.example.mynews.utils.RootNavigationGraph
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint


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

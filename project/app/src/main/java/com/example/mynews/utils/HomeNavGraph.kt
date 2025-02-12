package com.example.mynews.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.google.firebase.firestore.FirebaseFirestore

import com.example.mynews.data.UserRepositoryImpl
import com.example.mynews.presentation.views.GoalsScreen
import com.example.mynews.presentation.views.SocialScreen
import com.example.mynews.presentation.views.home.HomeScreen

@Composable
// UNCOMMENT and deal with HomeScreen when HomeNavGraph gets called in HomeScreen.kt
fun HomeNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        route = Graph.HOME,
        startDestination = AppScreenRoutes.HomeScreen.route
    ) {
        composable(AppScreenRoutes.HomeScreen.route) {
//            HomeScreen()  // Assuming HomeScreen is a composable function that shows your home UI
            HomeScreen(
                onLogoutClicked = {
                    // do nothing for now
                },
                onGoalsClicked = {
                    println("Goals Button Clicked from Home Nav")
                    navController.navigate(AppScreenRoutes.GoalsScreen.route)
                },
                onSocialClicked = {
                    println("Social Button Clicked from Home Nav")
                    navController.navigate(AppScreenRoutes.SocialScreen.route)
                }
            )
        }
        composable(AppScreenRoutes.GoalsScreen.route) {
            GoalsScreen()
        }
        composable(AppScreenRoutes.SocialScreen.route) {
            SocialScreen()
        }
    }
}

sealed class AppScreenRoutes(val route: String) {
    object HomeScreen : AppScreenRoutes("home_screen")
    object GoalsScreen : AppScreenRoutes("goals_screen")
    object SocialScreen : AppScreenRoutes("social_screen")
    // Remove other routes if they are not needed
}

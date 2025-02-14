package com.example.mynews.utils

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mynews.data.UserRepositoryImpl
import com.example.mynews.presentation.views.goals.GoalsScreen
import com.example.mynews.presentation.views.social.SocialScreen
import com.example.mynews.presentation.views.home.HomeScreen
// added for navbar
import com.example.mynews.presentation.views.settings.SettingsScreen


sealed class AppScreenRoutes(val route: String) {
    object HomeScreen : AppScreenRoutes("home_screen")
    object GoalsScreen : AppScreenRoutes("goals_screen")
    object SocialScreen : AppScreenRoutes("social_screen")
    object SettingsScreen: AppScreenRoutes("settings_screen")
    // Remove other routes if they are not needed
}


// HomeNavGraph - pre-attempt to fix the sign in and navigation
@Composable

fun HomeNavGraph(rootNavController: NavHostController, navController: NavHostController) {

    NavHost(
        navController = navController,
        route = Graph.HOME,
        startDestination = AppScreenRoutes.HomeScreen.route
    ) {
        composable(AppScreenRoutes.HomeScreen.route) {
            HomeScreen()
        }
        composable(AppScreenRoutes.GoalsScreen.route) {
            GoalsScreen(streakDays = 4, achievements = listOf())
        }

        composable(AppScreenRoutes.SocialScreen.route) {
            SocialScreen()
        }

        composable(AppScreenRoutes.SettingsScreen.route) {
            SettingsScreen(
                navController = navController,
                onNavigateToAuthScreen = {
                    Log.d("LogoutDebug", "Navigating to Auth Screen")
                    rootNavController.navigate(Graph.AUTHENTICATION){
                        popUpTo(Graph.ROOT) { inclusive = true}
                    }
                },
                userRepository = UserRepositoryImpl(FirebaseFirestore.getInstance()),
            )
        }

    }
}
package com.example.mynews.utils

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mynews.data.UserRepositoryImpl
import com.example.mynews.presentation.views.Achievement
import com.example.mynews.presentation.views.GoalsScreen
import com.example.mynews.presentation.views.SocialScreen
import com.example.mynews.presentation.views.home.HomeScreen
// added for navbar
import com.example.mynews.utils.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import com.example.mynews.presentation.views.home.SettingsScreen


sealed class AppScreenRoutes(val route: String) {
    object HomeScreen : AppScreenRoutes("home_screen")
    object GoalsScreen : AppScreenRoutes("goals_screen")
    object SocialScreen : AppScreenRoutes("social_screen")
    object SettingsScreen: AppScreenRoutes("settings_screen")
    // Remove other routes if they are not needed
}


// HomeNavGraph - pre-attempt to fix the sign in and navigation
@Composable
//fun HomeNavGraph(navController: NavHostController) {

//FIX
fun HomeNavGraph(rootNavController: NavHostController, navController: NavHostController) {



    /*
    Scaffold(
        bottomBar = { BottomNavBar(navController) }  // Add BottomNavBar here
    ) { paddingValues ->
        NavHost(
            navController = navController,
            route = Graph.HOME,
            startDestination = AppScreenRoutes.HomeScreen.route,
            modifier = Modifier.padding(paddingValues)  // Add padding to avoid overlap
        ) {
            composable(AppScreenRoutes.HomeScreen.route) {
                HomeScreen(
                    onLogoutClicked = { /* Handle logout */ }
                )
            }
            composable(AppScreenRoutes.GoalsScreen.route) {
                GoalsScreen(streakDays = 5, achievements = listOf())
            }
            composable(AppScreenRoutes.SocialScreen.route) {
                SocialScreen()
            }
        }
    }

     */

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

                    // FIX
                    rootNavController.navigate(Graph.AUTHENTICATION){
                        popUpTo(Graph.ROOT) { inclusive = true}
                    }

                    //navController.navigate(Graph.AUTHENTICATION){
                    //    popUpTo(Graph.ROOT) { inclusive = true}
                    //}
                },
                userRepository = UserRepositoryImpl(FirebaseFirestore.getInstance()),
            )
        }

    }
}


//@Composable
// UNCOMMENT and deal with HomeScreen when HomeNavGraph gets called in HomeScreen.kt
//fun HomeNavGraph(navController: NavHostController) {
//    NavHost(
//        navController = navController,
//        route = Graph.HOME,
//        startDestination = AppScreenRoutes.HomeScreen.route
//    ) {
//        composable(AppScreenRoutes.HomeScreen.route) {
////            HomeScreen()  // Assuming HomeScreen is a composable function that shows your home UI
//            HomeScreen(
//                onLogoutClicked = {
//                    // do nothing for now
//                },
//                onGoalsClicked = {
//                    println("Goals Button Clicked from Home Nav")
//                    navController.navigate(AppScreenRoutes.GoalsScreen.route)
//                },
//                onSocialClicked = {
//                    println("Social Button Clicked from Home Nav")
//                    navController.navigate(AppScreenRoutes.SocialScreen.route)
//                }
//            )
//        }
//        composable(AppScreenRoutes.GoalsScreen.route) {
//            GoalsScreen()
//        }
//        composable(AppScreenRoutes.SocialScreen.route) {
//            SocialScreen()
//        }
//    }
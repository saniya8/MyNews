package com.example.mynews.utils

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mynews.data.UserRepositoryImpl
import com.example.mynews.presentation.viewmodel.home.CondensedNewsArticleViewModel
import com.example.mynews.presentation.viewmodel.home.HomeViewModel
import com.example.mynews.presentation.viewmodel.social.FriendsViewModel
import com.example.mynews.presentation.viewmodel.home.NewsViewModel
import com.example.mynews.presentation.viewmodel.home.SavedArticlesViewModel
import com.example.mynews.presentation.views.goals.GoalsScreen
import com.example.mynews.presentation.views.social.FriendsScreen
import com.example.mynews.presentation.views.home.HomeScreen
import com.example.mynews.presentation.views.home.NewsArticleScreen
import com.example.mynews.presentation.views.home.CondensedNewsArticleScreen
// added for navbar
import com.example.mynews.presentation.views.settings.SettingsScreen
import com.example.mynews.presentation.views.home.SavedArticlesScreen
import com.example.mynews.presentation.views.social.SocialScreen


sealed class AppScreenRoutes(val route: String) {
    // Main Routes
    object HomeScreen : AppScreenRoutes("home_screen")
    object GoalsScreen : AppScreenRoutes("goals_screen")
    object SocialScreen : AppScreenRoutes("social_screen")
    object SettingsScreen: AppScreenRoutes("settings_screen")
    // Additional Routes
    //object NewsArticleScreen : AppScreenRoutes("news_article_screen")
    object NewsArticleScreen : AppScreenRoutes("news_article_screen/{articleUrl}/{origin}") {
        fun createRoute(articleUrl: String, origin: String) = "news_article_screen/$articleUrl/$origin"
    }

    object CondensedNewsArticleScreen : AppScreenRoutes("condensed_news_article_screen/{articleUrl}") {
        fun createRoute(articleUrl: String) = "condensed_news_article_screen/$articleUrl"
    }

    object SavedArticlesScreen : AppScreenRoutes("saved_articles_screen")

    object FriendsScreen: AppScreenRoutes("friends_screen")




}

@Composable

fun HomeNavGraph(rootNavController: NavHostController,
                 navController: NavHostController,
                 homeViewModel: HomeViewModel,
                 newsViewModel: NewsViewModel,
                 savedArticlesViewModel: SavedArticlesViewModel,
                 friendsViewModel: FriendsViewModel,
                 condensedNewsArticleViewModel: CondensedNewsArticleViewModel,
                 selectedCategory: MutableState<String?>,
                 searchQuery: MutableState<String>) {

    NavHost(
        navController = navController,
        route = Graph.HOME,
        startDestination = AppScreenRoutes.HomeScreen.route
    ) {
        composable(AppScreenRoutes.HomeScreen.route) {
            HomeScreen(navController = navController,
                       homeViewModel = homeViewModel,
                       newsViewModel = newsViewModel,
                       savedArticlesViewModel = savedArticlesViewModel,
                       selectedCategory = selectedCategory,
                       searchQuery = searchQuery)
        }
        composable(AppScreenRoutes.GoalsScreen.route) {
            GoalsScreen(navController = navController,
                        streakDays = 4,
                        achievements = listOf())
        }

        composable(AppScreenRoutes.SocialScreen.route) {
            SocialScreen(navController = navController,)
        }

        composable(route = AppScreenRoutes.NewsArticleScreen.route,
                   arguments = listOf(navArgument("articleUrl") { type = NavType.StringType },
                                      navArgument("origin") { type = NavType.StringType }
                   )
        ) { backStackEntry ->
            val articleUrl = backStackEntry.arguments?.getString("articleUrl") ?: ""
            val origin = backStackEntry.arguments?.getString("origin") ?: AppScreenRoutes.HomeScreen.route // default to home
            NewsArticleScreen(navController = navController,
                              articleUrl = articleUrl,
                              origin = origin)
        }

        composable(
            route = AppScreenRoutes.CondensedNewsArticleScreen.route,
            arguments = listOf(navArgument("articleUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleUrl = backStackEntry.arguments?.getString("articleUrl") ?: ""
            CondensedNewsArticleScreen(navController = navController,
                                       condensedNewsArticleViewModel = condensedNewsArticleViewModel,
                                       articleUrl = articleUrl)
        }

        composable(AppScreenRoutes.SavedArticlesScreen.route) {
            SavedArticlesScreen(navController = navController,
                                newsViewModel = newsViewModel,
                                savedArticlesViewModel = savedArticlesViewModel,)
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

        composable(AppScreenRoutes.FriendsScreen.route) {
            FriendsScreen(navController = navController,
                          friendsViewModel = friendsViewModel)
        }

    }
}
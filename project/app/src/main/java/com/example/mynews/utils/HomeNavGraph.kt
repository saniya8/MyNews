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
import com.example.mynews.data.api.Article
import com.example.mynews.presentation.viewmodel.NewsViewModel
import com.example.mynews.presentation.viewmodel.SavedArticlesViewModel
import com.example.mynews.presentation.views.goals.GoalsScreen
import com.example.mynews.presentation.views.social.SocialScreen
import com.example.mynews.presentation.views.home.HomeScreen
import com.example.mynews.presentation.views.home.NewsArticleScreen
import com.example.mynews.presentation.views.home.CondensedNewsArticleScreen
// added for navbar
import com.example.mynews.presentation.views.settings.SettingsScreen
import com.example.mynews.presentation.views.home.SavedArticlesScreen
import kotlinx.serialization.json.Json


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

    object CondensedNewsArticleScreen : AppScreenRoutes("condensed_news_article_screen/{articleContent}") {
        fun createRoute(articleContent: String) = "condensed_news_article_screen/$articleContent"
    }

    object SavedArticlesScreen : AppScreenRoutes("saved_articles_screen")




}


// HomeNavGraph - pre-attempt to fix the sign in and navigation
@Composable

fun HomeNavGraph(rootNavController: NavHostController,
                 navController: NavHostController,
                 newsViewModel: NewsViewModel,
                 savedArticlesViewModel: SavedArticlesViewModel,
                 selectedCategory: MutableState<String?>,
                 searchQuery: MutableState<String>) {

    NavHost(
        navController = navController,
        route = Graph.HOME,
        startDestination = AppScreenRoutes.HomeScreen.route
    ) {
        composable(AppScreenRoutes.HomeScreen.route) {
            HomeScreen(navController = navController,
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
            SocialScreen(navController = navController)
        }

        //composable(AppScreenRoutes.NewsArticleScreen.route) {
        //    NewsArticleScreen(navController = navController)
        //}

        composable(route = AppScreenRoutes.NewsArticleScreen.route,
                   arguments = listOf(navArgument("articleUrl") { type = NavType.StringType },
                                      navArgument("origin") { type = NavType.StringType }
                   )
        ) { backStackEntry ->
            val articleUrl = backStackEntry.arguments?.getString("articleUrl") ?: ""
            val origin = backStackEntry.arguments?.getString("origin") ?: AppScreenRoutes.HomeScreen.route // default to home
            NewsArticleScreen(navController = navController, articleUrl = articleUrl, origin = origin)
        }


        //composable("${AppScreenRoutes.NewsArticleScreen.route}/{article}") { backStackEntry ->
         //   val jsonArticle = backStackEntry.arguments?.getString("article") ?: ""
           // if (jsonArticle.isNullOrEmpty()) {
            //    Log.e("Serialization Debug", "Received null or empty article JSON")
           //     return@composable
            //}
        //    val article = Json.decodeFromString<Article>(jsonArticle) // Convert JSON back to object
        //    NewsArticleScreen(navController = navController, article = article)
        //}

        composable(
            route = AppScreenRoutes.CondensedNewsArticleScreen.route,
            arguments = listOf(navArgument("articleContent") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleContent = backStackEntry.arguments?.getString("articleContent") ?: ""
            CondensedNewsArticleScreen(navController = navController, articleContent = articleContent)
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

    }
}
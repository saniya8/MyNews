package com.example.mynews.presentation.views.general

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavHostController
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mynews.utils.AuthScreen
import com.example.mynews.utils.AppScreenRoutes
import com.example.mynews.presentation.components.BottomNavBar
import com.example.mynews.domain.model.BottomNavBarItem
import com.example.mynews.presentation.viewmodel.goals.GoalsViewModel
import com.example.mynews.presentation.viewmodel.home.CondensedNewsArticleViewModel
import com.example.mynews.presentation.viewmodel.home.HomeViewModel
import com.example.mynews.presentation.viewmodel.social.FriendsViewModel
import com.example.mynews.utils.HomeNavGraph
import com.example.mynews.presentation.viewmodel.home.NewsViewModel
import com.example.mynews.presentation.viewmodel.home.SavedArticlesViewModel
import com.example.mynews.presentation.viewmodel.settings.SettingsViewModel
import com.example.mynews.presentation.viewmodel.social.SocialViewModel


@Composable

fun MainScreen(rootNavController: NavHostController,
               homeViewModel: HomeViewModel = hiltViewModel(),
               newsViewModel: NewsViewModel = hiltViewModel(), // newsViewModel instantiated here to have one instance while app running
               savedArticlesViewModel: SavedArticlesViewModel = hiltViewModel(),
               friendsViewModel: FriendsViewModel = hiltViewModel(),
               condensedNewsArticleViewModel: CondensedNewsArticleViewModel = hiltViewModel(),
               settingsViewModel: SettingsViewModel = hiltViewModel(),
               socialViewModel: SocialViewModel = hiltViewModel(),
               goalsViewModel: GoalsViewModel = hiltViewModel(),

               ) {

    val navController = rememberNavController() // local controller for bottom nav

    val selectedCategory = rememberSaveable { mutableStateOf<String?>(null) } // Track selected category
    val searchQuery = rememberSaveable { mutableStateOf("")} // Track search query

    // selectedCategory can be null if a user deselects, in which case it should fetch
    // with just the language parameter and not the category parameter
    // use rememberSaveable instead of remember for selectedCategory since rememberSaveable stores
    // state in bundle so it survives configuration changes (like navigation) so even if user
    // navigates to different tabs, their filtering remains

    Scaffold(
        bottomBar = {
            if (currentRoute(navController) != AuthScreen.Login.route &&
                currentRoute(navController) != AuthScreen.Login.route) {
                BottomNavBar(
                    items = listOf(
                        BottomNavBarItem(
                            name = "Goals",
                            route = AppScreenRoutes.GoalsScreen.route,
                            icon = Icons.Default.Lightbulb
                        ),
                        BottomNavBarItem(
                            name = "Home",
                            route = AppScreenRoutes.HomeScreen.route,
                            icon = Icons.Default.Home
                        ),
                        BottomNavBarItem(
                            name = "Social",
                            route = AppScreenRoutes.SocialScreen.route,
                            icon = Icons.Default.Groups
                        ),
                        BottomNavBarItem(
                            name = "Settings",
                            route = AppScreenRoutes.SettingsScreen.route,
                            icon = Icons.Default.Settings
                        ),

                    ),
                    navController = navController,

                    // onItemClick below ensures screens persist between navigation
                    onItemClick = {
                        navController.navigate(it.route) {
                            launchSingleTop = true
                            restoreState = true // Restores previous state when returning to tab
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true // Saves previous screen state
                            }
                        }
                    }



                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = (Modifier.padding(bottom = paddingValues.calculateBottomPadding()))
        ) {
            HomeNavGraph(rootNavController = rootNavController,
                         navController = navController,
                         homeViewModel = homeViewModel,
                         newsViewModel = newsViewModel,
                         savedArticlesViewModel = savedArticlesViewModel,
                         friendsViewModel = friendsViewModel,
                         condensedNewsArticleViewModel = condensedNewsArticleViewModel,
                         settingsViewModel = settingsViewModel,
                         socialViewModel = socialViewModel,
                         goalsViewModel = goalsViewModel,
                         selectedCategory = selectedCategory,
                         searchQuery = searchQuery)
        }
    }
}


// currentRoute only affects the bottom navigation UI (ie which tab is highlighted), NOT
// the actual navigation behaviour of which screen is shown -> that is in HomeNavGraph

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}


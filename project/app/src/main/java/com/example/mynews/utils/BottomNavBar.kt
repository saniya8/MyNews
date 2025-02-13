package com.example.mynews.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Text

@Composable
fun BottomNavBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    BottomNavigation {
        BottomNavigationItem(
            icon = {},
            label = { Text("Home") },
            selected = currentRoute == AppScreenRoutes.HomeScreen.route,
            onClick = {
                if (currentRoute != AppScreenRoutes.HomeScreen.route) {
                    navController.navigate(AppScreenRoutes.HomeScreen.route) {
                        popUpTo(AppScreenRoutes.HomeScreen.route) { inclusive = true }
                    }
                }
            }
        )
        BottomNavigationItem(
            icon = {},
            label = { Text("Goals") },
            selected = currentRoute == AppScreenRoutes.GoalsScreen.route,
            onClick = {
                if (currentRoute != AppScreenRoutes.GoalsScreen.route) {
                    navController.navigate(AppScreenRoutes.GoalsScreen.route) {
                        popUpTo(AppScreenRoutes.HomeScreen.route)
                    }
                }
            }
        )
        BottomNavigationItem(
            icon = {},
            label = { Text("Social") },
            selected = currentRoute == AppScreenRoutes.SocialScreen.route,
            onClick = {
                if (currentRoute != AppScreenRoutes.SocialScreen.route) {
                    navController.navigate(AppScreenRoutes.SocialScreen.route) {
                        popUpTo(AppScreenRoutes.HomeScreen.route)
                    }
                }
            }
        )
    }
}

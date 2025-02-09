package com.example.mynews.utils
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mynews.presentation.views.authentication.LoginScreen
import com.example.mynews.presentation.views.authentication.RegisterScreen
import com.example.mynews.data.UserRepositoryImpl


fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        route = Graph.AUTHENTICATION,
        startDestination = AuthScreen.Login.route
    ) {
        composable(route = AuthScreen.Login.route){
            BackHandler(true) {
                // Or do nothing
                Log.i("LOG_TAG", "Clicked back")
            }
            LoginScreen(
                onLoginSuccessNavigation = {
                    //navController.popBackStack()
                    //navController.navigate(Graph.HOME)
                    navController.navigate(Graph.HOME) {
                        popUpTo(Graph.AUTHENTICATION) { inclusive = true }
                    }
                },
                onNavigateToRegisterScreen = {
                    navController.navigate(AuthScreen.Register.route){
                        popUpTo(0)
                    }
                }
            )
        }
        composable(AuthScreen.Register.route){
            RegisterScreen(
                onRegisterSuccessNavigation = {
                    //navController.popBackStack()
                    //navController.navigate(Graph.HOME)
                    navController.navigate(Graph.HOME) {
                        popUpTo(Graph.AUTHENTICATION) { inclusive = true }
                    }
                },
                onNavigateToLoginScreen = {
                    navController.navigate(AuthScreen.Login.route){
                        popUpTo(0)
                    }
                }
            )
        }
    }
}

sealed class AuthScreen(val route:String){
    object Login:AuthScreen("login_screen")
    object Register:AuthScreen("register_screen")
}



/*

OLD CODE:

// Will later deal with creating multiple nav graphs
// For now, keeping code simple and just dealing with authentication navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mynews.presentation.views.authentication.FinalDestination
import com.example.mynews.presentation.views.authentication.LoginScreen
import com.example.mynews.presentation.views.authentication.RegisterScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.LoginScreen.route
    ){
        composable(ScreenRoutes.LoginScreen.route){
            LoginScreen(
                onLoginSuccessNavigation = {
                    navController.navigate(ScreenRoutes.FinalDestination.route){
                        popUpTo(0)
                    }
                },
                onNavigateToRegisterScreen = {
                    navController.navigate(ScreenRoutes.RegisterScreen.route){
                        popUpTo(0)
                    }
                }
            )
        }
        composable(ScreenRoutes.RegisterScreen.route){
            RegisterScreen(
                onRegisterSuccessNavigation = {
                    navController.navigate(ScreenRoutes.FinalDestination.route){
                        popUpTo(0)
                    }
                },
                onNavigateToLoginScreen = {
                    navController.navigate(ScreenRoutes.LoginScreen.route){
                        popUpTo(0)
                    }
                }
            )
        }
        composable(ScreenRoutes.FinalDestination.route){
            FinalDestination()
        }
    }

}

sealed class ScreenRoutes(val route:String){
    object LoginScreen:ScreenRoutes("login_screen")
    object RegisterScreen:ScreenRoutes("register_screen")
    object FinalDestination:ScreenRoutes("final_destination")
}


 */
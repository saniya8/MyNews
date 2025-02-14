package com.example.mynews.utils
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
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

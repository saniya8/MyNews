package com.example.mynews.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mynews.data.AuthRepositoryImpl
import com.example.mynews.data.UserRepositoryImpl
import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.repositories.UserRepository
import android.util.Log
import com.example.mynews.presentation.views.home.MainScreen

// RootNavGraph is the main navigation that manages overall navigation of the app
// based on if the user is or isn't logged in
// - AuthenticationNavGraph manages the set of navigations when the user isn't logged in (is
//   trying to log in or register)
// - HomeNavGraph manages the set of navigations when the user is logged into the app

@Composable
fun RootNavigationGraph(navController: NavHostController) {

    val firestore = FirebaseFirestore.getInstance()

    val authRepository: AuthRepository
    val userRepository: UserRepository

    userRepository = UserRepositoryImpl(firestore)

    authRepository = AuthRepositoryImpl(
        UserRepositoryImpl(firestore),  // Replace with actual implementation
        firestore,
        FirebaseAuth.getInstance()
    )

    // Remember the logged-in state
    val loggedInState = remember { mutableStateOf<Boolean?>(null) }

    // Use LaunchedEffect to trigger the login state check
    LaunchedEffect(true) {
        // Check the login state and update the logged-in state
        loggedInState.value = authRepository.getLoginState()
    }

    val startDestination = when {
        loggedInState.value == true -> Graph.HOME
        else -> Graph.AUTHENTICATION
    }


    Log.d("MainActivity", "RootNavGraph: loggedInState value is ${loggedInState.value}")

    if (loggedInState.value == null) {
        Log.d("MainActivity", "RootNavGraph: loggedInState value is null")
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column (
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    } else {
        NavHost(
            navController = navController,
            route = Graph.ROOT,
            startDestination = startDestination
        ) {
            authNavGraph(navController = navController)
            composable(route = Graph.HOME) {

                MainScreen(
                    rootNavController = navController, // FIX
                    onLogoutClicked = {

                        FirebaseAuth.getInstance().signOut()
                        loggedInState.value = false

                        navController.navigate(Graph.AUTHENTICATION) {
                            popUpTo(Graph.ROOT) {inclusive = true}
                        }
                    }
                )
            }
        }
    }
}

object Graph {
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "auth_graph"
    const val HOME = "home_graph"
    const val GOALS = "goals_graph"
    const val SOCIAL = "social_graph"
    const val SETTINGS = "settings_graph"
}
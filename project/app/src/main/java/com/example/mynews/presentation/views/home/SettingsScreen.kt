package com.example.mynews.presentation.views.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mynews.presentation.viewmodel.SettingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.example.mynews.utils.Graph
import com.example.mynews.ui.theme.*
import android.util.Log


import kotlinx.coroutines.launch

import com.example.mynews.domain.repositories.UserRepository

@Composable
fun SettingsScreen(
    navController: NavHostController /*= rememberNavController()*/,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToAuthScreen: () -> Unit,
    userRepository: UserRepository,
) {
    val logoutState by settingsViewModel.logoutState.collectAsState()

    // Navigate to authentication screen when logged out
    //if (logoutState == true) {
    //    onNavigateToAuthScreen()
    //    settingsViewModel.resetLogoutState() // Reset state after navigating
    //}


/*
// Navigate to authentication screen when logged out
    LaunchedEffect(logoutState) {
        Log.d("LogoutDebug", "LaunchedEffect triggered with logoutState: $logoutState")
        if (logoutState == true) {
            Log.d("LogoutDebug", "Navigating to auth screen...")
            onNavigateToAuthScreen()
            settingsViewModel.resetLogoutState() // Reset state after navigating
        }
    }

 */



    Scaffold(
        //topBar = {
        //    TopAppBar(title = { Text("Settings") })
        //}
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            //Text("Settings", style = MaterialTheme.typography.h5)
            //Spacer(modifier = Modifier.height(20.dp))

            // Logout Button
            Button(
                onClick = {

                    Log.d("LogoutDebug", "Before clicking, logoutState: $logoutState")

                    settingsViewModel.logout() // UNCOMMENT THIS AFTER

                    Log.d("LogoutDebug", "After clicking, logoutState: $logoutState")

                   //FirebaseAuth.getInstance().signOut() // UNCOMMENT THIS AFTER OR UNCOMMENT THE ABOVE

                    Log.d("NavDebug", "Current destination: ${navController.currentDestination?.route}")
                    Log.d("NavDebug", "Logout button clicked. Attempting navigation...")
                    Log.d("NavDebug", "Attempting to navigate to: ${Graph.AUTHENTICATION}")

                    onNavigateToAuthScreen() // UNCOMMENT THIS IF LAUNCHED EFFECT DOESNT WORK - will test launched effect later

                    //navController.popBackStack(Graph.ROOT, inclusive = true)
                    //navController.navigate(Graph.AUTHENTICATION)

                    //navController.navigate(Graph.AUTHENTICATION) //{
                        //popUpTo(Graph.ROOT) { inclusive = true }
                        //popUpTo(0)
                        //launchSingleTop = true
                    //}






                   // onNavigateToAuthScreen()

                    /*
                    onLogoutClicked = {

                        FirebaseAuth.getInstance().signOut()
                        loggedInState.value = false

                        //navController.navigate("login_route") {
                        //    popUpTo("home_route") { inclusive = true }
                        //}
                        navController.navigate(Graph.AUTHENTICATION) {
                            popUpTo(Graph.ROOT) {inclusive = true}
                        }
                    }

                     */


                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = NavyBlue,
                    contentColor = Color.White,
                )

            ) {
                Text("Log Out")
            }
        }
    }
}
package com.example.mynews.presentation.views.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.mynews.presentation.viewmodel.SettingsViewModel
import com.example.mynews.utils.Graph
import com.example.mynews.ui.theme.*
import android.util.Log
import com.example.mynews.domain.repositories.UserRepository

@Composable
fun SettingsScreen(
    navController: NavHostController /*= rememberNavController()*/,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToAuthScreen: () -> Unit,
    userRepository: UserRepository,
) {
    val logoutState by settingsViewModel.logoutState.collectAsState()

    /*
    // Navigate to authentication screen when logged out
    // TO DO: Test
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {

            // Logout Button
            Button(
                onClick = {

                    Log.d("LogoutDebug", "Before clicking, logoutState: $logoutState")

                    settingsViewModel.logout()

                    Log.d("LogoutDebug", "After clicking, logoutState: $logoutState")
                    Log.d("NavDebug", "Current destination: ${navController.currentDestination?.route}")
                    Log.d("NavDebug", "Logout button clicked. Attempting navigation...")
                    Log.d("NavDebug", "Attempting to navigate to: ${Graph.AUTHENTICATION}")

                    //FirebaseAuth.getInstance().signOut()
                    onNavigateToAuthScreen()

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
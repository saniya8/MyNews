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
import com.example.mynews.presentation.viewmodel.settings.SettingsViewModel
import com.example.mynews.utils.Graph
import com.example.mynews.ui.theme.*
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.mynews.domain.repositories.UserRepository
import com.example.mynews.presentation.views.social.AddedFriendsList
import com.example.mynews.presentation.views.social.FriendsSearchBar
import com.example.mynews.presentation.views.social.ReactionItem
import com.example.mynews.utils.AppScreenRoutes

@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToAuthScreen: () -> Unit,
    userRepository: UserRepository,
) {
    val logoutState by settingsViewModel.logoutState.collectAsState()
    val username by settingsViewModel.username.collectAsState()

    LaunchedEffect(Unit) {
        val userID = userRepository.getCurrentUserId()
        settingsViewModel.getUsername(userID!!)
    }

    // very basic just logout
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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


            Spacer(modifier = Modifier.height(16.dp)) // Space between button and text

            // Username Text
            Text(
                text = "Username: ${username ?: "Loading..."}", // Show username or fallback
                fontSize = 24.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )


        }
    }




}
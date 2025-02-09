package com.example.mynews.presentation.views.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import com.google.firebase.auth.FirebaseAuth
import com.example.mynews.presentation.viewmodel.SettingsViewModel


@Composable
fun HomeScreen(
    onLogoutClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ){


        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                style = MaterialTheme.typography.body1,
                text = "Congratulations"
            )
            /* eventually, put this button in SettingsScreen.kt,
            - For now leaving it here to be able to log out of app once logged in
            - onLogoutClick passed as lambda function, and this HomeScreen function
              is called in RootNavGraph.kt
            */
            Button(onClick = { onLogoutClicked() }) {
                Text("Logout")
            }
        }
    }
}
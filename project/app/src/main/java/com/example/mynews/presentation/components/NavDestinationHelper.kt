package com.example.mynews.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

// Purpose: used to manage navigation between LoginScreen, RegisterScreen, and HomeScreen

@Composable
fun NavDestinationHelper(
    shouldNavigate:()->Boolean,
    destination:()->Unit
) {
    LaunchedEffect(key1 = shouldNavigate()){
        if(shouldNavigate()){
            destination()
        }
    }
}
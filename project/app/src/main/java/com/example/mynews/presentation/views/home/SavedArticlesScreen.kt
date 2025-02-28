package com.example.mynews.presentation.views.home

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.navigation.NavHostController
import com.example.mynews.data.api.Article
import com.example.mynews.utils.AppScreenRoutes

@Composable
fun SavedArticlesScreen(
    navController: NavHostController,
    //article: Article
){

    Box (
        // User can swipe left to right to return back to the home screen
        modifier = Modifier
            .fillMaxSize()

            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) { // Detect swipe right to go back
                        //navController.popBackStack()
                        navController.popBackStack(AppScreenRoutes.HomeScreen.route, false)
                    }
                }
            }

    ) {

        // Simple column layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Padding around the entire screen
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display the article content received as a parameter
            Text(
                text = "On saved article screen",
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                modifier = Modifier.padding(8.dp), // Padding around the text
            )
        }
    }





}
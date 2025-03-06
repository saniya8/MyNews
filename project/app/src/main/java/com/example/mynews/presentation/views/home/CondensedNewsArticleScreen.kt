package com.example.mynews.presentation.views.home

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavHostController
import com.example.mynews.presentation.viewmodel.home.CondensedNewsArticleViewModel
import com.example.mynews.utils.AppScreenRoutes

@Composable
fun CondensedNewsArticleScreen(
    navController: NavHostController,
    condensedNewsArticleViewModel: CondensedNewsArticleViewModel,
    articleUrl: String
) {
    val articleText by condensedNewsArticleViewModel.articleText.collectAsState()
    val summarizedText by condensedNewsArticleViewModel.summarizedText.collectAsState()

    // Reset state when the articleUrl changes
    LaunchedEffect(articleUrl) {
        condensedNewsArticleViewModel.fetchArticleText(articleUrl)
    }

    // Recalculate summarizedText once articleText is available
    LaunchedEffect(articleText) {
        if (articleText.isNotEmpty()) {
            condensedNewsArticleViewModel.fetchSummarizedText(articleText, 200)
        }
    }

    // User can swipe left to right to return back to the home screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) { // Detect swipe right to go back
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
            // Display the summarizedText
            Text(
                text = summarizedText,
                style = androidx.compose.ui.text.TextStyle(
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                modifier = Modifier.padding(8.dp), // Padding around the text
            )
        }
    }
}

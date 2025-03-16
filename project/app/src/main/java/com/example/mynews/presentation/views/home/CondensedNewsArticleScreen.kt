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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mynews.presentation.viewmodel.home.CondensedNewsArticleViewModel
import com.example.mynews.utils.AppScreenRoutes

@Composable
fun CondensedNewsArticleScreen(
    navController: NavHostController,
    condensedNewsArticleViewModel: CondensedNewsArticleViewModel,
    articleUrl: String,
    articleTitle: String
) {
    val articleText by condensedNewsArticleViewModel.articleText.collectAsState()
    val summarizedText by condensedNewsArticleViewModel.summarizedText.collectAsState()
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(articleUrl) {
        condensedNewsArticleViewModel.clearCondensedArticleState().also {
            condensedNewsArticleViewModel.fetchArticleText(articleUrl)
        }
    }

    LaunchedEffect(articleText) {
        if (articleText.isNotEmpty()) {
            condensedNewsArticleViewModel.fetchSummarizedText(articleText, 200)
        }
    }

    LaunchedEffect(summarizedText) {
        val errorMessages = listOf(
            "Failed to extract meaningful content from the article.",
            "No article content found in Diffbot response"
        )

        if (summarizedText in errorMessages || summarizedText.startsWith("Error:")) {
            showErrorDialog = true
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                navController.popBackStack() // Navigate back when dismissed
            },
            confirmButton = {
                TextButton(onClick = {
                    showErrorDialog = false
                    navController.popBackStack()
                }) {
                    Text("OK")
                }
            },
            title = { Text("Condensed Article Unavailable") },
            text = { Text("A condensed article is unavailable for this article.") }
        )
    }

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
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                // Header section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp) // Adjust spacing
                ) {
                    Text(
                        text = articleTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 25.sp,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Condensed article content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp), // Padding around the content
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Display summarizedText
                    Text(
                        text = summarizedText,
                        style = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.Normal,
                            color = Color.Black,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.padding(8.dp) // Padding around the text
                    )
                }
            }
        }
    }
}

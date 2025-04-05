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
import com.example.mynews.presentation.components.LoadingIndicator
import com.example.mynews.presentation.viewmodel.home.CondensedNewsArticleViewModel
import com.example.mynews.presentation.viewmodel.settings.SettingsViewModel
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
    val currentArticleUrl by condensedNewsArticleViewModel.currentArticleUrl.collectAsState()

    var wasNavigatedBack by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(articleUrl) {
        condensedNewsArticleViewModel.fetchArticleText(articleUrl)
    }

    LaunchedEffect(articleText, currentArticleUrl) {
        val articleErrorMessages = listOf(
            "Failed to extract meaningful content from the article",
            "No article content found in Diffbot response",
            "No summary generated from Huggingface",
            "Invalid summary containing 'CNN'",
        )
        if (!wasNavigatedBack && (
                    articleText in articleErrorMessages ||
                    articleText.startsWith("Error:") ||
                    articleTitle.contains("Bloomberg.com", ignoreCase = true))
        ){
            condensedNewsArticleViewModel.clearArticleText()
            condensedNewsArticleViewModel.clearSummarizedText()
            showErrorDialog = true
        } else if (articleText.isNotEmpty() && currentArticleUrl == articleUrl) {
            condensedNewsArticleViewModel.fetchSummarizedText(
                url = articleUrl,
                text = articleText,
            )
        }
    }

    LaunchedEffect(summarizedText) {
        val errorMessages = listOf(
            "Failed to extract meaningful content from the article",
            "No article content found in Diffbot response",
            "No summary generated from Huggingface",
            "Invalid summary containing 'CNN'",
        )
        if (!wasNavigatedBack && (
                    summarizedText in errorMessages ||
                    summarizedText.startsWith("Error:")
           )
        ){
            condensedNewsArticleViewModel.clearSummarizedText() // summarized text will show the error message, clear it
            showErrorDialog = true
        }
    }

    // clear state when leaving screen
    DisposableEffect(navController.currentBackStackEntry) {
        onDispose {
            condensedNewsArticleViewModel.clearCondensedArticleState()
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                wasNavigatedBack = true // to prevent relaunch of alert dialog
                navController.popBackStack() // Navigate back when dismissed
            },
            confirmButton = {
                TextButton(onClick = {
                    showErrorDialog = false
                    wasNavigatedBack = true // to prevent relaunch of alert dialog
                    navController.popBackStack()
                }) {
                    Text("OK")
                }
            },

            title = {
                Text(
                    text = "Summary Unavailable",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },

            text = {
                Text(
                    text = "A summary is unavailable \n for this article",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 16.dp)
                    )
                }

                // Condensed article content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp), // Padding around the content
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {


                    // only display summarizedText if the current url matches the article url
                    if (currentArticleUrl != articleUrl || summarizedText.isEmpty()) {

                        /*CircularProgressIndicator(
                            modifier = Modifier
                                .size(32.dp),
                            color = Color.Blue, // Customize color if needed
                            strokeWidth = 4.dp
                        )*/

                        LoadingIndicator(
                            color = Color.Blue
                        )


                    } else {
                        // Display summarizedText when ready
                        Text(
                            text = summarizedText,
                            style = androidx.compose.ui.text.TextStyle(
                                fontWeight = FontWeight.Normal,
                                color = Color.Black,
                                fontSize = 18.sp
                            ),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

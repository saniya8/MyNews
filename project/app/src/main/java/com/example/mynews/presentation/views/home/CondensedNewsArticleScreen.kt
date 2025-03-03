package com.example.mynews.presentation.views.home

import android.util.Log
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavHostController
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.io.InputStreamReader
import java.io.BufferedReader
import org.json.JSONObject
import org.jsoup.Jsoup
import com.example.mynews.utils.AppScreenRoutes

@Composable
fun CondensedNewsArticleScreen(
    navController: NavHostController,
    articleUrl: String
) {
    var articleText by remember { mutableStateOf("Loading...") }

    LaunchedEffect(articleUrl) {
        articleText = getArticleText(articleUrl)
    }

    Box(
        // User can swipe left to right to return back to the home screen
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
            // Display the article content received as a parameter
            Text(
                text = articleText,
                style = androidx.compose.ui.text.TextStyle(
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                modifier = Modifier.padding(8.dp), // Padding around the text
            )
        }
    }
}

suspend fun getArticleText(url: String): String {
    return withContext(Dispatchers.IO) {
        val token = "4d081799e61246e9c1cc86dc67e6bd9b"
        try {
            val apiUrl = "https://api.diffbot.com/v3/article?token=$token&url=$url"
            val connection = URL(apiUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode

            val response = StringBuilder()
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
            } else {
                val errorReader = BufferedReader(InputStreamReader(connection.errorStream))
                var errorLine: String?
                while (errorReader.readLine().also { errorLine = it } != null) {
                    response.append(errorLine)
                }
                errorReader.close()
            }
//            println("Diffbot Response: ${response)}") // This line can be used to see what is being returned by Diffbot

            val jsonResponse = JSONObject(response.toString())
            val htmlContent = jsonResponse.getJSONArray("objects").getJSONObject(0).getString("html")

            if (htmlContent.isNotEmpty()) {
                val document = Jsoup.parse(htmlContent)
                val text = document.text()

                if (text.isNotEmpty()) {
                    return@withContext text
                } else {
                    return@withContext "Failed to extract meaningful content from the article."
                }
            } else {
                return@withContext "No article content found in Diffbot response"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Error: ${e.message}"
        }
    }
}


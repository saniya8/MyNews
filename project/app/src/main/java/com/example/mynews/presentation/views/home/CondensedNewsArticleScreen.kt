package com.example.mynews.presentation.views.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun CondensedNewsArticleScreen(articleContent: String) {
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
            text = articleContent,
            style = TextStyle(
                fontWeight = FontWeight.Normal,
                color = Color.Black
            ),
            modifier = Modifier.padding(8.dp), // Padding around the text
        )
    }
}
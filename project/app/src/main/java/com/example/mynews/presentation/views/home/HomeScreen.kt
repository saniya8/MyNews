package com.example.mynews.presentation.views.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.navigation.NavHostController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.mynews.presentation.viewmodel.NewsViewModel
import com.example.mynews.presentation.viewmodel.SettingsViewModel
import com.example.mynews.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.mynews.presentation.views.home.NewsScreen
import com.example.mynews.utils.AppScreenRoutes


fun todayDateText() : String {
    val today = LocalDate.now()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("MMMM d")) // "e.g., February 17"
    return formattedDate;
}


@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController(),
    newsViewModel: NewsViewModel = hiltViewModel(), // keep here so newsViewModel persists between navigation
    /*onLogoutClicked: () -> Unit*/
) {


    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            // Heading
            Text(text = "My News",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Bold,
                color = CaptainBlue,
                fontSize = 25.sp,
                fontFamily = FontFamily.SansSerif
            )

            // Spacing
            Spacer(modifier = Modifier.height(8.dp))

            // Today's date
            Text(text = todayDateText(),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                //fontWeight = FontWeight.Bold,
                color = CaptainBlue,
                fontSize = 20.sp,
                fontFamily = FontFamily.SansSerif
            )

            NewsScreen(newsViewModel)

        }

    }


    /*

    // DEMO 1 SKELETON


    // Sample data for articles

    val articles = listOf(
        "Article 1: How to Improve Your Productivity",
        "Article 2: The Future of Technology",
        "Article 3: Understanding the Basics of Kotlin",
        "Article 4: Top 10 Places to Visit in 2025"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Title
        Text(
            text = "Your News Feed",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = Color.Black
        )

        // Filter Banner
        Card(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E3D83))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Filter My Feed",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }

        // Scrollable list of articles
        LazyColumn(
            //modifier = Modifier.fillMaxSize()
            modifier = Modifier.weight(1f)
        ) {
            items(articles) { article ->
                ArticleRow(article)
            }
        }

        // Old logout button (pre on settings screen)
        /*
        Button(onClick = { onLogoutClicked() }) {
            Text("Logout")
        }
         */

    }

     */



}

@Composable
fun ArticleRow(article: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Article Icon",
            modifier = Modifier
                .size(40.dp)
                .padding(end = 16.dp)
        )

        Box(
            modifier = Modifier
                .background(Color(0xFFD8E6FF), shape = RoundedCornerShape(8.dp))
                .padding(12.dp)
                .fillMaxWidth(0.85f)
        ) {
            Text(
                text = article,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

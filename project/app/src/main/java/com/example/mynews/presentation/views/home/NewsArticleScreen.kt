package com.example.mynews.presentation.views.home

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.mynews.data.api.Article

@Composable
fun NewsArticleScreen(
    navController: NavHostController,
    //article : Article
    articleUrl: String
){

    AndroidView(factory = {context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            loadUrl(articleUrl)
        }
    })

    /* for testing - pre-implementing web-view
    //Text(text = "News article page")
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
        }
        Text(text = "News Article Page", fontSize = 20.sp)
        Text(text = "URL: $articleUrl")
        //Text(text = article.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        //Text(text = article.description, fontSize = 16.sp)
        //Text(text = "URL IS: ${article.url}")

    }

     */
}
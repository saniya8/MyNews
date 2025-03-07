package com.example.mynews.presentation.views.home

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import com.example.mynews.utils.AppScreenRoutes

@Composable
fun NewsArticleScreen(
    navController: NavHostController,
    articleUrl: String,
    origin: String,
){

    var isLoading by remember { mutableStateOf(true) } // track loading state
    var loadFailed by remember { mutableStateOf(false) } // track if load fails

    Box (
        // User can swipe left to right to return back to the home screen
        modifier = Modifier
            .fillMaxSize()

            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) { // Detect swipe right to go back
                        //navController.popBackStack()
                        if (origin == "HomeScreen") {
                            navController.popBackStack(AppScreenRoutes.HomeScreen.route, false)
                        } else if (origin == "SavedArticlesScreen") {
                            navController.popBackStack(AppScreenRoutes.SavedArticlesScreen.route, false)
                        } else {
                            Log.d("NewsArticleScreen", "Origination error: did not originate from HomeScreen or SavedArticlesScreen")
                        }
                    }
                }
            }

    ){

        // Still have to test if the Failed to Load Article message appears correctly based off
        // of the code in AndroidView when either:
        // a) Initial load fails, or
        // b) It loads but then fails
        // Can't test this until API provides an article that actually fails
        AndroidView(factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                webViewClient = object : WebViewClient() {

                    // Page starts to load URL
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        isLoading = true
                        loadFailed = false // Reset failure state for new page
                    }

                    // Page starts to render URL contents
                    // Need this so that the circular progress indicator stops immediately when
                    // the page starts being rendered
                    override fun onPageCommitVisible(view: WebView?, url: String?) {
                        isLoading = false
                        loadFailed = false // Reset failure state since content is now visible
                    }


                    override fun onPageFinished(view: WebView?, url: String?) {
                        // Check if WebView content is actually visible
                        view?.evaluateJavascript(
                            "(function() { return document.body.innerText.length })();"
                        ) { contentLength ->
                            if (contentLength == "0" || contentLength == "null") {
                                loadFailed = true // If body is empty, mark as failed
                            } else {
                                isLoading = false
                            }
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        if (request?.isForMainFrame == true) {
                            isLoading = false
                            loadFailed = true
                        }
                    }

                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?
                    ) {
                        if (request?.isForMainFrame == true) {
                            isLoading = false
                            loadFailed = true
                        }
                    }
                }
                loadUrl(articleUrl)
            }
        })


        // Show circular progress indicator while the article is loading
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center) // Center the loading circle
                    .size(50.dp),
                color = Color.Blue, // Customize color if needed
                strokeWidth = 4.dp
            )
        }

        // Show error message if loading fails
        if (loadFailed) {
            Text(
                text = "Failed to load article.",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center),
                fontSize = 18.sp,
                //fontWeight = FontWeight.Bold
            )
        }

    }

}

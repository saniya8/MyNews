package com.example.mynews.presentation.views.home

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import com.example.mynews.presentation.components.LoadingIndicator
import com.example.mynews.utils.AppScreenRoutes

@Composable
fun NewsArticleScreen(
    navController: NavHostController,
    articleUrl: String,
    origin: String,
){

    var isLoading by remember { mutableStateOf(true) } // track loading state
    var loadFailed by remember { mutableStateOf(false) } // track if load fails
    var webView: WebView? by remember { mutableStateOf(null) } // reference to reload if needed
    var shouldReloadWebView by remember { mutableStateOf(true) }


    Box (
        // User can swipe left to right to return back to the home screen
        modifier = Modifier
            .fillMaxSize()

            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) { // Detect swipe right to go back
                        if (origin == "HomeScreen") {
                            navController.popBackStack(AppScreenRoutes.HomeScreen.route, false)
                        } else if (origin == "SavedArticlesScreen") {
                            navController.popBackStack(AppScreenRoutes.SavedArticlesScreen.route, false)
                        } else if (origin == "SocialScreen") {
                            navController.popBackStack(AppScreenRoutes.SocialScreen.route, false)
                        } else {
                            Log.d("NewsArticleScreen", "Origination error: did not originate from HomeScreen or SavedArticlesScreen or SocialScreen")
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

        if (!loadFailed && shouldReloadWebView) {
            AndroidView(factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true

                    webViewClient = object : WebViewClient() {

                        // Page starts to load URL
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            Handler(Looper.getMainLooper()).post { // state update happens on UI thread
                                isLoading = true
                                loadFailed = false // Reset failure state for new page
                            }
                        }

                        // Page starts to render URL contents
                        // Need this so that the circular progress indicator stops immediately when
                        // the page starts being rendered
                        override fun onPageCommitVisible(view: WebView?, url: String?) {
                            Handler(Looper.getMainLooper()).post { // state update happens on UI thread
                                isLoading = false
                                loadFailed = false // Reset failure state since content is now visible
                            }
                        }


                        override fun onPageFinished(view: WebView?, url: String?) {
                            // Check if WebView content is actually visible
                            view?.evaluateJavascript(
                                "(function() { return document.body.innerText.length })();"
                            ) { contentLength ->
                                Handler(Looper.getMainLooper()).post { // state update happens on UI thread
                                    if (contentLength == "0" || contentLength == "null") {
                                        loadFailed = true // If body is empty, mark as failed
                                    } else {
                                        isLoading = false
                                    }
                                }
                            }
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            if (request?.isForMainFrame == true) {
                                Handler(Looper.getMainLooper()).post { // state update happens on UI thread
                                    isLoading = false
                                    loadFailed = true
                                }
                            }
                        }

                        override fun onReceivedHttpError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            errorResponse: WebResourceResponse?
                        ) {
                            if (request?.isForMainFrame == true) {
                                Handler(Looper.getMainLooper()).post { // state update happens on UI thread
                                    isLoading = false
                                    loadFailed = true
                                }
                            }
                        }
                    }

                    loadUrl(articleUrl)

                    /*
                    // simulate error:
                    //loadUrl("http://deadline.com/2025/03/impractical-jokers-joe-gatto-denies-sexual-assualt-allegation-1236347194/")
                    val urlToLoad = if (articleUrl.contains("cnn", ignoreCase = true)) {
                        // Simulate a broken URL
                        "http://deadline.com/2025/03/impractical-jokers-joe-gatto-denies-sexual-assualt-allegation-1236347194/"
                    } else {
                        articleUrl
                    }
                    loadUrl(urlToLoad)
                     */


                    webView = this // save reference to enable reload of webview
                }
            })
        }


        // Show circular progress indicator while the article is loading
        if (isLoading) {
            /*CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center) // Center the loading circle
                    .size(32.dp),
                color = Color.Blue, // Customize color if needed
                strokeWidth = 4.dp
            )*/
            LoadingIndicator(
                color = Color.Blue
            )
        }

        // Show error message if loading fails
        if (loadFailed) {

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                Text(
                    text = "Failed to load article",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    Log.d("NewsArticleScreen", "Retrying article load...")
                    //webView?.reload() // retry logic
                    isLoading = true
                    loadFailed = false // this triggers the webview to be recreated
                    shouldReloadWebView = false // temporarily disable
                    // Delayed re-enable to trigger clean reload
                    Handler(Looper.getMainLooper()).postDelayed({
                        shouldReloadWebView = true
                    }, 1500) // Delay in ms

                }) {
                    Text("Try Again")
                }


            }

        }

    }

}

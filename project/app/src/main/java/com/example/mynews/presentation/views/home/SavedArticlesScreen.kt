package com.example.mynews.presentation.views.home

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mynews.presentation.components.ScreenHeader
import com.example.mynews.presentation.viewmodel.goals.GoalsViewModel
import com.example.mynews.presentation.viewmodel.home.NewsViewModel
import com.example.mynews.presentation.viewmodel.home.SavedArticlesViewModel
import com.example.mynews.utils.AppScreenRoutes

@Composable
fun SavedArticlesScreen(
    navController: NavHostController,
    newsViewModel: NewsViewModel,
    savedArticlesViewModel: SavedArticlesViewModel,
    goalsViewModel: GoalsViewModel,
){

    val articles by savedArticlesViewModel.savedArticles.observeAsState(emptyList())
    val emptyListState = rememberLazyListState()

    // Fetch saved articles when screen is initially created
    LaunchedEffect(Unit) {
        savedArticlesViewModel.fetchSavedArticles()
    }

    Box (
        // user can swipe left to right to return back to the home screen
        modifier = Modifier
            .fillMaxSize()

            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) { // detect swipe right to go back
                        navController.popBackStack(AppScreenRoutes.HomeScreen.route, false)
                    }
                }
            }

    ) {

        Scaffold(
            modifier = Modifier.fillMaxSize(),

        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {

                // standardized
                ScreenHeader(
                    useTopPadding = false,
                    title = "Saved Articles",
                )

                if (articles.isEmpty()) { // user has no saved articles
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "You have no saved articles",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Swipe left on an article to save it",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    }


                } else { // user has saved articles

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text =
                            if (articles.size == 1) {
                                "You have ${articles.size} saved article"
                            } else { // articles.size > 1
                                "You have ${articles.size} saved articles"
                            },
                            fontSize = 18.sp,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.Start),
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        NewsScreen(
                            navController = navController,
                            newsViewModel = newsViewModel,
                            savedArticlesViewModel = savedArticlesViewModel,
                            goalsViewModel = goalsViewModel,
                            articles = articles,
                            origin = "SavedArticlesScreen",
                            openDrawer = {}, // empty since no drawer on this screen,
                            onLongPressRelease = {_, _ ->}, // empty callback,
                            listState = emptyListState,
                        ) // Display news

                }
            }

        } // end of body of scaffold

    }

}
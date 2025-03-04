package com.example.mynews.presentation.views.home

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavHostController
import com.example.mynews.presentation.viewmodel.home.SavedArticlesViewModel
import com.example.mynews.utils.AppScreenRoutes
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.mynews.presentation.viewmodel.home.NewsViewModel
import com.example.mynews.ui.theme.CaptainBlue

@Composable
fun SavedArticlesScreen(
    navController: NavHostController,
    newsViewModel: NewsViewModel,
    savedArticlesViewModel: SavedArticlesViewModel,
    //article: Article
){

    // uncomment this when saved articles variable is created
    val articles by savedArticlesViewModel.savedArticles.observeAsState(emptyList())

    // no need for LaunchedEffect based on articles value because
    // in SavedArticlesViewModel's fetchSavedArticles, addSnapshotListener is used meaning
    // whenever articles subcollection is updated, firestore detects a change in real time and
    // in that function, triggers _savedArticles.postValue(userSavedArticles) which updates
    // _savedArticles which updates savedArticles in the view model. Since SavedArticlesScreen
    // is observing savedArticles in the view model, whenever savedArticles in view model updates,
    // UI will be rerendered

    // Fetch saved articles when screen is initially created
    LaunchedEffect(Unit) {
        savedArticlesViewModel.fetchSavedArticles()
    }

    // MIGHT HAVE TO CHANGE THIS BOX POINTER INPUT WHEN ADDING DELETE FUNCTION ON SAVED ARTICLE
    // Different swiping right on saved article to delete it versus swiping right on the screen
    // to go back to the HomeScreen
    Box (
        // User can swipe left to right to return back to the home screen
        modifier = Modifier
            .fillMaxSize()

            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) { // Detect swipe right to go back
                        //navController.popBackStack()
                        navController.popBackStack(AppScreenRoutes.HomeScreen.route, false)
                    }
                }
            }

    ) {

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            //topBar = { // moving this button to be next to the search bar
            //    IconButton(onClick = { scope.launch { drawerState.open() } }) {
            //        Icon(Icons.Default.Tune, contentDescription = "Filter Menu")
            //    }
            // }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp) // REMOVE IF BACK BUTTON IS ADDED IN LINE W HEADER
                ) {
                    // "Saved Articles" - Exactly centered
                    Text(
                        text = "Saved Articles",
                        fontWeight = FontWeight.Bold,
                        color = CaptainBlue,
                        fontSize = 25.sp,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    /*

                // keeping commented here in case need to replicate for a return button
                // Saved Articles Icon - Pinned to the top right
                IconButton(
                    onClick = { navController.navigate(AppScreenRoutes.SavedArticlesScreen.route)
                    },
                    modifier = Modifier.align(Alignment.TopEnd) // Ensures it stays in the top-right
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        //imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = "Saved Articles",
                        tint = CaptainBlue
                    )
                }

                 */


                }


                if (articles.isEmpty()) { // user has no saved articles
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
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

                        /*
                    // Today's date
                    Text(
                        text = todayDateText(),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = CaptainBlue,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.SansSerif
                    )

                     */

                        Spacer(modifier = Modifier.height(10.dp))

                        NewsScreen(
                            navController = navController,
                            newsViewModel = newsViewModel,
                            savedArticlesViewModel = savedArticlesViewModel,
                            articles = articles,
                            origin = "SavedArticlesScreen",
                            openDrawer = {}, // empty since no drawer on this screen
                        ) // Display news

                    }
            }

        } // end of body of scaffold






    }

}
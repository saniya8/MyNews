package com.example.mynews.presentation.views.home

import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mynews.presentation.viewmodel.home.NewsViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.mynews.data.api.Article
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mynews.presentation.viewmodel.home.SavedArticlesViewModel
import com.example.mynews.utils.AppScreenRoutes
import kotlin.math.roundToInt
import kotlinx.coroutines.launch


@Composable
fun NewsScreen(
    navController: NavHostController,
    newsViewModel: NewsViewModel,
    savedArticlesViewModel: SavedArticlesViewModel,
    articles: List<Article>,
    origin: String,
    openDrawer: () -> Unit,
) {

    // Observe the articles
    // now doing this individually in HomeScreen and SavedArticlesScreen
    //val articles by newsViewModel.articles.observeAsState(emptyList())

    Column(
        modifier = Modifier.fillMaxSize()
    )

    {

        // LazyColumn is used to display a vertically scrolling list of items
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()

        ) {
            items(articles, key = { it.url }) {article ->
                //Text(text = article.title) // for testing
                //Text(text = article.urlToImage) // for testing
                //Text(text = "----------------") // for testing
                ArticleItem(navController = navController,
                            newsViewModel = newsViewModel,
                            savedArticlesViewModel = savedArticlesViewModel,
                            article = article,
                            origin = origin,
                            openDrawer = openDrawer,
                            )

            }

        }

    }

}



@Composable
fun ArticleItem(
    navController: NavHostController,
    newsViewModel: NewsViewModel,
    savedArticlesViewModel: SavedArticlesViewModel,
    article: Article,
    origin: String,
    openDrawer: () -> Unit,
){
    val encodedUrl = Uri.encode(article.url)

    // Box for animation when saving article
    val swipeOffset = remember { Animatable(0f) }
    val itemWidth = remember { mutableStateOf(1f) }
    val scope = rememberCoroutineScope()
    val cardHeight = 130.dp


    Box(
        modifier = Modifier
            .height(cardHeight)
            .fillMaxWidth()
            .background(Color.Transparent)
            .onSizeChanged { itemWidth.value = it.width.toFloat()
            },

    ) {
        // Background Save Icon (only visible when swiping)

        Box(
            modifier = Modifier
                .matchParentSize()
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(horizontal = 16.dp),
            contentAlignment =
                if (origin == "HomeScreen") {
                    Alignment.CenterEnd
                } else {
                    Alignment.CenterStart
                } // icon on the right for save, left for delete
        )

        {
            Icon(
                imageVector =
                    if (origin == "HomeScreen") {
                        Icons.Filled.Bookmark
                    } else {
                        Icons.Filled.Delete
                    },
                contentDescription =
                    if (origin == "HomeScreen") {
                        "Save"
                    } else {
                        "Delete"
                    },
                tint = Color.Blue,
                modifier = Modifier
                    .size(40.dp)
                    .alpha(
                        if (origin == "HomeScreen") {
                            (-swipeOffset.value / itemWidth.value).coerceIn(0f, 1f)
                        } else {
                            (swipeOffset.value / itemWidth.value).coerceIn(0f, 1f)
                        }
                    ) // only visible when swiping
            )
        }

        Card(
            modifier = Modifier

                .padding(8.dp)
                .height(cardHeight) // fixing image
                .fillMaxWidth()
                .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                .pointerInput(Unit) {

                    //Log.d("NewsScreen", "SwipeLocation: $swipeLocation for article: ${article.title}")

                    if (origin == "HomeScreen") {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (swipeOffset.value < -0.4f * itemWidth.value) {
                                        // if swiped past threshold then save article & reset position
                                        Log.d(
                                            "NewsScreen",
                                            "Swiped LEFT on article: ${article.title}"
                                        )

                                        savedArticlesViewModel.saveArticle(article)
                                        swipeOffset.animateTo(0f) // reset to original position after saving article
                                    } else {
                                        // snap back if not swiped enough
                                        swipeOffset.animateTo(0f)
                                    }
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                scope.launch {

                                    // only handle right-to-left swipes
                                    if (dragAmount < 0) {
                                        swipeOffset.snapTo(
                                            (swipeOffset.value + dragAmount).coerceIn(
                                                -itemWidth.value,
                                                0f
                                            )
                                        )
                                    } else if (swipeOffset.value == 0f && dragAmount > 50) {
                                        // left to right swipes: Open drawer ONLY if swipe starts at zero and is strong enough
                                        Log.d(
                                            "GestureDebug",
                                            "Swiped RIGHT on article → Opening Drawer"
                                        )
                                        openDrawer()
                                    } else {
                                        // left to right swipes: Just reset position (DO NOT open drawer)
                                        Log.d(
                                            "GestureDebug",
                                            "Swiped RIGHT on article (Resetting) → No Drawer"
                                        )
                                        swipeOffset.animateTo(0f)
                                    }
                                }
                            }
                        )
                    } else if (origin == "SavedArticlesScreen") {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (swipeOffset.value > 0.4f * itemWidth.value) {
                                        // if swiped past threshold then delete article & reset position
                                        Log.d(
                                            "NewsScreen",
                                            "Swiped RIGHT on article: ${article.title}"
                                        )

                                        savedArticlesViewModel.deleteSavedArticle(article)
                                        swipeOffset.animateTo(0f) // reset to original position after deleting article
                                    } else {
                                        // snap back if not swiped enough
                                        swipeOffset.animateTo(0f)
                                    }
                                }
                            },

                            onHorizontalDrag = { _, dragAmount ->
                                scope.launch {

                                    // only handle left-to-right swipes
                                    if (dragAmount > 0) {
                                        swipeOffset.snapTo(
                                            (swipeOffset.value + dragAmount).coerceIn(
                                                0f,
                                                itemWidth.value
                                            )
                                        )
                                    }

                                    // for right to left swipes, do nothing
                                }
                            }
                        )

                    } else {
                        Log.e("NewsScreen", "Caller was neither HomeScreen nor SavedArticlesScreen")
                    }

                }
            ,

            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            onClick = {

                if (origin == "HomeScreen" || origin == "SavedArticlesScreen") {
                    navController.navigate(
                        AppScreenRoutes.NewsArticleScreen.createRoute(
                            encodedUrl,
                            origin
                        )
                    )
                } else {
                    Log.d("NewsScreen", "On click, origin is neither HomeScreen nor SavedArticlesScreen")
                }

            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically

            ) {
                // Show the thumbnail

                Log.d("CoilDebug", "Loading image URL: ${article.urlToImage}")


                val placeholderImage: String =
                    "https://s.france24.com/media/display/e6279b3c-db08-11ee-b7f5-005056bf30b7/w:1024/p:16x9/news_en_1920x1080.jpg"

                val articleImageUrl = if (article.urlToImage?.startsWith("https") == true) {
                    // use the article.urlToImage only if it is non-null and starts with "https"
                    // so image is retrieved correctly
                    article.urlToImage
                } else {
                    // if article.urlToImage is null, or if article.urlToImage is not null but
                    // doesn't start with "https", then use the placeholder image
                    placeholderImage
                }

                AsyncImage(
                    model = articleImageUrl /*article.urlToImage?: placeholderImage*/,
                    contentDescription = "Article Image",
                    modifier = Modifier.size(80.dp)
                        .size(80.dp) // fixing image to fixed square size
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.fillMaxSize()

                        .padding(start = 8.dp) // gap between Date at top of page and the scrollable articles
                ) {

                    // show the article title
                    Text(
                        text = article.title,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis // cuts off text with "..."
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp) // adds space between the two Text objects
                    ) {
                        // show the source
                        Text(
                            text = article.source.name,
                            maxLines = 1,
                            fontSize = 14.sp
                        )

                        Text(
                            text = "Condensed Article",
                            modifier = Modifier.clickable {
                                // navigate to the CondensedNewsArticleScreen with article content
                                Log.d("Condensed Debug", "Article clicked: ${article.title}")
                                navController.navigate(
                                    AppScreenRoutes.CondensedNewsArticleScreen.createRoute(
                                        encodedUrl
                                    )
                                )
                            },
                            color = Color.Blue,
                            fontSize = 14.sp
                        )
                    }

                }
            }
        } // end of Card
    } // end of Box

}

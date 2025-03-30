package com.example.mynews.presentation.views.home

import android.net.Uri
import android.util.Log
import android.view.ViewConfiguration
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
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
import com.example.mynews.data.api.news.Article
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.mynews.R
import com.example.mynews.presentation.viewmodel.goals.GoalsViewModel
import com.example.mynews.presentation.viewmodel.home.SavedArticlesViewModel
import com.example.mynews.ui.theme.BiasColors
import com.example.mynews.utils.AppScreenRoutes
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs

@Composable
fun NewsScreen(
    navController: NavHostController,
    newsViewModel: NewsViewModel,
    savedArticlesViewModel: SavedArticlesViewModel,
    goalsViewModel: GoalsViewModel,
    articles: List<Article>,
    origin: String,
    openDrawer: () -> Unit,
    onLongPressRelease: (Article, Float) -> Unit, // callback to notify HomeScreen
    listState: LazyListState,
) {

    // Observe the articles
    // now doing this individually in HomeScreen and SavedArticlesScreen
    //val articles by newsViewModel.articles.observeAsState(emptyList())
    Column(
        modifier = Modifier.fillMaxSize()
    )

    {

        // LazyColumn is used to display a vertically scrolling list of items
        LazyColumn( state = listState,
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
                            goalsViewModel = goalsViewModel,
                            article = article,
                            origin = origin,
                            openDrawer = openDrawer,
                            onLongPressRelease = onLongPressRelease, // pass callback to ArticleItem
                            listState = listState,
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
    goalsViewModel: GoalsViewModel,
    article: Article,
    origin: String,
    openDrawer: () -> Unit,
    onLongPressRelease: (Article, Float) -> Unit, // callback to notify HomeScreen
    listState: LazyListState,
){
    val encodedUrl = Uri.encode(article.url)

    // Box for animation when saving article
    val swipeOffset = remember { Animatable(0f) }
    val itemWidth = remember { mutableStateOf(1f) }
    val scope = rememberCoroutineScope()
    val cardHeight = 130.dp
    val isTapValid = remember { mutableStateOf(true) }
    var articleYCoord = 0f
    var articleLayoutCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }



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
                .onGloballyPositioned { coordinates ->
                    //articleYCoord = coordinates.positionInRoot().y // store absolute Y-position (but doesn't trigger recomposition)
                    articleLayoutCoordinates = coordinates
                }

                .pointerInput(Unit) {
                    var wasLongPress = false

                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val downPosition = down.position

                            var longPressTriggered = false
                            var dragDistance = 0f
                            val moveThreshold = 10f
                            var horizontalDrag = 0f
                            var isScrollGesture = false
                            isTapValid.value = true // Reset at start of new interaction

                            // Detect long press with improved cancellation handling
                            val longPressJob = scope.launch {
                                try {
                                    // Standard Android long press timeout
                                    delay(ViewConfiguration.getLongPressTimeout().toLong())

                                    // Only trigger if minimal movement has occurred
                                    if (origin == "HomeScreen" && dragDistance < moveThreshold) {
                                        longPressTriggered = true
                                        wasLongPress = true
                                        isTapValid.value = false // Prevent tap handling

                                        Log.d("GestureDebug", "Long press detected at $downPosition")
                                    }
                                } catch (e: CancellationException) {
                                    Log.d("GestureDebug", "Long press canceled")
                                }
                            }

                            try {
                                var hasMoved = false

                                do {
                                    val event = awaitPointerEvent()
                                    val dragEvent = event.changes.firstOrNull { it.id == down.id }

                                    if (dragEvent != null && dragEvent.positionChange() != Offset.Zero) {
                                        val positionChange = dragEvent.positionChange()
                                        val currentPosition = dragEvent.position
                                        dragDistance = (currentPosition - downPosition).getDistance()

                                        // If significant movement happened, cancel long press and handle as drag
                                        if (dragDistance > moveThreshold && !hasMoved) {
                                            hasMoved = true
                                            longPressJob.cancel()
                                            isTapValid.value = false
                                        }

                                        // detect scrolling early
                                        if (abs(positionChange.y) > abs(positionChange.x) * 1.5f) {
                                            isScrollGesture = true // user is scrolling
                                        }

                                        // Handle horizontal swipes
                                        if (hasMoved && !isScrollGesture) {
                                            val horizontalDragAmount = dragEvent.position.x - dragEvent.previousPosition.x
                                            horizontalDrag += horizontalDragAmount

                                            // Check if mostly horizontal movement (to distinguish from scrolling)
                                            val isHorizontalMovement = abs(positionChange.x) > abs(positionChange.y) * 1.5f

                                            if (isHorizontalMovement) {
                                                if (origin == "HomeScreen") {
                                                    if (horizontalDragAmount < 0) { // Left swipe
                                                        scope.launch {
                                                            swipeOffset.snapTo(
                                                                (swipeOffset.value + horizontalDragAmount).coerceIn(-itemWidth.value, 0f)
                                                            )
                                                        }
                                                        dragEvent.consume()
                                                    } else if (swipeOffset.value == 0f && horizontalDrag > 50) { // Right swipe
                                                        Log.d("GestureDebug", "Opening drawer from article swipe")
                                                        if (!isScrollGesture) {
                                                            openDrawer()
                                                            dragEvent.consume()
                                                            break
                                                        }
                                                    }
                                                } else if (origin == "SavedArticlesScreen") {
                                                    if (horizontalDragAmount > 0) { // Right swipe in saved articles
                                                        scope.launch {
                                                            swipeOffset.snapTo(
                                                                (swipeOffset.value + horizontalDragAmount).coerceIn(0f, itemWidth.value)
                                                            )
                                                        }
                                                        dragEvent.consume()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while (event.changes.any { it.pressed })

                                // y coord correct for all articles, even on scroll, and also correct for
                                // top article if partially cut off
                                if (wasLongPress && !hasMoved) {
                                    Log.d("GestureDebug", "Long press released, showing reaction bar")

                                    // Get the article's Y position in root coordinates
                                    var updatedYCoord = articleLayoutCoordinates?.positionInRoot()?.y ?: articleYCoord

                                    // Get scroll state details
                                    val firstVisibleItemOffset = listState.firstVisibleItemScrollOffset.toFloat()
                                    val firstVisibleItemIndex = listState.firstVisibleItemIndex

                                    Log.d("GestureDebug", "Original updatedYCoord: $updatedYCoord")
                                    Log.d("GestureDebug", "First Visible Item Offset: $firstVisibleItemOffset")
                                    Log.d("GestureDebug", "First Visible Item Index: $firstVisibleItemIndex")

                                    // Define a minimum Y-coordinate (e.g., just below search bar or top of screen)
                                    val minYCoord = 460f // Adjust this based on your header/search bar height

                                    // If the article's top is scrolled out of view (negative or too small Y),
                                    // clamp it to the top of the visible area
                                    if (firstVisibleItemIndex == 0 && updatedYCoord < minYCoord) {
                                        Log.d("GestureDebug", "Top article partially off-screen, clamping Y to minYCoord")
                                        updatedYCoord = minYCoord
                                    } else {
                                        // For articles fully in view, ensure reaction bar is above the article
                                        // You might subtract a small offset if needed (e.g., reaction bar height)
                                        updatedYCoord -= 5f // Optional: fine-tune to position "just above"
                                    }

                                    // Ensure it doesn't go below a sensible minimum (safety check)
                                    updatedYCoord = maxOf(updatedYCoord, minYCoord)

                                    Log.d("GestureDebug", "Final updatedYCoord: $updatedYCoord")

                                    onLongPressRelease(article, updatedYCoord)
                                    continue
                                }


                                // Handle completed swipe
                                if (horizontalDrag != 0f && !isScrollGesture) {
                                    scope.launch {
                                        if (origin == "HomeScreen" && swipeOffset.value < -0.4f * itemWidth.value) {
                                            Log.d("NewsScreen", "Saving article from swipe: ${article.title}")
                                            savedArticlesViewModel.saveArticle(article)
                                        } else if (origin == "SavedArticlesScreen" && swipeOffset.value > 0.4f * itemWidth.value) {
                                            Log.d("NewsScreen", "Deleting article from swipe: ${article.title}")
                                            savedArticlesViewModel.deleteSavedArticle(article)
                                        }

                                        swipeOffset.animateTo(0f) // Reset position
                                    }
                                }

                            } finally {
                                // Clean up
                                if (longPressJob.isActive) {
                                    longPressJob.cancel()
                                }
                                wasLongPress = false
                            }
                        }
                    }
                }

            ,

            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            onClick = {

                if (isTapValid.value) { // only valid taps trigger navigation

                    if (origin == "HomeScreen" || origin == "SavedArticlesScreen") {

                        if (origin == "HomeScreen") {
                            goalsViewModel.logArticleRead(article)
                        }
                        navController.navigate(
                            AppScreenRoutes.NewsArticleScreen.createRoute(
                                encodedUrl,
                                origin
                            )
                        )
                    } else {
                        Log.d(
                            "NewsScreen",
                            "On click, origin is neither HomeScreen nor SavedArticlesScreen"
                        )
                    }
                } else {
                    Log.d("GestureDebug", "Tap ignored due to invalid tap state")
                }

                isTapValid.value = true

            }
        ) {


            Box( // wrap everything inside a Box so political bias flag placed correctly
                modifier = Modifier.fillMaxSize()
            ) {


                // political bias flag

                //val bias = newsViewModel.fetchBiasForSource(article.source.name)
                //val biasColor = BiasColors.getBiasColour(bias)

                var biasColor by remember { mutableStateOf(Color.Black) } // just for initialization


                newsViewModel.fetchBiasForSource(article.source.name) { bias ->
                    biasColor = BiasColors.getBiasColour(bias) // Update bias color once fetched
                }

                Box(
                    modifier = Modifier
                        .size(width = 12.dp, height = 18.dp) // Fixed size for now
                        .background(biasColor) // Placeholder color (to be updated)
                        .align(Alignment.TopEnd) // Position in the top right
                        .padding(4.dp) // Small padding from edges
                )




                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    // Show the thumbnail

                    Log.d("CoilDebug", "Loading image URL: ${article.urlToImage}")

                    // correctly handles errors
                    // for images that starts with http, goes to fallback as expected
                    // for images that result in error, goes to fallback as expected
                    // fallback image now added to the project itself rather than retrieved
                    // from url
                    AsyncImage(
                        model = article.urlToImage,
                        contentDescription = "Article Image",
                        //placeholder = painterResource(R.drawable.news_placeholder_image),
                        error = painterResource(R.drawable.news_placeholder_image),
                        fallback = painterResource(R.drawable.news_placeholder_image),
                        modifier = Modifier
                            .size(80.dp)
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )

                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        Column(
                            modifier = Modifier.fillMaxSize()
                                .weight(1f) // makes the text take available space but respects the flag
                                .padding(start = 8.dp, end = 15.dp) // adds extra space on the right
                                // start: the space between the article image and the text
                                // end: the space to the right of the entire column (where column contains
                                // article title, below it the source and Condensed Article. This ensures the
                                // text does not overlap with the flag

                            //.padding(start = 8.dp)
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
                                        Log.d(
                                            "Condensed Debug",
                                            "Article clicked: ${article.title}"
                                        )
                                        navController.navigate(
                                            AppScreenRoutes.CondensedNewsArticleScreen.createRoute(
                                                encodedUrl,
                                                article.title
                                            )
                                        )
                                    },
                                    color = Color.Blue,
                                    fontSize = 14.sp
                                )
                            }

                        } // end of Column
                    }


                }

            }

        } // end of Card
    } // end of Box

}
package com.example.mynews.presentation.views.home

import android.net.Uri
import android.util.Log
import android.view.ViewConfiguration
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mynews.presentation.viewmodel.home.SavedArticlesViewModel
import com.example.mynews.utils.AppScreenRoutes
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


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
    val isTapValid = remember { mutableStateOf(true) }


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



                /*
                // WORKS - pre-adding reaction bar long press
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

                 */

                // .pointerInput to handle tap, long press, and swipe
                .pointerInput(Unit) {

                    var wasLongPress = false // tracks if long press was completed before the user lifted their finger

                    // step 1: set up the gesture detection
                    awaitPointerEventScope {  // starts listening for pointer (touch) events
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false) // waits for the first touch
                            val downPosition = down.position // records where the touch started

                            var longPressTriggered = false // becomes true if long press detected
                            var dragDistance = 0f // how far the user moved their finger
                            val moveThreshold = 10f // minimum movement to count as a swipe
                            var shouldBreakLoop = false // used to exit the while(true) if needed
                            var isDragging = false // true if user strarts dragging (not just tapping)

                            // step 2: detect long press
                            val longPressJob = scope.launch {
                                try {
                                    delay(ViewConfiguration.getLongPressTimeout().toLong()) // wait the long press duration
                                    if (origin == "HomeScreen" && dragDistance < moveThreshold) { // if it came from home screen and barely any movement
                                        longPressTriggered = true // mark as a long press
                                        wasLongPress = true // immediately mark long press as detected
                                        isTapValid.value = false // don't let long press be treated as a tap
                                        Log.d("GestureDebug", "Long press detected at $downPosition")
                                        Log.d("GestureDebug", "longPressTriggered: ${longPressTriggered}")
                                        //Log.d("GestureDebug", "------------------------")
                                    }
                                } catch (e: CancellationException) { // user moved too much, long press cancelled
                                    Log.d("GestureDebug", "Long press canceled")
                                }
                            }

                            // step 3: track drags

                            var horizontalDrag = 0f // tracks horizontal movement of finger
                            //var wasLongPress = false // tracks if long press was completed before the user lifted their finger

                            try {
                                do {
                                    val event = awaitPointerEvent() // wait for the next pointer event (i.e., even a tiny finger movement)
                                    val dragEvent = event.changes.firstOrNull { it.id == down.id } // gets the touch event for the original touch

                                    if (dragEvent != null && dragEvent.positionChange() != Offset.Zero) { // if there is any movement at all
                                        val positionChange = dragEvent.positionChange() // gets the change in position since the last event
                                        val currentPosition = dragEvent.position // gets the current position of the touch
                                        val distance = (currentPosition - downPosition).getDistance() // calculates the total distance moved from the ORIGINAL touch ie the starting point
                                        dragDistance = distance // assigns that distance to the dragDistance variable

                                        if (positionChange.y > moveThreshold || positionChange.y < -moveThreshold) {
                                            // if movement is mostly vertical, treat it as scrolling and stop the other gestures
                                            isDragging = true
                                        }

                                        /*
                                        // remove - handled above immediately after long press detected
                                        if (longPressTriggered) { // if a long press was detected earlier
                                            wasLongPress = true // mark that the long press happened
                                            isTapValid.value = false // prevent long press from being treated as a tap
                                            //continue // POTENTIAL BUG: skip further processing NEEDS TO HANDLE RELEASE - MAY NEED TO REMOVE CONTINUE
                                        }

                                         */

                                        // step 4: handle swipes

                                        if (distance > moveThreshold) { // user moved their finger a significant amount
                                            longPressJob.cancel() // cancel the long press job because no longer a long press
                                            isTapValid.value = false // ensure that this touch event is not interpreted as a tap
                                            Log.d("GestureDebug", "Long press canceled due to movement")


                                            val horizontalDragAmount = dragEvent.position.x - dragEvent.previousPosition.x // change in horizontal postion
                                            horizontalDrag += horizontalDragAmount // accumulate the total horizontal movement

                                            if (!isDragging) { // if the user is not dragging vertically (ie mostly horizontal movement)
                                                if (origin == "HomeScreen") { // if this is happening on the home screen
                                                    if (horizontalDragAmount < 0) { // if the user is swiping left
                                                        scope.launch {
                                                            swipeOffset.snapTo( // move the article item left
                                                                (swipeOffset.value + horizontalDragAmount).coerceIn(-itemWidth.value, 0f)
                                                            )
                                                        }
                                                    } else if (swipeOffset.value == 0f && horizontalDrag > 50) { // if the user is swiping right and is strong enough
                                                        Log.d("GestureDebug", "Swiped RIGHT on article → Opening Drawer")
                                                        openDrawer() // open the sidebar
                                                        shouldBreakLoop = true // stop processing further gestures
                                                    }
                                                } else if (origin == "SavedArticlesScreen") { // if this is happening on the saved articles screen
                                                    if (horizontalDragAmount > 0) { // if the user is swiping right
                                                        scope.launch {
                                                            swipeOffset.snapTo(
                                                                (swipeOffset.value + horizontalDragAmount).coerceIn(0f, itemWidth.value) // move the article item right
                                                            )
                                                        }
                                                        dragEvent.consume() // stop event from propogating to unwanted actions
                                                    }
                                                }
                                            }

                                            if (shouldBreakLoop) break // if we already handled the gesture (e.g., like opening the drawer), break out and stop processing
                                        }
                                    }
                                    //Log.d("GestureDebug", "About to iterate while loop")


                                } while (event.changes.any { it.pressed }) // continue looping while the user is still pressing the screen

                                //Log.d("GestureDebug", "Out of the while loop")
                                // step 5: cleanup


                                if (wasLongPress) { // if the user did a long press before releasing
                                    Log.d("GestureDebug", "Long press released")
                                    continue // skip tap handling because we don't want to go to next code block and accidentally check swipe
                                }

                                if (horizontalDrag != 0f) { // if there was any horizontal movement (swipe)
                                    scope.launch {
                                        if (origin == "HomeScreen" && swipeOffset.value < -0.4f * itemWidth.value) { // if swiped left past threshold on home screen
                                            Log.d("NewsScreen", "Swiped LEFT on article: ${article.title}")
                                            savedArticlesViewModel.saveArticle(article) // save the article
                                        } else if (origin == "SavedArticlesScreen" && swipeOffset.value > 0.4f * itemWidth.value) { // if swiped right past threshold on saved articles screen
                                            Log.d("NewsScreen", "Swiped RIGHT on article: ${article.title}")
                                            savedArticlesViewModel.deleteSavedArticle(article) // delete the article
                                            isTapValid.value = false // prevent accidental navigation
                                            awaitPointerEventScope {
                                                currentEvent.changes.forEach { it.consume() } // stops propogation - prevents this gesture from affecting other UI elements
                                            }
                                        }

                                        swipeOffset.animateTo(0f) // reset swipe position after action is taken
                                    }
                                }

                            } finally {
                                if (longPressJob.isActive) { // if the long press was still running
                                    longPressJob.cancel() // cancel it to stop unintended behaviour
                                }
                                isTapValid.value = true // reset it after entire interaction is complete
                            }
                        }
                    }
                }

            ,

            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            onClick = {

                if (isTapValid.value) { // only valid taps trigger navigation

                    if (origin == "HomeScreen" || origin == "SavedArticlesScreen") {
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

@Composable
fun ReactionBar(
    onReactionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .wrapContentWidth()
            .height(50.dp)
            .background(Color.White, shape = RoundedCornerShape(25.dp))
            .shadow(8.dp, shape = RoundedCornerShape(25.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val reactions = listOf("👍", "❤️", "🤯", "😮", "🤔", "😡", "😂")

            reactions.forEach { reaction ->
                Text(
                    text = reaction,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable { onReactionSelected(reaction) }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewReactionBar() {
    ReactionBar(
        onReactionSelected = { reaction -> Log.d("ReactionBar", "Selected: $reaction") }
    )
}

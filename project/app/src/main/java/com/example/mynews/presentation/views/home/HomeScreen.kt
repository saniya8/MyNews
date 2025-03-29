package com.example.mynews.presentation.views.home

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavHostController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mynews.presentation.viewmodel.home.NewsViewModel
import com.example.mynews.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.mynews.utils.AppScreenRoutes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.foundation.layout.height
import kotlinx.coroutines.CoroutineScope
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.example.mynews.presentation.viewmodel.home.SavedArticlesViewModel
import com.example.mynews.data.api.news.Article
import com.example.mynews.presentation.components.ScreenHeader
import com.example.mynews.presentation.viewmodel.goals.GoalsViewModel
import com.example.mynews.presentation.viewmodel.home.HomeViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt


fun todayDateText() : String {
    val today = LocalDate.now()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("MMMM d")) // "e.g., February 17"
    return formattedDate;
}


@Composable
fun HomeScreen(
    navController: NavHostController /*= rememberNavController()*/,
    homeViewModel: HomeViewModel,
    newsViewModel: NewsViewModel, // Keep here so NewsViewModel persists between navigation
    savedArticlesViewModel: SavedArticlesViewModel,
    goalsViewModel: GoalsViewModel,
    selectedCategory: MutableState<String?>,
    searchQuery: MutableState<String>,
) {

    val articles by newsViewModel.articles.observeAsState(emptyList())
    val articleReactions by homeViewModel.articleReactions.observeAsState(emptyMap())



    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) // Controls drawer open/close
    val scope = rememberCoroutineScope() // Required for controlling the drawer

    var activeReactionArticle by remember { mutableStateOf<Article?>(null)}
    var activeReactionArticleYCoord by remember { mutableFloatStateOf(0f) }

    //val reactionBarBounds = remember { mutableStateOf<Rect?>(null) }
    //var reactionBarBounds = remember { null as Rect? }
    var reactionBarBounds by remember { mutableStateOf<Rect?>(null) } //  does NOT trigger recompositions unless the variable is directly used inside a Composable UI element (which this isn't)
    val listState = rememberLazyListState()

    var selectedReaction = remember { mutableStateOf<String?>(null) } // track the selected reaction
    var isFetchingReaction by remember { mutableStateOf(false) } // New loading state

    var closeJob by remember { mutableStateOf<Job?>(null) } // to track closing job - for if user selects another reaction before automatically closure

    var showBiasLegend by remember { mutableStateOf(false) } // state to track visibility of legend dialog box

    /*val displayedReaction by remember {
        derivedStateOf { selectedReaction } // ensures immediate recomposition
    }

     */

    fun openDrawer() {
        scope.launch { drawerState.open() }
    }

    fun closeReactionBar() {
        activeReactionArticle = null
        selectedReaction.value = null
        isFetchingReaction = false
        closeJob = null
    }

    LaunchedEffect(Unit) {

        Log.i("FlickerBug", "In LaunchedEffect(Unit)")
        Log.i("FlickerBug", "searchQuery: ${searchQuery.value}")
        Log.i("FlickerBug", "selectedCategory: ${selectedCategory.value}")


        // searchQuery.value and selectedCategory.value
        // isNotEmpty && != null -> not possible, currently mutually exclusive
        // isEmpty && == null -> possible, fetch top headlines - in all three LaunchedEffects
        // isNotEmpty && null -> possible, fetch everything by search - in LaunchedEffect(Unit)
        // isEmpty && != null -> possible, fetch top headlines by category - in LaunchedEffect(S



        if (searchQuery.value.isNotEmpty() && selectedCategory.value == null) {
            // when recreating home screen, if there is a search query, it should load search
            // results (without requiring user to click search bar)
            Log.i("FlickerBug", "Fetching everything by search")
            newsViewModel.fetchEverythingBySearch(searchQuery.value)
        } else if (searchQuery.value.isEmpty() && selectedCategory.value == null) {
            newsViewModel.fetchTopHeadlines()
            Log.i("FlickerBug", "Fetching top headlines")
        }

        Log.i("FlickerBug", "----------------------")
    }

    // when selectedCategory.value change, it should fetch the top headlines by the category
    LaunchedEffect(selectedCategory.value) {

        Log.i("FlickerBug", "In LaunchedEffect(selectedCategory)")
        Log.i("FlickerBug", "selectedCategory: ${selectedCategory.value}")

        val category = selectedCategory.value // store a local stable copy

        if (category == null && searchQuery.value.isEmpty()) {
            // used to fetch top headlines if user deselects their selected cateogry
            // note: on initial creation of home screen, this will result in duplicate api
            // call, one in LaunchedEffect(Unit) and one here
            newsViewModel.fetchTopHeadlines(forceFetch = true)
        } else if (category != null && searchQuery.value.isEmpty()) {
            newsViewModel.fetchTopHeadlinesByCategory(category)
        }
        Log.i("FlickerBug", "----------------------")
    }

    // when the searchQuery.value changes, ONLY if become empty should it fetch the top headlines
    LaunchedEffect(searchQuery.value) {
        Log.i("SearchQuery Value", "In Launched Effect for SearchQuery: ${searchQuery.value}")
        Log.i("SearchQuery Value", "Value is: ${searchQuery.value}")
        Log.i("SearchQuery Value", "isEmpty is: ${searchQuery.value.isEmpty()}")

        Log.i("FlickerBug", "In LaunchedEffect(searchQuery.value)")
        Log.i("FlickerBug", "searchQuery: ${searchQuery.value}")
        if (searchQuery.value.isEmpty() && selectedCategory.value == null) {
            // used to fetch top headlines if user clears their search
            // note: on initial creation of home screen, this will result in duplicate api
            // call, one in LaunchedEffect(Unit) and one here and one in LaunchedEffect(selectedCategory)
            // try: move this to the onValueChange if the value length is 0
            newsViewModel.fetchTopHeadlines(forceFetch = true) // Re-fetch when search is cleared
        } // if it's not empty,
        // if within home screen and click search icon, then it fetches everything by search on click
        // if going back to home screen (new instance of home screen created), then it
        // fetches everything by search in LaunchedEffect(Unit)
        Log.i("FlickerBug", "----------------------")
    }

    /*
    LaunchedEffect(activeReactionArticle) {
        activeReactionArticle?.let { article ->

            /*
            val storedReaction = articleReactions[article.url]

            // this launched effect is use as a backup to ensure UI updates in real-time
            // it only runs if onLongPressRelease was delayed in updating selectedReaction


            if (selectedReaction.value != storedReaction) { // meaning onLongPressRelease did not update selectedReaction in time to be the stored reaction
                Log.d("GestureDebug", "Syncing selectedReaction: $selectedReaction -> $storedReaction")
                selectedReaction.value = storedReaction
            }

             */

            selectedReaction.value = articleReactions[article.url] //  immediately set UI

            newsViewModel.fetchReaction(article) { reaction ->
                if (reaction != selectedReaction.value) {
                    selectedReaction.value = reaction // ensure accuracy without flicker
                }
            }

        }
    }

     */


    // CL: Add a remember state to track drawer drag gesture
    val isDraggingDrawer = remember { mutableStateOf(false) }
    // CL: Add a remember state to track scroll gesture
    val isScrolling = remember { mutableStateOf(false) }



    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                drawerState = drawerState,
                scope = scope,
                selectedCategory = selectedCategory, //.value, // Pass selected category
                searchQuery = searchQuery,
            )
        }
    ) {


        Box(
            modifier = Modifier
                .fillMaxSize()

                // version 3
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            // Wait for first pointer down event
                            val firstDown = awaitFirstDown(requireUnconsumed = false)
                            val touchPosition = firstDown.position
                            var dragStartTime = System.currentTimeMillis()
                            var totalDragX = 0f
                            var totalDragY = 0f
                            var isGestureDetermined = false
                            var isScrollGesture = false
                            var isDrawerGesture = false
                            var lockScrollMode = false

                            // If reaction bar is open, close it on any touch outside the bar itself
                            // (The bar itself has its own clickable modifier that will stop propagation)
                            if (activeReactionArticle != null) {

                                val bounds = reactionBarBounds

                                if (bounds != null && bounds.contains(touchPosition)) {
                                    Log.d("GestureDebug", "Touch inside the reaction bar - allow selection of reactions")
                                } else {

                                    Log.d(
                                        "GestureDebug",
                                        "Touch detected while reaction bar is open"
                                    )
                                    closeReactionBar()

                                    // Wait for up event and consume it
                                    do {
                                        val event = awaitPointerEvent()
                                        event.changes.forEach { it.consume() }
                                    } while (event.changes.any { it.pressed })

                                    continue
                                }

                            }

                            // Process drag events until pointer up
                            while (true) {
                                val event = awaitPointerEvent()
                                val mainPointer = event.changes.firstOrNull() ?: break

                                if (!mainPointer.pressed) break

                                val positionChange = mainPointer.positionChange()
                                totalDragX += positionChange.x
                                totalDragY += positionChange.y

                                // Determine the gesture type early
                                if (!isGestureDetermined && (abs(totalDragX) > 10f || abs(totalDragY) > 10f)) {
                                    isGestureDetermined = true

                                    // Horizontal right drag (for drawer) needs to be significantly stronger
                                    // than vertical to avoid confusion with scroll
                                    /*
                                    if (abs(totalDragX) > abs(totalDragY) * 2f && totalDragX > 0) {
                                        isDrawerGesture = true
                                        Log.d("GestureDebug", "Detected drawer drag")
                                    } else if (abs(totalDragY) > abs(totalDragX)) {
                                        isScrollGesture = true
                                        Log.d("GestureDebug", "Detected scroll gesture")
                                    }

                                     */

                                    if (abs(totalDragY) > abs(totalDragX)) {
                                        isScrollGesture = true
                                        lockScrollMode = true
                                    } else if (!lockScrollMode && abs(totalDragX) > abs(totalDragY) * 2f && totalDragX > 0) {
                                        isDrawerGesture = true
                                    }

                                }

                                // For scrolling, don't consume events to let them propagate
                                //if (isScrollGesture) {
                                //    // Intentionally not consuming events
                               // }

                                if (lockScrollMode) {
                                    continue
                                }

                                // Handle drawer opening if in drawer gesture mode
                                if (isDrawerGesture) {
                                    // Consume the event to prevent other handlers from processing it
                                    mainPointer.consume()

                                    if (totalDragX > 50f && drawerState.isClosed) {
                                        scope.launch { drawerState.open() }
                                        break
                                    }
                                }


                            }
                        }
                    }
                }

        )

        {

            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {


                        /*
                        // unstandardized
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                            //.padding(horizontal = 16.dp)
                        ) {

                            // Info Icon - Pinned to the top left
                            IconButton(
                                onClick = { showBiasLegend = true }, // open the dialog box
                                modifier = Modifier.align(Alignment.TopStart) // stays in the top-left
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Info",
                                    tint = CaptainBlue
                                )
                            }

                            // "My News" - Exactly centered
                            Text(
                                text = "My News",
                                fontWeight = FontWeight.Bold,
                                color = CaptainBlue,
                                fontSize = 25.sp,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center)
                            )


                            // Saved Articles Icon - Pinned to the top right
                            IconButton(
                                onClick = {
                                    navController.navigate(AppScreenRoutes.SavedArticlesScreen.route) {
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd) // stays in the top-right
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    //imageVector = Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Saved Articles",
                                    tint = CaptainBlue
                                )
                            }

                        }

                         */

                        // standardized
                        ScreenHeader(
                            useTopPadding = false,
                            title = "My News",
                            leftContent = {
                                IconButton(
                                    onClick = { showBiasLegend = true },
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = "Info",
                                        tint = CaptainBlue
                                    )
                                }
                            },
                            rightContent = {
                                IconButton(
                                    onClick = {
                                        navController.navigate(AppScreenRoutes.SavedArticlesScreen.route) {
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "Saved Articles",
                                        tint = CaptainBlue
                                    )
                                }
                            }
                        )

                        // show Bias Legend Dialog
                        if (showBiasLegend) {
                            BiasLegendDialog(onDismiss = { showBiasLegend = false })
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Today's date
                        Text(
                            text = todayDateText(),
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = CaptainBlue,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.SansSerif
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SearchAndFilter(
                            newsViewModel = newsViewModel,
                            drawerState = drawerState,
                            scope = scope,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery
                        )


                        Box(modifier = Modifier.fillMaxSize()) {
                            NewsScreen(
                                navController = navController,
                                newsViewModel = newsViewModel,
                                savedArticlesViewModel = savedArticlesViewModel,
                                goalsViewModel = goalsViewModel,
                                articles = articles,
                                origin = "HomeScreen",
                                openDrawer = ::openDrawer,
                                onLongPressRelease = { article, yCoord -> // callback from ArticleItem
                                    Log.d(
                                        "GestureDebug",
                                        "Updating reaction bar state: ${article.title} at Y = $yCoord"
                                    ) // debug

                                    val cachedReaction = articleReactions[article.url] // debug
                                    Log.d("ReactionDebug", "Setting selectedReaction to: $cachedReaction") // debug

                                    selectedReaction.value = articleReactions[article.url] // cached reaction
                                    activeReactionArticle = article // store active article
                                    activeReactionArticleYCoord = yCoord // store y coordinate for correct placement
                                    isFetchingReaction = true // Start loading

                                    // fetch the selected reaction for initial UI update when reaction bar loads

                                    // NEWFIX
                                    // set this immediately based on what is in articleReactions to immediately
                                    // update UI

                                    homeViewModel.fetchReaction(article) { reaction ->
                                        Log.d("ReactionDebug", "Selected ReactionValue pre-fetch: $selectedReaction.value")
                                        Log.d(
                                            "ReactionDebug",
                                            "Fetched reaction: $reaction for ${article.title}"
                                        )
                                        if (reaction !== selectedReaction.value) { // NEWFIX
                                            // need the above to immediately update UI without delay
                                            // but this is here in case what's in firestore is for some small
                                            // chance different than the cached reaction (e.g., local cache is
                                            // different)
                                            selectedReaction.value = reaction // store the fetched reaction

                                        }
                                        isFetchingReaction = false // always reset when fetch completes; this will trigger reaction bar to open and also trigger animation
                                    }

                                },
                                listState = listState,
                            ) // Display news


                            // render the Reaction Bar if an article is long pressed and released
                            // CL: Fixed reaction bar handling with proper positioning and event bubbling prevention
                            if (activeReactionArticle != null) {

                                //val currentReaction = articleReactions[activeReactionArticle!!.url] // get the selected reaction -> for live updates during changes to selection
                                //selectedReaction.value = articleReactions[activeReactionArticle!!.url]
                                // CL: Semi-transparent overlay to capture clicks outside reaction bar


                                Box(

                                    // works - pre dimming entire screen
                                    modifier = Modifier
                                        .fillMaxSize()
                                        //.background(Color.Black.copy(alpha = 0.3f)) // dim
                                        .zIndex(5f)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null // No ripple effect
                                        ) {
                                            Log.d("GestureDebug", "CL: Overlay clicked - closing reaction bar")
                                            closeReactionBar()
                                        }

                                )




                                // works - without animation
                                if (!isFetchingReaction) {

                                    // CL: Position the reaction bar at the correct Y coordinate
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .zIndex(10f)
                                    ) {
                                        val density =
                                            LocalDensity.current // get density in a Composable-safe way
                                        val itemHeightPx =
                                            with(density) { 130.dp.toPx() }.toInt() // convert once before .offset {}
                                        ReactionBar(
                                            article = activeReactionArticle!!,
                                            homeViewModel = homeViewModel,
                                            selectedReaction = selectedReaction,
                                            onReactionSelected = { reaction ->

                                                selectedReaction.value = reaction
                                                // CL: Properly log the reaction and close after delay
                                                Log.d(
                                                    "GestureDebug",
                                                    "CL: Selected reaction: $reaction for ${activeReactionArticle?.title}"
                                                )

                                                // cancel any close job if it is running
                                                closeJob?.cancel()

                                                closeJob = scope.launch {
                                                    // CL: Consider showing some feedback to user here
                                                    delay(2000) // shorter delay for better UX
                                                    closeReactionBar()
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth(0.8f) // 80% width for better appearance
                                                .wrapContentHeight()
                                                .align(Alignment.TopCenter) // center horizontally

                                                //WORKS EXCEPT FOR EDGE CASE WHERE SCROLLING AND REACTING TO HALF CUT OFF ARTICLE
                                                // CL: Properly position at the tap location
                                                .offset {
                                                    // CL: Calculate position based on the recorded Y coordinate
                                                    IntOffset(
                                                        0,
                                                        activeReactionArticleYCoord.roundToInt() - 610 // offset up slightly was -60 as offset
                                                    )
                                                }

                                                .onGloballyPositioned { layoutCoordinates ->
                                                    reactionBarBounds =
                                                        layoutCoordinates.boundsInRoot() // store reaction bar position

                                                }

                                        )
                                    }
                                }

                            } // end of if activeReactionArticle != null

                        }
                    }
                } // end of scaffold

            }
        }
    } // end of modal navigation drawer
}

@Composable
fun SearchAndFilter(newsViewModel: NewsViewModel,
                    drawerState: DrawerState,
                    scope: CoroutineScope,
                    selectedCategory: MutableState<String?>,
                    searchQuery: MutableState<String>
) {

    Row (
        modifier = Modifier.fillMaxWidth()
            //.height(50.dp) // Height for testing
            //.background(Color(0xFFFFC0CB)) // Light Pink Color for testing
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically

    ) {

        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = {
                searchQuery.value = it
                //if (it.isEmpty()) {
                //    newsViewModel.fetchTopHeadlines()
                //} // else - should have to click the trailingIcon button (so it doesn't
                  // keep giving results for every incremental value change - too many
                  // fetch requests
            },
            modifier = Modifier
                .padding(8.dp)
                .height(50.dp) // slightly increased height
                //.fillMaxWidth()
                .weight(1f) // this instead of fillMaxWidth makes the search
                // bar fill the remaining width, but then it looks weird
                // because the My News and Date at the top look off centered
                // even though they are not
                .clip(CircleShape), // clip to rounded edges
            shape = CircleShape, // ensures rounded edges
            textStyle = TextStyle(fontSize = 16.sp), // Adjust font size if needed
            singleLine = true, // Prevents multiline input
            maxLines = 1,
            trailingIcon = {
                IconButton(
                    onClick = { // only search onClick
                        if(searchQuery.value.isNotEmpty()) {
                            if (selectedCategory.value != null) { // if any categories are selected
                                selectedCategory.value = null; // clear any category selections
                                // check that it's not null because if it was null and
                                // you set it to null again, it would trigger
                                // LaunchedEffect(selectedCategory.value) which would
                                // unnecessarily trigger another API call
                            }
                            newsViewModel.fetchEverythingBySearch(searchQuery.value)
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon")
                }
            }

            //placeholder = { Text("Search...") }, // placeholder text
        )

        IconButton(onClick = { scope.launch { drawerState.open() } }) {
            Icon(Icons.Default.Tune, contentDescription = "Filter Menu")
        }

    }

}

@Composable
fun DrawerContent(
    drawerState: DrawerState,
    scope: CoroutineScope,
    selectedCategory: MutableState<String?>,
    searchQuery: MutableState<String>
) {
    // excluding General since General is just the same as the regular news that you
    // pull from API
    // categories provided in the documentation
    val categories = listOf("Business", "Entertainment", "Health", "Science", "Sports", "Technology")

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .width(250.dp)
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        item {
            // close (X) Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { scope.launch { drawerState.close() } }) {
                    Icon(Icons.Default.Close, contentDescription = "Close Drawer")
                }
            }
        }

        item {
            Text(
                text = "Filter My News",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Category",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Create buttons for each category
        // Note: this logic only permits single selection of category
        // Multiple selection of categories = too many API calls and too complex since
        // API does not allow passing multiple categories
        items(categories) { category ->
            val isSelected = selectedCategory.value == category
            val backgroundColor = if (isSelected) Color(0xFF90CAF9) else Color(0xFFBBDEFB) // Subtle contrast
            val borderColor = if (isSelected) Color(0xFF64B5F6) else Color.Transparent // Light blue border when selected

            Button(
                onClick = {

                    if(searchQuery.value.isNotEmpty()) { // if there is a search query
                        searchQuery.value = ""; // clear the search query
                    }

                    val newCategory = if (isSelected) null else category // Toggle selection
                    //onCategorySelected(newCategory)
                    selectedCategory.value = newCategory // Notify HomeScreen of change
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                border = BorderStroke(2.dp, borderColor)
            ) {
                Text(text = category, color = Color.Black)
            }
        }
    }
}


/*
// works - no animation
@Composable
fun ReactionBar(
    article: Article,
    newsViewModel: NewsViewModel,
    selectedReaction: MutableState<String?>,
    onReactionSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .wrapContentWidth()
            .height(50.dp)
            .background(Color.White, shape = RoundedCornerShape(25.dp)) // i think can remove this
            .shadow(8.dp, shape = RoundedCornerShape(25.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White), // Reaction Bar is white
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

                val isSelected = reaction == selectedReaction.value // true if the reaction is the selectedReaction, false otherwise


                Text(
                    text = reaction,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            Log.d("GestureDebug", "isSelected for ${selectedReaction} is: ${isSelected}")
                            val newReaction = if (isSelected) null else reaction // toggle selection
                            onReactionSelected(newReaction) // update the selectedReaction.value, close the reaction bar after 1 sec delay, do this first so UI doesnt flicker
                            newsViewModel.updateReaction(article, newReaction) // then update in backend

                        }
                        .background(
                            if (isSelected) Color(0xFFD2E4FF) else Color.Transparent, // highlight selected reaction to indicate selected
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(4.dp)
                )
            }
        }
    }
}

 */



// works - wave
@Composable
fun ReactionBar(
    article: Article,
    homeViewModel: HomeViewModel,
    selectedReaction: MutableState<String?>,
    onReactionSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation state for the bar
    var shouldAnimate by remember { mutableStateOf(false) }
    var shouldJiggle by remember { mutableStateOf(false) } // Trigger jiggle after bounce

    // Trigger animations sequentially
    LaunchedEffect(Unit) {
        shouldAnimate = true // Start bounce immediately
        delay(520) // Wait for bounce to finish (almost matches durationMillis)
        shouldJiggle = true // Start jiggle right after
    }

    // Bar bounce-in effect (unchanged)
    val bounceScale by animateFloatAsState(
        targetValue = if (shouldAnimate) 1f else 0.85f,
        animationSpec = keyframes {
            durationMillis = 600
            0.85f at 0
            1.12f at 150 with FastOutSlowInEasing
            0.98f at 300 with FastOutSlowInEasing
            1.03f at 450 with FastOutSlowInEasing
            1.00f at 600 with FastOutSlowInEasing
        },
        label = "BounceScale"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (shouldAnimate) 0f else 40f,
        animationSpec = keyframes {
            durationMillis = 600
            40f at 0
            -6f at 250 with FastOutSlowInEasing
            0f at 500 with FastOutSlowInEasing
        },
        label = "OffsetY"
    )

    Box(
        modifier = modifier
            .offset(y = offsetY.dp)
            .scale(bounceScale)
    ) {
        Card(
            modifier = Modifier
                .wrapContentWidth()
                .height(50.dp)
                .shadow(8.dp, shape = RoundedCornerShape(25.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val reactions = listOf("👍", "❤️", "🤯", "😮", "🤔", "😢", "🥹", "😡", "😂")
                reactions.forEachIndexed { index, reaction ->
                    val isSelected = reaction == selectedReaction.value

                    // Icon jiggle effect (starts after bounce)
                    val jiggleScale by animateFloatAsState(
                        targetValue = if (shouldJiggle) 1f else 0.9f,
                        animationSpec = keyframes {
                            durationMillis = 600 // Short, snappy jiggle
                            0.9f at 0 // Start slightly smaller
                            1.3f at 150 with FastOutSlowInEasing // Big pop
                            0.85f at 300 with FastOutSlowInEasing // Dip
                            1.1f at 450 with FastOutSlowInEasing // Small rebound
                            1.0f at 600 with FastOutSlowInEasing // Settle
                            delayMillis = index * 50 // Staggered ripple
                        },
                        label = "JiggleScale"
                    )

                    Text(
                        text = reaction,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                Log.d("GestureDebug", "isSelected for ${selectedReaction.value} is: $isSelected")
                                val newReaction = if (isSelected) null else reaction
                                onReactionSelected(newReaction)
                                homeViewModel.updateReaction(article, newReaction)
                            }
                            .background(
                                if (isSelected) Color(0xFFD2E4FF) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp)
                            .scale(jiggleScale) // Apply jiggle scale
                    )
                }
            }
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun PreviewReactionBar() {
    ReactionBar(
        onReactionSelected = { reaction -> Log.d("ReactionBar", "Selected: $reaction") }
    )
}

 */

@Composable
fun BiasLegendDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Bias Guide",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, //
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column() {
                Text(
                    text = "Articles have a flag in their top-right corner indicating their political bias.",
                    textAlign = TextAlign.Center, // center the explanation text
                    modifier = Modifier.padding(bottom = 8.dp) // space before legend
                )
                BiasLegendItem("Left", BiasColors.Left)
                BiasLegendItem("Lean Left", BiasColors.LeanLeft)
                BiasLegendItem("Center", BiasColors.Center)
                BiasLegendItem("Lean Right", BiasColors.LeanRight)
                BiasLegendItem("Right", BiasColors.Right)
                BiasLegendItem("Mixed", BiasColors.Mixed)
                BiasLegendItem("Neutral", BiasColors.Neutral)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}


@Composable
fun BiasLegendItem(label: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp) // small color box
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp)) // space between box & text
        Text(text = label, fontSize = 16.sp)
    }
}








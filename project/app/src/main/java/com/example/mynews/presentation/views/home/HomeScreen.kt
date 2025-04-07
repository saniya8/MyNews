package com.example.mynews.presentation.views.home

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.example.mynews.domain.entities.Article
import com.example.mynews.presentation.components.ScreenHeader
import com.example.mynews.presentation.theme.BiasColors
import com.example.mynews.presentation.theme.CaptainBlue
import com.example.mynews.presentation.theme.SkyBlue
import com.example.mynews.presentation.viewmodel.goals.GoalsViewModel
import com.example.mynews.presentation.viewmodel.home.HomeViewModel
import com.example.mynews.presentation.viewmodel.home.NewsViewModel
import com.example.mynews.presentation.viewmodel.home.SavedArticlesViewModel
import com.example.mynews.utils.AppScreenRoutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt


private fun todayDateText() : String {
    val today = LocalDate.now()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("MMMM d")) // "e.g., February 17"
    return formattedDate;
}


@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    newsViewModel: NewsViewModel,
    savedArticlesViewModel: SavedArticlesViewModel,
    goalsViewModel: GoalsViewModel,
    searchQuery: MutableState<String>,
    selectedCategory: MutableState<String?>,
    selectedCountry: MutableState<String?>,
    selectedDateRange: MutableState<String?>,
) {

    val articles by newsViewModel.articles.observeAsState(emptyList())
    val articleReactions by homeViewModel.articleReactions.observeAsState(emptyMap())

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) // Controls drawer open/close
    val scope = rememberCoroutineScope() // Required for controlling the drawer

    var activeReactionArticle by remember { mutableStateOf<Article?>(null)}
    var activeReactionArticleYCoord by remember { mutableFloatStateOf(0f) }

    var reactionBarBounds by remember { mutableStateOf<Rect?>(null) }
    val listState = rememberLazyListState()

    var selectedReaction = remember { mutableStateOf<String?>(null) } // track the selected reaction
    var isFetchingReaction by remember { mutableStateOf(false) } // new loading state

    var closeJob by remember { mutableStateOf<Job?>(null) } // to track closing job - for if user selects another reaction before automatically closure

    var showBiasLegend by remember { mutableStateOf(false) } // to track visibility of legend dialog box

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

        // each time screen is created or re-entered, fetch the news to display the most up-to-date news

        if (searchQuery.value.isEmpty() && selectedCategory.value == null && selectedCountry.value == null && selectedDateRange.value == null) {
            newsViewModel.fetchTopHeadlines()
            Log.i("FlickerBug", "Fetching top headlines")
        } else if (searchQuery.value.isNotEmpty() && selectedCategory.value == null && selectedCountry.value == null && selectedDateRange.value == null) {
            Log.i("FlickerBug", "Fetching everything by search")
            newsViewModel.fetchEverythingBySearch(searchQuery.value)
        } else if (searchQuery.value.isEmpty() && selectedCategory.value != null && selectedCountry.value == null && selectedDateRange.value == null) {
            Log.i("FlickerBug", "Fetching top headlines by category: ${selectedCategory.value}")
            newsViewModel.fetchTopHeadlinesByCategory(selectedCategory.value!!)
        } else if (searchQuery.value.isEmpty() && selectedCategory.value == null && selectedCountry.value != null && selectedDateRange.value == null) {
            Log.i("FlickerBug", "Fetching top headlines by country: ${selectedCountry.value}")
            newsViewModel.fetchTopHeadlinesByCountry(selectedCountry.value!!)
        } else if (searchQuery.value.isEmpty() && selectedCategory.value == null && selectedCountry.value == null && selectedDateRange.value != null) {
            Log.i("FlickerBug", "Fetching top headlines by date range: ${selectedDateRange.value}")
            newsViewModel.fetchEverythingByDateRange(selectedDateRange.value!!)
        }

    }

    // when the searchQuery.value changes, ONLY if become empty should it fetch the top headlines
    LaunchedEffect(searchQuery.value) {

        if (searchQuery.value.isEmpty() && selectedCategory.value == null && selectedCountry.value == null && selectedDateRange.value == null) {
            newsViewModel.fetchTopHeadlines(forceFetch = true) // re-fetch when search is cleared
        }
    }

    // when selectedCategory.value change, it should fetch the top headlines by the category
    LaunchedEffect(selectedCategory.value) {
        val category = selectedCategory.value // store a local stable copy
        if (category == null && searchQuery.value.isEmpty() && selectedCountry.value == null && selectedDateRange.value == null) {
            newsViewModel.fetchTopHeadlines(forceFetch = true)
        } else if (category != null && searchQuery.value.isEmpty() && selectedCountry.value == null && selectedDateRange.value == null) {
            newsViewModel.fetchTopHeadlinesByCategory(category)
        }
    }


    // when selectedCountry.value change, it should fetch the top headlines by the country
    LaunchedEffect(selectedCountry.value) {

        val country = selectedCountry.value // store a local stable copy
        if (country == null && searchQuery.value.isEmpty() && selectedCategory.value == null && selectedDateRange.value == null) {
            newsViewModel.fetchTopHeadlines(forceFetch = true)
        } else if (country != null && searchQuery.value.isEmpty() && selectedCategory.value == null && selectedDateRange.value == null) {
            newsViewModel.fetchTopHeadlinesByCountry(country)
        }
    }

    // when selectedDateRange.value change, it should fetch the top headlines by the date range
    LaunchedEffect(selectedDateRange.value) {

        val dateRange = selectedDateRange.value // store a local stable copy
        if (dateRange == null && searchQuery.value.isEmpty() && selectedCategory.value == null && selectedCountry.value == null) {
            newsViewModel.fetchTopHeadlines(forceFetch = true)
        } else if (dateRange != null && searchQuery.value.isEmpty() && selectedCategory.value == null && selectedCountry.value == null) {
            newsViewModel.fetchEverythingByDateRange(dateRange)
        }
    }

    ModalNavigationDrawer( // side panel filter bar
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                drawerState = drawerState,
                scope = scope,
                searchQuery = searchQuery,
                selectedCategory = selectedCategory,
                selectedCountry = selectedCountry,
                selectedDateRange = selectedDateRange,
            )
        }
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()

                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            // Wait for first pointer down event
                            val firstDown = awaitFirstDown(requireUnconsumed = false)
                            val touchPosition = firstDown.position
                            var totalDragX = 0f
                            var totalDragY = 0f
                            var isGestureDetermined = false
                            var isDrawerGesture = false
                            var lockScrollMode = false

                            // if reaction bar is open, close it on any touch outside the bar itself
                            // (the bar itself has its own clickable modifier that will stop propagation)
                            if (activeReactionArticle != null) {

                                val bounds = reactionBarBounds

                                if (bounds != null && bounds.contains(touchPosition)) {
                                    Log.d(
                                        "GestureDebug",
                                        "Touch inside the reaction bar - allow selection of reactions"
                                    )
                                } else {

                                    Log.d(
                                        "GestureDebug",
                                        "Touch detected while reaction bar is open"
                                    )
                                    closeReactionBar()

                                    // wait for up event and consume it
                                    do {
                                        val event = awaitPointerEvent()
                                        event.changes.forEach { it.consume() }
                                    } while (event.changes.any { it.pressed })

                                    continue
                                }

                            }

                            // process drag events until pointer up
                            while (true) {
                                val event = awaitPointerEvent()
                                val mainPointer = event.changes.firstOrNull() ?: break

                                if (!mainPointer.pressed) break

                                val positionChange = mainPointer.positionChange()
                                totalDragX += positionChange.x
                                totalDragY += positionChange.y

                                // determine the gesture type early
                                if (!isGestureDetermined && (abs(totalDragX) > 10f || abs(totalDragY) > 10f)) {
                                    isGestureDetermined = true

                                    // horizontal right drag (for drawer) needs to be significantly stronger
                                    // than vertical to avoid confusion with scroll

                                    if (abs(totalDragY) > abs(totalDragX)) {
                                        lockScrollMode = true
                                    } else if (!lockScrollMode && abs(totalDragX) > abs(totalDragY) * 2f && totalDragX > 0) {
                                        isDrawerGesture = true
                                    }

                                }

                                if (lockScrollMode) {
                                    continue
                                }

                                // handle drawer opening if in drawer gesture mode
                                if (isDrawerGesture) {
                                    // consume the event to prevent other handlers from processing it
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

                        // today's date
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
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            selectedCountry = selectedCountry,
                            selectedDateRange = selectedDateRange,
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
                                    isFetchingReaction = true // start loading

                                    // fetch the selected reaction for initial UI update when reaction bar loads
                                    // set this immediately based on what is in articleReactions to immediately

                                    homeViewModel.fetchReaction(article) { reaction ->
                                        Log.d("ReactionDebug", "Selected ReactionValue pre-fetch: $selectedReaction.value")
                                        Log.d(
                                            "ReactionDebug",
                                            "Fetched reaction: $reaction for ${article.title}"
                                        )
                                        if (reaction !== selectedReaction.value) {
                                            selectedReaction.value = reaction // store the fetched reaction

                                        }
                                        isFetchingReaction = false // always reset when fetch completes; this will trigger reaction bar to open and also trigger animation
                                    }

                                },
                                listState = listState,
                            ) // display news


                            // render the Reaction Bar if an article is long pressed and released
                            if (activeReactionArticle != null) {

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .zIndex(5f)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            Log.d(
                                                "GestureDebug",
                                                "Overlay clicked - closing reaction bar"
                                            )
                                            closeReactionBar()
                                        }
                                )

                                if (!isFetchingReaction) {
                                    // position the reaction bar at the correct Y coordinate
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .zIndex(10f)
                                    ) {
                                        val density =
                                            LocalDensity.current // get density in a Composable-safe way
                                        ReactionBar(
                                            selectedReaction = selectedReaction,
                                            onReactionSelected = { reaction ->

                                                selectedReaction.value = reaction
                                                homeViewModel.updateReaction(activeReactionArticle!!, reaction)

                                                Log.d(
                                                    "GestureDebug",
                                                    "Selected reaction: $reaction for ${activeReactionArticle?.title}"
                                                )

                                                // cancel any close job if it is running
                                                closeJob?.cancel()

                                                closeJob = scope.launch {
                                                    delay(2000) // shorter delay for better UX
                                                    closeReactionBar()
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth(0.8f) // 80% width for better appearance
                                                .wrapContentHeight()
                                                .align(Alignment.TopCenter) // center horizontally
                                                // properly position at the tap location
                                                .offset {
                                                    // calculate position based on the recorded Y coordinate
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
                    searchQuery: MutableState<String>,
                    selectedCategory: MutableState<String?>,
                    selectedCountry: MutableState<String?>,
                    selectedDateRange: MutableState<String?>,
) {

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically

    ) {

        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = {
                searchQuery.value = it
            },
            modifier = Modifier
                .padding(8.dp)
                .height(50.dp) // slightly increased height
                .weight(1f)
                .clip(CircleShape), // clip to rounded edges
            shape = CircleShape, // ensures rounded edges
            textStyle = TextStyle(fontSize = 16.sp),
            singleLine = true, // prevents multiline input
            maxLines = 1,
            trailingIcon = {
                IconButton(
                    onClick = { // only search onClick
                        if(searchQuery.value.isNotEmpty()) {
                            if (selectedCategory.value != null) { // if any categories are selected
                                selectedCategory.value = null; // clear any category selections
                            }
                            if (selectedCountry.value != null) {
                                selectedCountry.value = null; // clear any country selections
                            }

                            if (selectedDateRange.value != null) {
                                selectedDateRange.value = null; // clear any date range selections
                            }

                            newsViewModel.fetchEverythingBySearch(searchQuery.value)
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon")
                }
            }
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
    searchQuery: MutableState<String>,
    selectedCategory: MutableState<String?>,
    selectedCountry: MutableState<String?>,
    selectedDateRange: MutableState<String?>,
) {

    // mutable state for managing the dropdown menu (country selection)
    var expanded by remember { mutableStateOf(false) } // Dropdown expanded state
    var textFieldPosition by remember { mutableStateOf(Offset(0f, 0f)) } // To store the global position of the TextField
    var textFieldHeight by remember { mutableStateOf(0f) } // To store the height of the TextField

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .width(250.dp)
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {


        item {

            Spacer(modifier = Modifier.height(7.dp))

            // Row to hold "Filter My News" and "X" icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter My News",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { scope.launch { drawerState.close() } }) {
                    Icon(Icons.Default.Close, contentDescription = "Close Drawer")
                }
            }

            Spacer(modifier = Modifier.height(15.dp))
        }

        // Filtering by category

        item {

            // Row to hold "Category" text and "Clear" button
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                    //.height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween, // Space between "Category" and "Clear"
                verticalAlignment = Alignment.Bottom // Align bottoms of "Category" and "Clear"
            ) {

                Text(
                    text = "Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                val shouldShowClear = selectedCategory.value != null

                if (shouldShowClear) {

                    Text(
                        text = "Clear",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable(
                                onClick = {

                                    if (searchQuery.value.isNotEmpty()) { // if there is a search query
                                        searchQuery.value = "" // clear the search query
                                    }

                                    if (selectedCountry.value != null) {
                                        selectedCountry.value = null
                                    }

                                    if (selectedDateRange.value != null) {
                                        selectedDateRange.value = null
                                    }

                                    selectedCategory.value = null // Set to null to clear the filter
                                    expanded = false // Close the dropdown
                                },
                                indication = LocalIndication.current, // Apply ripple effect
                                interactionSource = remember { MutableInteractionSource() } // Interaction source for ripple
                            )
                            .align(Alignment.Bottom) // Ensure it's aligned at the bottom
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

        }

        items(categories) { category ->
            val isSelected = selectedCategory.value == category
            val backgroundColor = if (isSelected) Color(0xFF90CAF9) else Color(0xFFBBDEFB) // Subtle contrast
            val borderColor = if (isSelected) Color(0xFF64B5F6) else Color.Transparent // Light blue border when selected

            Button(
                onClick = {

                    if (searchQuery.value.isNotEmpty()) { // if there is a search query
                        searchQuery.value = "" // clear the search query
                    }

                    if (selectedCountry.value != null) { // if there is a selected country
                        selectedCountry.value = null // clear the country
                    }

                    if (selectedDateRange.value != null) { // if there is a selected date range
                        selectedDateRange.value = null // clear the date range
                    }

                    val newCategory = if (isSelected) null else category // Toggle selection
                    selectedCategory.value = newCategory // Notify HomeScreen of change
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                border = BorderStroke(2.dp, borderColor)
            ) {
                Text(text = category, color = Color.Black)
            }
        }

        // Filtering by Country

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {

            // Row to hold "Country" text and "Clear" button
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Space between "Country" and "Clear"
                verticalAlignment = Alignment.Bottom // Align bottoms of "Country" and "Clear"
            ) {

                Text(
                    text = "Country",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                val shouldShowClear = selectedCountry.value != null

                if (shouldShowClear) {

                    Text(
                        text = "Clear",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable(
                                onClick = {

                                    if (searchQuery.value.isNotEmpty()) { // if there is a search query
                                        searchQuery.value = "" // clear the search query
                                    }

                                    if (selectedCategory.value != null) {
                                        selectedCategory.value = null
                                    }

                                    if (selectedDateRange.value != null) { // if there is a selected date range
                                        selectedDateRange.value = null // clear the date range
                                    }

                                    selectedCountry.value = null // Set to null to clear the filter
                                    expanded = false // Close the dropdown
                                },
                                indication = LocalIndication.current, // Apply ripple effect
                                interactionSource = remember { MutableInteractionSource() } // Interaction source for ripple
                            )
                            .align(Alignment.Bottom) // Ensure it's aligned at the bottom
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

        }

        // Dropdown for country selection

        item {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded } // Toggle dropdown on entire field click
                    .onGloballyPositioned { coordinates ->
                        textFieldPosition =
                            coordinates.positionInWindow() // Get the global position
                        textFieldHeight = coordinates.size.height.toFloat() // Get the height
                    }
            ) {
                OutlinedTextField(
                    value = selectedCountry.value?.let { countries[it] } ?: "Select a country",
                    onValueChange = {}, // Read-only
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 4.dp),
                    trailingIcon = {
                        Icon(
                            imageVector =
                            if (expanded){
                                Icons.Default.ArrowDropUp
                            } else {
                                Icons.Default.ArrowDropDown
                            },
                            contentDescription = "Arrow",
                            tint = CaptainBlue,
                            modifier = Modifier
                                .size(30.dp)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Black,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        disabledIndicatorColor = Color.White,
                        cursorColor = Color.Transparent
                    ),
                    enabled = false, // prevents keyboard from popping up
                )

            }

            // custom Popup for the dropdown
            if (expanded) {
                Popup(
                    onDismissRequest = { expanded = false },
                    properties = PopupProperties(
                        focusable = true,
                        usePlatformDefaultWidth = false // remove unnecessary width
                    ),
                    offset = IntOffset(
                        x = textFieldPosition.x.toInt(),
                        y = (textFieldPosition.y + textFieldHeight).toInt()
                    )
                ) {
                    Surface(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .border(1.dp, Color(0xFF607C8A)),
                        color = SkyBlue,
                        shadowElevation = 4.dp,
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .heightIn(max = 300.dp) // limit height to make it scrollable
                        ) {
                            items(countries.entries.toList()) { (countryCode, countryName) ->
                                Text(
                                    text = countryName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (searchQuery.value.isNotEmpty()) { // if there is a search query
                                                searchQuery.value = "" // clear the search query
                                            }
                                            if (selectedCategory.value != null) {
                                                selectedCategory.value = null
                                            }

                                            if (selectedDateRange.value != null) { // if there is a selected date range
                                                selectedDateRange.value = null // clear the date range
                                            }

                                            selectedCountry.value = countryCode
                                            expanded = false
                                        }
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Filtering by Date Range

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }


        item {

            // Row to hold "Date Range" text and "Clear" button
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Space between "Category" and "Clear"
                verticalAlignment = Alignment.Bottom // Align bottoms of "Category" and "Clear"
            ) {

                Text(
                    text = "Date Range",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                val shouldShowClear = selectedDateRange.value != null

                if (shouldShowClear) {

                    Text(
                        text = "Clear",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable(
                                onClick = {

                                    if (searchQuery.value.isNotEmpty()) { // if there is a search query
                                        searchQuery.value = "" // clear the search query
                                    }

                                    if (selectedCategory.value != null) {
                                        selectedCategory.value = null
                                    }

                                    if (selectedCountry.value != null) {
                                        selectedCountry.value = null
                                    }

                                    selectedDateRange.value = null // Set to null to clear the filter
                                    expanded = false // Close the dropdown
                                },
                                indication = LocalIndication.current, // Apply ripple effect
                                interactionSource = remember { MutableInteractionSource() } // Interaction source for ripple
                            )
                            .align(Alignment.Bottom) // Ensure it's aligned at the bottom
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

        }

        items(dateRanges) { dateRange ->
            val isSelected = selectedDateRange.value == dateRange
            val backgroundColor = if (isSelected) Color(0xFF90CAF9) else Color(0xFFBBDEFB) // Subtle contrast
            val borderColor = if (isSelected) Color(0xFF64B5F6) else Color.Transparent // Light blue border when selected

            Button(
                onClick = {

                    if (searchQuery.value.isNotEmpty()) { // if there is a search query
                        searchQuery.value = "" // clear the search query
                    }

                    if (selectedCategory.value != null) {
                        selectedCategory.value = null
                    }

                    if (selectedCountry.value != null) { // if there is a selected country
                        selectedCountry.value = null // clear the country
                    }

                    val newDateRange = if (isSelected) null else dateRange // Toggle selection
                    selectedDateRange.value = newDateRange // Notify HomeScreen of change
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                border = BorderStroke(2.dp, borderColor)
            ) {
                Text(text = dateRange, color = Color.Black)
            }
        }
    }
}

@Composable
fun ReactionBar(
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
                                Log.d(
                                    "GestureDebug",
                                    "isSelected for ${selectedReaction.value} is: $isSelected"
                                )
                                val newReaction = if (isSelected) null else reaction
                                onReactionSelected(newReaction) // homeModel.updateReaction done in callback
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

val categories = listOf(
    "Business",
    "Entertainment",
    "Health",
    "Science",
    "Sports",
    "Technology"
)

val countries = mapOf(
    "us" to "United States",
    "ca" to "Canada",
    "gb" to "United Kingdom",
    "in" to "India",
    "au" to "Australia",

)

val dateRanges = listOf(
    "Last 7 Days",
    "Last 14 Days",
    "Last 30 Days",

)









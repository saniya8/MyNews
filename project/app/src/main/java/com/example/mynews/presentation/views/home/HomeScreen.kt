package com.example.mynews.presentation.views.home

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.navigation.NavHostController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.mynews.presentation.viewmodel.NewsViewModel
import com.example.mynews.presentation.viewmodel.SettingsViewModel
import com.example.mynews.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.mynews.presentation.views.home.NewsScreen
import com.example.mynews.utils.AppScreenRoutes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.CoroutineScope
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import com.example.mynews.presentation.viewmodel.SavedArticlesViewModel


fun todayDateText() : String {
    val today = LocalDate.now()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("MMMM d")) // "e.g., February 17"
    return formattedDate;
}

//TODO: non urgent - fix duplicate api calls

// Right now in the code, the category/search filter is maintained between navigation (as desired)
// However, ONLY when the HomeScreen is recreated (aka when navigating back into HomeScreen tab),
// there are duplicate API calls due to the LaunchedEffects. This does NOT affect what the user
// sees - from user POV, between navigations to and from home tab, when getting back to home
// tab, filter looks correct. However, it does cause few unnecessary duplicate API calls in the
// backend. This is fine since it works and doesn't do too many duplicate calls.
// Only try to optimize this if there is time/need to do so
// (e.g., by moving code from LaunchedEffect(selectedCategory.value) and LaunchedEffect(searchQuery.value)
// to their respective composables onClick/onValueChange)

@Composable
fun HomeScreen(
    navController: NavHostController /*= rememberNavController()*/,
    newsViewModel: NewsViewModel, // Keep here so NewsViewModel persists between navigation
    savedArticlesViewModel: SavedArticlesViewModel,
    selectedCategory: MutableState<String?>,
    searchQuery: MutableState<String>
) {

    val articles by newsViewModel.articles.observeAsState(emptyList())
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) // Controls drawer open/close
    val scope = rememberCoroutineScope() // Required for controlling the drawer

    fun openDrawer() {
        scope.launch { drawerState.open() }
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                newsViewModel = newsViewModel,
                drawerState = drawerState,
                scope = scope,
                selectedCategory = selectedCategory, //.value, // Pass selected category
                searchQuery = searchQuery,
                //onCategorySelected = { newCategory ->
                //    selectedCategory.value = newCategory // Update selected category state
                //}
            )
        }
    ) {
        // Box is to be able to swipe right to open drawer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount > 50) { // Detect swipe right to open drawer
                            scope.launch { drawerState.open() }
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


                    /*
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                            //.padding(horizontal = 16.dp, vertical = 8.dp), // Adjust padding for alignment
                        horizontalArrangement = Arrangement.SpaceBetween, // Space title & icon apart
                        verticalAlignment = Alignment.CenterVertically
                    ) {


                        /*
                        // Heading
                        Text(
                            text = "My News",
                            //modifier = Modifier.align(Alignment.CenterHorizontally),
                            modifier = Modifier.weight(1f), // Pushes icon to the right, centers text
                            textAlign = TextAlign.Center, // Ensures text itself is centered
                            fontWeight = FontWeight.Bold,
                            color = CaptainBlue,
                            fontSize = 25.sp,
                            fontFamily = FontFamily.SansSerif
                        )

                        // Saved Articles Icon (Click to navigate to SavedArticlesScreen)
                        IconButton(onClick = {
                            //navController.navigate(AppScreenRoutes.SavedArticlesScreen.route)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Saved Articles",
                                tint = CaptainBlue // Match title color
                            )
                        }

                         */

                        // Empty Spacer to push "My News" to the exact center
                        Spacer(modifier = Modifier.weight(1f))

                        // "My News" exactly centered
                        Text(
                            text = "My News",
                            fontWeight = FontWeight.Bold,
                            color = CaptainBlue,
                            fontSize = 25.sp,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center
                        )

                        // Another Spacer to balance space on both sides
                        Spacer(modifier = Modifier.weight(1f))

                        // Saved Articles Icon (pinned to the right)
                        IconButton(onClick = {
                            //navController.navigate(AppScreenRoutes.SavedArticlesScreen.route)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Saved Articles",
                                tint = CaptainBlue
                            )
                        }



                    }

                     */

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            //.padding(horizontal = 16.dp)
                    ) {
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
                                .align(Alignment.TopEnd) // Ensures it stays in the top-right
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                //imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = "Saved Articles",
                                tint = CaptainBlue
                            )
                        }
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

                    SearchAndFilter(newsViewModel = newsViewModel,
                              drawerState = drawerState,
                              scope = scope,
                              selectedCategory = selectedCategory,
                              searchQuery = searchQuery)


                    NewsScreen(
                        navController = navController,
                        newsViewModel = newsViewModel,
                        savedArticlesViewModel = savedArticlesViewModel,
                        articles = articles,
                        origin = "HomeScreen",
                        openDrawer = ::openDrawer,
                    ) // Display news

                }
            }
        }
    }
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

        // Search icon
        //IconButton(
        //    onClick = {} // creating icon button to leave this as an option
        //) {
        //    Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon")
        //}

        // Search bar

        /*
        OutlinedTextField(
            modifier = Modifier.padding(8.dp)
                .height(48.dp)
                .border(1.dp, Color.Gray, CircleShape)
                .clip(CircleShape),
            value = searchQuery.value,
            onValueChange = {searchQuery.value = it}
        )

         */

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
                .height(50.dp) // Slightly increased height
                //.fillMaxWidth()
                .weight(1f) // this instead of fillMaxWidth makes the search
                // bar fill the remaining width, but then it looks weird
                // because the My News and Date at the top look off centered
                // even though they are not
                .clip(CircleShape), // Clip to rounded edges
            shape = CircleShape, // Ensures rounded edges
            textStyle = TextStyle(fontSize = 16.sp), // Adjust font size if needed
            singleLine = true, // Prevents multiline input
            maxLines = 1,
            trailingIcon = {
                IconButton(
                    onClick = { // only search onClick
                        if(searchQuery.value.isNotEmpty()) {
                            if (selectedCategory.value != null) { // if any categories are selected
                                selectedCategory.value = null; // clear any category selections
                                // TODO: FIX this duplicate API call
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


            //placeholder = { Text("Search...") }, // Optional placeholder text
        )

        IconButton(onClick = { scope.launch { drawerState.open() } }) {
            Icon(Icons.Default.Tune, contentDescription = "Filter Menu")
        }







    }

}

@Composable
fun DrawerContent(
    newsViewModel: NewsViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope,
    //selectedCategory: String?, // Receive selected category from HomeScreen
    selectedCategory: MutableState<String?>,
    searchQuery: MutableState<String>
    //onCategorySelected: (String?) -> Unit // Function to update selection
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
            // Close (X) Button
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
                        // TODO: FIX this duplicate API call
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






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
import androidx.compose.ui.Alignment
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch



fun todayDateText() : String {
    val today = LocalDate.now()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("MMMM d")) // "e.g., February 17"
    return formattedDate;
}

@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController(),
    newsViewModel: NewsViewModel, // Keep here so NewsViewModel persists between navigation
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) // Controls drawer open/close
    val scope = rememberCoroutineScope() // Required for controlling the drawer
    val selectedCategory = remember { mutableStateOf<String?>(null) } // Track selected category

    // selectedCategory can be null if a user deselects, in which case it should fetch
    // with just the language parameter and not the category parameter

    LaunchedEffect(Unit) {
        newsViewModel.fetchNewsTopHeadlines()
    }

    // testing to check that selectedCategory.value actually updates
    LaunchedEffect(selectedCategory.value) {
        Log.d("CategorySelection", "Selected Category: ${selectedCategory.value}")
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                drawerState = drawerState,
                scope = scope,
                selectedCategory = selectedCategory.value, // Pass selected category
                onCategorySelected = { newCategory ->
                    selectedCategory.value = newCategory // Update selected category state
                }
            )
        }
    ) {
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
                topBar = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Tune, contentDescription = "Filter Menu")
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    // Heading
                    Text(
                        text = "My News",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontWeight = FontWeight.Bold,
                        color = CaptainBlue,
                        fontSize = 25.sp,
                        fontFamily = FontFamily.SansSerif
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Today's date
                    Text(
                        text = todayDateText(),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = CaptainBlue,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.SansSerif
                    )

                    NewsScreen(newsViewModel) // Display news
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    drawerState: DrawerState,
    scope: CoroutineScope,
    selectedCategory: String?, // Receive selected category from HomeScreen
    onCategorySelected: (String?) -> Unit // Function to update selection
) {
    val categories = listOf("Business", "Entertainment", "General", "Health", "Science", "Sports", "Technology")

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
            val isSelected = selectedCategory == category
            val backgroundColor = if (isSelected) Color(0xFF90CAF9) else Color(0xFFBBDEFB) // Subtle contrast
            val borderColor = if (isSelected) Color(0xFF64B5F6) else Color.Transparent // Light blue border when selected

            Button(
                onClick = {
                    val newCategory = if (isSelected) null else category // Toggle selection
                    onCategorySelected(newCategory) // Notify HomeScreen of change
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

// version 1 - pre making category buttons look pretty (bright purple here)
@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController(),
    newsViewModel: NewsViewModel, // Keep here so NewsViewModel persists between navigation
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) // Controls drawer open/close
    val scope = rememberCoroutineScope() // Required for controlling the drawer

    LaunchedEffect(Unit) {
        newsViewModel.fetchNewsTopHeadlines()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // drawerContent is the UI content for the sidebar
        drawerContent = {
            DrawerContent(drawerState, scope)
        }
    ) {

        // body of ModalNavigationDrawer is where the main UI content for the screen goes


        // Box wraps the rest of the UI content so that swiping left to open filter content work
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount > 50) { // Detect swipe right
                            scope.launch { drawerState.open() }
                        }
                    }
                }
        ) {

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    // Add a hamburger menu button in the top-left
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Tune, contentDescription = "Menu")
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    // Heading
                    Text(
                        text = "My News",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontWeight = FontWeight.Bold,
                        color = CaptainBlue,
                        fontSize = 25.sp,
                        fontFamily = FontFamily.SansSerif
                    )

                    // Spacing
                    Spacer(modifier = Modifier.height(8.dp))

                    // Today's date
                    Text(
                        text = todayDateText(),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = CaptainBlue,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.SansSerif
                    )

                    NewsScreen(newsViewModel) // Display news
                }
            } // end of scaffold
        } // end of box

    } // end of body
}

@Composable
fun DrawerContent(drawerState: DrawerState, scope: CoroutineScope) {
    val categories = listOf("Business", "Entertainment", "General", "Health", "Science", "Sports", "Technology")
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .width(250.dp)
            .background(Color.LightGray)
            .padding(16.dp)
    ) {

        // Using LazyColumn so sidebar becomes vertically scrollable

        // Everything in LazyColumn needs to be wrapped in item
        item {
            // Row for Close (X) Button, aligned to the top-right
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
            // Title
            Text(
                text = "Filter My News",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Add category options here (later for filtering)
        item {
            // Category Heading
            Text(
                text = "Category",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }


        // Generate buttons dynamically for each category
        items(categories) { category ->
            Button(
                onClick = { /* TODO: Implement filtering logic later */ },
                shape = RoundedCornerShape(20.dp), // Rounded rectangle buttons
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = category)
            }
        }
    }
}

 */





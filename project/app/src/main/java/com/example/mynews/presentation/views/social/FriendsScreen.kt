package com.example.mynews.presentation.views.social

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import com.example.mynews.presentation.viewmodel.social.FriendsViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import com.example.mynews.utils.AppScreenRoutes
import kotlinx.coroutines.delay

//import com.example.mynews.presentation.views.social.FriendScreen

@Composable
fun FriendsScreen(
    navController: NavHostController,
    friendsViewModel: FriendsViewModel
) {
    val users by friendsViewModel.users.observeAsState(emptyList())
    val friends by friendsViewModel.friends.observeAsState(emptyList())
    val filteredUsers = remember { mutableStateOf(users) }
    val searchQuery = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        friendsViewModel.fetchAllUsers()
        friendsViewModel.fetchFriends()
    }

    LaunchedEffect(searchQuery.value) {
        delay(300) // Wait for 300ms before updating the filtered list
        filteredUsers.value = users.filter { user ->
            user.contains(searchQuery.value, ignoreCase = true)
        }
    }



    Box (
        // User can swipe left to right to return back to the social screen
        modifier = Modifier
            .fillMaxSize()

            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) { // Detect swipe right to go back
                        //navController.popBackStack()
                        navController.popBackStack(AppScreenRoutes.SocialScreen.route, false)
                    }
                }
            }

    ) {

        Scaffold(
            modifier = Modifier.fillMaxSize(),

            ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "Find Friends",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                FriendsSearchBar(searchQuery = searchQuery)

                Spacer(modifier = Modifier.height(10.dp))

                // Username List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // users or filtered users
                    if (filteredUsers.value.isEmpty()) {
                        item {
                            Text(
                                text = "No users found",
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    } else {
                        items(filteredUsers.value) { user ->
                            UserItem(
                                username = user,
                                onAddFriend = {
                                    friendsViewModel.addFriend(user)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                AddedFriendsList(listOf("Joe", "Jane"))
                // AddedFriendsList(friends = friends)
            }

        }

}



}

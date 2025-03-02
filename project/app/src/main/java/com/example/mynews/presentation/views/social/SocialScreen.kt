package com.example.mynews.presentation.views.social

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button // check this
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import com.example.mynews.presentation.viewmodel.FriendsViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import com.example.mynews.domain.repositories.UserRepository

@Composable
fun SocialScreen(
    navController: NavHostController,
    friendsViewModel: FriendsViewModel
) {
    val users by friendsViewModel.users.observeAsState(emptyList())
    val searchQuery = remember { mutableStateOf("") }

    // Fetch all users when the screen is first displayed
    LaunchedEffect(Unit) {
        friendsViewModel.fetchAllUsers()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Find Friends",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        FriendsSearchBar(
            friendsViewModel = friendsViewModel,
            searchQuery = searchQuery,
            friendsList = users,
            onFriendAction = { friend, isAdding ->
                if (isAdding) {
                    friendsViewModel.addFriend(friend)
                } else {
                    // Handle removing a friend if needed TODO
                }
            }
        )

        // User List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(users) { user ->
                UserItem(
                    username = user,
                    onAddFriend = {
                        friendsViewModel.addFriend(user)
                    }
                )
            }
        }
    }
}

@Composable
fun UserItem(
    username: String,
    onAddFriend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = username,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = onAddFriend,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text("Add")
        }
    }
}

@Composable
fun FriendsSearchBar(
    friendsViewModel: FriendsViewModel,
    searchQuery: MutableState<String>,
    friendsList: List<String>,
    onFriendAction: (String, Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            modifier = Modifier
                .weight(1f) // Allow the search bar to take the full width
                .clip(CircleShape),
            shape = CircleShape, // Ensures rounded edges
            textStyle = TextStyle(fontSize = 16.sp),
            singleLine = true,
            maxLines = 1,
            placeholder = { Text("Search for friends...") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (searchQuery.value.isNotEmpty()) {
                            // Trigger friend search logic here
                            // Update the displayed list based on the search query
                            friendsViewModel.fetchAllUsers()
                            //friendsViewModel.filterFriends(searchQuery.value)
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon")
                }
            }
        )
    }

    // Display the filtered friends list
    LazyColumn {
        items(friendsList.filter { it.contains(searchQuery.value, ignoreCase = true) }) { friend ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = friend,
                    modifier = Modifier.weight(1f)
                )

                // Add/Remove button
                IconButton(
                    onClick = {
                        val isAdding = !friendsList.contains(friend)
                        onFriendAction(friend, isAdding) // True for adding, false for removing
                    }
                ) {
                    Icon(
                        imageVector = if (friendsList.contains(friend)) Icons.Default.Remove else Icons.Default.Add,
                        contentDescription = "Add/Remove Friend"
                    )
                }
            }
        }
    }
}
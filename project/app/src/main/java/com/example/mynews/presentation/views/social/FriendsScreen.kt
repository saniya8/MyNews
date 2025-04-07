package com.example.mynews.presentation.views.social

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import com.example.mynews.presentation.components.ScreenHeader
import com.example.mynews.presentation.theme.CaptainBlue
import com.example.mynews.utils.AppScreenRoutes


@Composable
fun FriendsScreen(
    navController: NavHostController,
    friendsViewModel: FriendsViewModel,
) {

    val searchQuery by friendsViewModel.searchQuery.collectAsState()
    val recentlyAddedFriend by friendsViewModel.recentlyAddedFriend.collectAsState()
    val friends by friendsViewModel.friends.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        friendsViewModel.updateSearchQuery("")
        friendsViewModel.fetchFriends()
    }


    if (friendsViewModel.showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                friendsViewModel.showErrorDialog = false
            },
            confirmButton = {
                TextButton(onClick = {
                    friendsViewModel.showErrorDialog = false
                }) {
                    Text("OK")
                }
            },

            title = {
                Text(
                    text = "Cannot Add User",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },

            text = {
                Text(
                    text = friendsViewModel.errorDialogMessage ?: "Unknown error",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }

        )
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
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {

                ScreenHeader(
                    useTopPadding = false,
                    title = "Friends",
                )

                Spacer(modifier = Modifier.height(10.dp))

                FriendsSearchBar(searchQuery = searchQuery,
                                 onQueryChanged = { newQuery -> friendsViewModel.updateSearchQuery(newQuery)},
                                 onAddNewFriend = { friendsViewModel.addFriend(searchQuery)})

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Your Friends",
                    fontSize = 20.sp,
                    color = CaptainBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                )

                if (friends.isEmpty()) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No friends added yet",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    }

                } else {
                    LazyColumn (modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                    ) {
                        items(friends) { friend ->
                            FriendItem(
                                friend = friend,
                                isRecentlyAdded = (friend == recentlyAddedFriend),
                                onRemoveFriend = { friendsViewModel.removeFriend(friend) }
                            )
                        }
                    }
                }




            }
        }
    }
}

@Composable
fun FriendsSearchBar(
    searchQuery: String,
    onQueryChanged: (String) -> Unit,
    onAddNewFriend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .weight(1f)
                .clip(CircleShape),
            shape = CircleShape,
            textStyle = TextStyle(fontSize = 16.sp),
            singleLine = true,
            maxLines = 1,
            placeholder = { Text("Search for friends...") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        onAddNewFriend()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add friend")
                }
            }
        )
    }
}


@Composable
fun FriendItem(
    friend: String,
    isRecentlyAdded: Boolean,
    onRemoveFriend: () -> Unit
) {

    val cardHeight = 80.dp

    // animate background color
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isRecentlyAdded) Color(0xFFD8EAFE) else Color(0xFFF1F4FA),
        animationSpec = tween(durationMillis = 600),
        label = "recently-added-friend-bg"
    )

    Box(
        modifier = Modifier
            .height(cardHeight)
            .fillMaxWidth()
    ) {

        Card(
            modifier = Modifier
                .padding(8.dp)
                .height(cardHeight)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = animatedBackgroundColor) // light bluish-grey background
        ) {

            Box(modifier = Modifier.fillMaxSize()) {

                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // 15% — Emoji Reaction section
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(2.dp)
                            .weight(0.15f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Circle with first initial
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3A5A8C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = friend.firstOrNull()?.uppercase() ?: "",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                    }

                    Row (
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(0.85f)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        Text(
                            text = friend,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)

                        )

                        IconButton(
                            onClick = onRemoveFriend
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove Friend",
                                tint = CaptainBlue
                            )
                        }





                    }
                } // end of row
            } // end of Box


        } // end of Card
    } // end of Box

}

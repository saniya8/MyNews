package com.example.mynews.presentation.views.social

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.mynews.presentation.components.ScreenHeader
import com.example.mynews.ui.theme.CaptainBlue
import com.example.mynews.utils.AppScreenRoutes


@Composable
fun FriendsScreen(
    navController: NavHostController,
    friendsViewModel: FriendsViewModel,
) {
    val searchQuery = remember { mutableStateOf("") }
    val friends by friendsViewModel.friends.observeAsState(emptyList())

    LaunchedEffect(Unit) {
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
                    useTopPadding = false, // true bc this screen wrapped in top-level box, not top-level scaffold
                    title = "Friends",
                )

                Spacer(modifier = Modifier.height(10.dp))

                FriendsSearchBar(searchQuery = searchQuery,
                                 onAddNewFriend = { friendsViewModel.addFriend(searchQuery.value) } )

                Spacer(modifier = Modifier.height(10.dp))

                //AddedFriendsList(friends = friends, viewModel = friendsViewModel)

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
    searchQuery: MutableState<String>,
    onAddNewFriend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { query ->
                searchQuery.value = query
            },
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
                        // SK: here, it should call add friend function from FriendsViewModel
                        // which will trigger your UI (friend items aka rectangles of friends)
                        // to be updated, now that a new friend was added
                        // each of these friend rectangles will have the delete button on them
                        // which when clicked will trigger removeFriend from FriendsViewModel
                        // and will rerender UI
                        // UI will automatically rerender per note at the top of FriendsScreen
                        // No additional code should be required to re-render it
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
fun AddedFriendsList(
    friends: List<String>,
    viewModel: FriendsViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Your Friends",
            fontSize = 20.sp,
            color = CaptainBlue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 16.dp)
        )

        if (friends.isEmpty()) {
            Text(
                text = "No friends added yet",
                fontSize = 16.sp,
                color = Color.Gray
            )
        } else {
            LazyColumn {
                items(friends) { friend ->
                    FriendItem(
                        friend = friend,
                        onRemoveFriend = { viewModel.removeFriend(friend) }
                    )
                }
            }
        }
    }
}

@Composable
fun FriendItem(
    friend: String,
    onRemoveFriend: () -> Unit
) {

    val cardHeight = 80.dp


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
                containerColor = Color(0xFFF1F4FA) // light bluish-grey background
            )
        ) {

            Box(modifier = Modifier.fillMaxSize()) {

                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                        //.padding(8.dp),
                        //.background(Color.Magenta),
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
                                .background(Color(0xFF3A5A8C)), // Your primary blue or choose a soft tone
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
                            //.background(Color.Magenta), // for testing
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

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import com.example.mynews.utils.AppScreenRoutes

@Composable
fun FriendsScreen(
    navController: NavHostController,
    friendsViewModel: FriendsViewModel,
) {
    val searchQuery = remember { mutableStateOf("") }
    val friends by friendsViewModel.friends.observeAsState(emptyList())

    // SK: no other Launched Effects other than LaunchedEffect(Unit) should be needed here
    // UI will automatically render when user's friends change ie when friend added, friend removed
    // without the use of LaunchedEffect
    // This was tested and worked with SavedArticles feature, so should work here
    // Reason:
    // in friendRepository's getFriends, addSnapshotListener is used meaning
    // whenever users_friends subcollection is updated, firestore detects a change in real time and
    // in the viewmodel, triggers _friends.postValue(friendsList) which updates
    // _friends which updates friends in the view model. Since FriendsScreen
    // is observing friends in the view model, whenever friends in view model updates,
    // UI will be re-rendered

    LaunchedEffect(Unit) {
        friendsViewModel.fetchFriends()
    }

    // SK: might have to change the launchedeffects here since by the simple approach,
    // searchQuery.value changing should not trigger a delay and a filtering,
    // nothing should happen if searchQuery.value changes
    // it's only if the user clicks on the trailing icon (e.g., add friend icon) that the UI
    // should update
    // don't think below launched effect is needed at all

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
                    text = "Add Friends",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                FriendsSearchBar(searchQuery = searchQuery, onAddNewFriend = { friendsViewModel.addFriend(searchQuery.value) } )

                Spacer(modifier = Modifier.height(10.dp))

                AddedFriendsList(friends = friends, viewModel = friendsViewModel)
            }
        }
}
}

package com.example.mynews.presentation.views.social

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.mynews.presentation.viewmodel.home.HomeViewModel
import com.example.mynews.ui.theme.CaptainBlue
import com.example.mynews.utils.AppScreenRoutes
import com.example.mynews.presentation.viewmodel.social.FriendsViewModel

@Composable
fun SocialScreen(
    navController: NavHostController,
    friendsViewModel: FriendsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val friendsIds by friendsViewModel.friendsIds.observeAsState(emptyList())
    //val friendsUsernames by friendsViewModel.friends.observeAsState(emptyList())
    val reactions by homeViewModel.reactions.collectAsState()
    val friendsMap by friendsViewModel.friendsMap.collectAsState()

    LaunchedEffect(Unit) {
        friendsViewModel.getFriendIds()
        friendsViewModel.fetchFriendIdsAndUsernames()
    }

    LaunchedEffect(friendsIds) {
        if (friendsIds.isNotEmpty()) {
            homeViewModel.fetchFriendsReactions(friendsIds)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Friend Activity",
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
                        navController.navigate(AppScreenRoutes.FriendsScreen.route)
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd) // ensure it stays in the top right
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAddAlt1,
                        //imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = "Saved Articles",
                        tint = CaptainBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (reactions.isEmpty()) {
                Box (
                    modifier = Modifier
                        .fillMaxWidth()
                ){
                    Text(
                        text = "No Friend Activity Yet",
                        fontWeight = FontWeight.Bold,
                        color = CaptainBlue,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                LazyColumn {
                    items(reactions) { reaction ->
                        ReactionItem(reaction = reaction, username = friendsMap[reaction.userID].toString(), navController = navController)
                    }
                }
            }
        }
    }
}


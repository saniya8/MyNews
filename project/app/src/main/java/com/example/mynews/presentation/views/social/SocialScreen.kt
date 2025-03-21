package com.example.mynews.presentation.views.social

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.mynews.domain.model.Reaction
import com.example.mynews.ui.theme.CaptainBlue
import com.example.mynews.utils.AppScreenRoutes
import com.example.mynews.presentation.viewmodel.social.SocialViewModel
import java.util.Date

@Composable
fun SocialScreen(
    navController: NavHostController,
    socialViewModel: SocialViewModel = hiltViewModel(),
) {
    //val friendsIds by socialViewModel.friendsIds.observeAsState(emptyList())
    val reactions by socialViewModel.reactions.collectAsState()
    val friendsMap by socialViewModel.friendsMap.collectAsState()

    LaunchedEffect(Unit) {
        //socialViewModel.fetchFriendIds()
        //socialViewModel.fetchFriendIdsAndUsernames()
        socialViewModel.fetchFriends()
    }

    /*
    LaunchedEffect(friendsIds) {
        if (friendsIds.isNotEmpty()) {
            socialViewModel.fetchFriendsReactions(friendsIds)
        }
    }

     */

    LaunchedEffect(friendsMap) {

        if (friendsMap.isNotEmpty()) {
            val friendsIdsList = friendsMap.keys.toList()
            socialViewModel.fetchFriendsReactions(friendsIdsList)
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
                IconButton(
                    onClick = {
                        navController.navigate(AppScreenRoutes.FriendsScreen.route)
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAddAlt1,
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

@Composable
fun ReactionItem(
    reaction: Reaction,
    username: String,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate(
                    AppScreenRoutes.NewsArticleScreen.createRoute(
                        Uri.encode(reaction.article.url),
                        "SocialScreen" // fixed this: TODO might be wrong
                    )
                )
            },
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "$username reacted:",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = reaction.reaction,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Article: ${reaction.article.title}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "Timestamp: ${Date(reaction.timestamp)}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}


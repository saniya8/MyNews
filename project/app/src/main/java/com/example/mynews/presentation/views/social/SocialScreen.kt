package com.example.mynews.presentation.views.social

import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.mynews.domain.model.Reaction
import com.example.mynews.presentation.components.ScreenHeader
import com.example.mynews.ui.theme.CaptainBlue
import com.example.mynews.utils.AppScreenRoutes
import com.example.mynews.presentation.viewmodel.social.SocialViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@Composable
fun SocialScreen(
    navController: NavHostController,
    socialViewModel: SocialViewModel,
) {
    //val friendsIds by socialViewModel.friendsIds.observeAsState(emptyList())

    val searchQuery by socialViewModel.searchQuery.collectAsState()
    //val reactions by socialViewModel.reactions.collectAsState()
    val reactions by socialViewModel.filteredReactions.collectAsState() // implemented dynamic filtering below
    val friendsMap by socialViewModel.friendsMap.collectAsState()
    val isLoading by socialViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        socialViewModel.updateSearchQuery("")
        socialViewModel.fetchFriends()
    }



    LaunchedEffect(friendsMap) {

        if (friendsMap.isNotEmpty()) {
            val friendsIdsList = friendsMap.keys.toList()
            socialViewModel.fetchFriendsReactions(friendsIdsList)
        } else {
            socialViewModel.fetchFriendsReactions(emptyList())
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

            // standardized

            ScreenHeader(
                useTopPadding = false, //scaffold already adds system padding
                title = "Friend Activity",
                rightContent = {
                    IconButton(
                        onClick = {
                            navController.navigate(AppScreenRoutes.FriendsScreen.route)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAddAlt1,
                            contentDescription = "Friends",
                            tint = CaptainBlue
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SocialSearchBar(searchQuery = searchQuery,
                            onQueryChanged = { newQuery -> socialViewModel.updateSearchQuery(newQuery)},
                            onSearchFriendsReactions = {})

            Spacer(modifier = Modifier.height(7.dp))

            if (!isLoading) {

                // four cases: 
                // case 1: reactions is empty and searchQuery is empty -> user isn't searching and no reactions at all
                // case 2: reactions is empty and searchQuery is full -> user searched for something but no reactions matched search
                // case 3: reactions is not empty and searchQuery is empty
                // case 4: reactions is not empty and searchQuery is not empty
                // in case 3 and 4, regardless of if the user searched or not, there are reactions, so display them

                val isActuallySearching = searchQuery.isNotEmpty() && searchQuery.trim().isNotEmpty()


                // case 1
                if (reactions.isEmpty() && !isActuallySearching) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No friend activity yet",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                }

                // case 2
                else if (reactions.isEmpty() && isActuallySearching) {

                    val isUsernameInFriends = friendsMap.values.any { it == searchQuery.trim() }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isUsernameInFriends)
                                        "No activity from your friend yet"
                                    else
                                        "You can only view activity \n from your friends",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                }

                // cases 3 & 4
                else { // searchQuery isn't empty or reactions isn't empty


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        Text(
                            text = "Click reaction to view what your friend read",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    LazyColumn (
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        items(reactions) { reaction ->
                            ReactionItem(
                                reaction = reaction,
                                username = friendsMap[reaction.userID].toString(),
                                navController = navController
                            )
                        }
                    }
                }
            } // else isLoading == true, show nothing
        }
    }
}


@Composable
fun SocialSearchBar(
    searchQuery: String,
    onQueryChanged: (String) -> Unit,
    onSearchFriendsReactions: () -> Unit
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
            placeholder = { Text("Search friends...") },

            /*trailingIcon = {
                IconButton(
                    onClick = {
                        onSearchFriendsReactions()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Add friend")
                }
            }*/

            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChanged("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                    }
                } else {
                    // no search icon because dynamic search so no need
                    //Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                }
            }
        )
    }
}

@Composable
fun ReactionItem(
    reaction: Reaction,
    username: String,
    navController: NavController
) {

    val cardHeight = 130.dp


    Box(
        modifier = Modifier
            .height(cardHeight)
            .fillMaxWidth()
    ) {

        Card(
            modifier = Modifier
                .padding(8.dp)
                .height(cardHeight)
                .fillMaxWidth()
                .clickable {
                    navController.navigate(
                        AppScreenRoutes.NewsArticleScreen.createRoute(
                            Uri.encode(reaction.article.url),
                            "SocialScreen"
                        )
                    )
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE9ECF5) // soft blue-grey background
            )
        ) {

            Box(modifier = Modifier.fillMaxSize()) {

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // 85% — Content section
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.85f)
                            //.background(Color.Magenta) // for testing
                    ) {

                        Text(
                            text = getReactionMessage(username, reaction.reaction),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis // cuts off text with "..."
                            //fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = reaction.article.title,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis // cuts off text with "..."
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = getRelativeTimestamp(reaction.timestamp),
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                    }

                    // 15% — Emoji Reaction section
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.15f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = reaction.reaction,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }


                } // end of row
        } // end of Box


        } // end of Card
    } // end of Box
}

val reactionMessages = mapOf(
    "👍" to "liked",
    "❤️" to "loved",
    "🤯" to "was mind-blown by",
    "😮" to "was surprised by",
    "🤔" to "was intrigued by",
    "😢" to "was saddened by",
    "🥹" to "was moved by",
    "😡" to "was angered by",
    "😂" to "found this hilarious"
)


private fun getReactionMessage(username: String, reaction: String): String {
    val reactionMessage = reactionMessages[reaction] ?: "reacted to:"
    return "$username $reactionMessage"
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun getRelativeTimestamp(timestamp: Long): String {

    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    val reactionDate = Date(timestamp)
    val calendarNow = Calendar.getInstance()
    val calendarReaction = Calendar.getInstance().apply { time = reactionDate }


    // "Yesterday" means the event happened on the previous calendar day,
    // "1 day ago" means it happened more than 24 hours ago but not exactly on the previous calendar day,
    // "2 days ago" means it happened 48+ hours ago, regardless of calendar days.


    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        days < 2 && calendarNow.get(Calendar.DAY_OF_YEAR) - calendarReaction.get(Calendar.DAY_OF_YEAR) == 1 -> "Yesterday"
        days < 7 -> {
            val daysUnit = if (days == 1L) "day" else "days"
            "$days $daysUnit ago"
        }
        calendarNow.get(Calendar.YEAR) == calendarReaction.get(Calendar.YEAR) -> {
            SimpleDateFormat("MMM d", Locale.getDefault()).format(reactionDate) // e.g., Mar 29
        }
        else -> {
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(reactionDate) // e.g., Sep 12, 2023
        }
    }
}


// unit test also made for the function but just creating this composable regardless
@Composable
@Preview(showBackground = true)
fun ReactionTimePreview() {
    val now = System.currentTimeMillis()

    val testReactions = listOf(
        now, // Just now
        now - TimeUnit.MINUTES.toMillis(5), // 5 min ago
        now - TimeUnit.HOURS.toMillis(2),   // 2 hr ago
        now - TimeUnit.DAYS.toMillis(1),    // Yesterday
        now - TimeUnit.DAYS.toMillis(3),    // 3 days ago
        now - TimeUnit.DAYS.toMillis(10),   // 10 days ago
        Calendar.getInstance().apply {
            set(Calendar.YEAR, 2023)
            set(Calendar.MONTH, Calendar.MARCH)
            set(Calendar.DAY_OF_MONTH, 29)
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 15)
        }.timeInMillis // Mar 29, 2023
    )

    val labels = listOf(
        "Just now",
        "5 min ago",
        "2 hr ago",
        "Yesterday",
        "3 days ago",
        "10 days ago",
        "Last year"
    )

    Column(modifier = Modifier.padding(16.dp)) {
        testReactions.forEachIndexed { index, timestamp ->
            Text(
                text = "${labels[index]} → ${getRelativeTimestamp(timestamp)}",
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


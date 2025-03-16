package com.example.mynews.presentation.views.social

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.MutableState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import com.example.mynews.presentation.viewmodel.social.FriendsViewModel
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Card
import androidx.navigation.NavController
import com.example.mynews.data.api.news.Reaction
import com.example.mynews.utils.AppScreenRoutes
// TODO check this import
import java.util.Date
import android.net.Uri

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
fun FriendItem(
    friend: String,
    onRemoveFriend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = friend,
            fontSize = 20.sp,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onRemoveFriend
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove friend"
            )
        }
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
            .padding(16.dp)
    ) {
        Text(
            text = "Your Friends",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
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
                // Navigate to the article screen with the article's URL
                navController.navigate(
                            AppScreenRoutes.NewsArticleScreen.createRoute(
                                Uri.encode(reaction.article.url),
                                "HomeScreen" // TODO might be wrong
                            )
                        )
            },
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "User $username reacted:", // reaction.userId
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
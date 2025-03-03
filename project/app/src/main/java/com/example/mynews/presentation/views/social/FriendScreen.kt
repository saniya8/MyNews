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
    searchQuery: MutableState<String>,
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
                        // TODO maybe
                    }
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon")
                }
            }
        )
    }
}

@Composable
fun AddedFriendsList(
    friends: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Added Friends",
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
                    Text(
                        text = friend,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
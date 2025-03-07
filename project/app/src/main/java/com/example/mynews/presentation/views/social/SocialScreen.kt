package com.example.mynews.presentation.views.social

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mynews.ui.theme.CaptainBlue
import com.example.mynews.utils.AppScreenRoutes


@Composable
fun SocialScreen(navController: NavHostController,) {

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
                //.padding(horizontal = 16.dp)
            ) {
                // "My News" - Exactly centered
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
        }
    }
}


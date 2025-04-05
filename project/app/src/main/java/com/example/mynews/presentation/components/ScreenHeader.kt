package com.example.mynews.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mynews.presentation.theme.CaptainBlue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun ScreenHeader(
    modifier: Modifier = Modifier,
    useTopPadding: Boolean = true,
    title: String,
    leftContent: (@Composable () -> Unit)? = null,
    rightContent: (@Composable () -> Unit)? = null,

) {
    // Top spacing based on system status bar (matches Scaffold behavior)
    //val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val topPadding = if (useTopPadding) { // top padding for future use in non-Scaffold screens
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    } else {
        0.dp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topPadding)
            .height(50.dp) // box size same regardless of if there are icons or not so headers are in line w/ each other
            //.height(IntrinsicSize.Min) // Makes height depend on content
    ) {
        // Left icon (optional)
        if (leftContent != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            ) {
                leftContent()
            }
        }

        // Centered Title
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = CaptainBlue,
            fontSize = 25.sp,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )

        // Right icon (optional)
        if (rightContent != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            ) {
                rightContent()
            }
        }
    }
}

@Preview(showBackground = true, name = "ScreenHeader - Alignment Preview")
@Composable
fun ScreenHeaderPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FB)) // Optional light background for contrast
    ) {
        val lineColor = Color.Red

        // 1. My News — left + right icons
        Box {
            ScreenHeader(
                title = "My News",
                leftContent = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Info",
                            tint = CaptainBlue
                        )
                    }
                },
                rightContent = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Bookmark",
                            tint = CaptainBlue
                        )
                    }
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(lineColor)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Friend Activity — only right icon
        Box {
            ScreenHeader(
                title = "Friend Activity",
                rightContent = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.PersonAddAlt1,
                            contentDescription = "Add Friends",
                            tint = CaptainBlue
                        )
                    }
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(lineColor)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Saved Articles — no icons
        Box {
            ScreenHeader(title = "Saved Articles")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(lineColor)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Add Friends — no icons
        Box {
            ScreenHeader(title = "Add Friends")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(lineColor)
            )
        }
    }
}



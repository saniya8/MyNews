package com.example.mynews.presentation.views

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



@Composable
fun SocialScreen() {
    // Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    //   Column(horizontalAlignment = Alignment.CenterHorizontally)
    //   Box(
    //       modifier = Modifier
    //           .size(width = 200.dp, height = 100.dp)
    //           .background(Color.Red)
    //   )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Friend Activity",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        // First Rectangle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFD8E6FF), RoundedCornerShape(16.dp))

        ) {
            // Circle Icon Top Right
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF2E3D83), CircleShape)
                    //     .absoluteOffset(x = (-20).dp) // Move circle outside to the left
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = "JK",
                    modifier = Modifier
                        .align(Alignment.Center) // Center text inside the rectangle
                        .padding(1.dp), // Optional padding to avoid text touching the edges
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            // Column to place two lines of text with space between them
            Column(
                modifier = Modifier
                    .align(Alignment.Center) // Center the Column inside the Box
                    .padding(4.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Recently Read",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp)) // Space between the texts
                Text(
                    text = "How to Improve your Productivity",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            // Rectangle Comment Bottom Left
            Box(
                modifier = Modifier
                    .size(250.dp, 40.dp)
                    .background(Color(0xF5FDF0DB), RoundedCornerShape(8.dp))
                    .align(Alignment.BottomEnd) // Position it at the bottom-right corner
                    .padding(8.dp) // Add some padding if needed
            ) {
                Text(
                    text = "\"Great morning read to start the day\"",
                    modifier = Modifier
                        .align(Alignment.Center) // Center text inside the rectangle
                        .padding(1.dp), // Optional padding to avoid text touching the edges
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))



        // Rectangle 2
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFD8E6FF), RoundedCornerShape(16.dp))

        ) {
            // Circle Icon Top Right
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF2E3D83), CircleShape)
                    //     .absoluteOffset(x = (-20).dp) // Move circle outside to the left
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = "SK",
                    modifier = Modifier
                        .align(Alignment.Center) // Center text inside the rectangle
                        .padding(1.dp), // Optional padding to avoid text touching the edges
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Column to place two lines of text with space between them
            Column(
                modifier = Modifier
                    .align(Alignment.Center) // Center the Column inside the Box
                    .padding(4.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Liked",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp)) // Space between the texts
                Text(
                    text = "The Future of Technology: Diving In",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Rectangle Comment Bottom Left
            Box(
                modifier = Modifier
                    .size(250.dp, 40.dp)
                    .background(Color(0xF5FDF0DB), RoundedCornerShape(8.dp))
                    .align(Alignment.BottomEnd) // Position it at the bottom-right corner
                    .padding(8.dp) // Add some padding if needed
            ) {
                Text(
                    text = "\"Thought-provoking and insightful\"",
                    modifier = Modifier
                        .align(Alignment.Center) // Center text inside the rectangle
                        .padding(1.dp), // Optional padding to avoid text touching the edges
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))



            // Rectangle 3
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFD8E6FF), RoundedCornerShape(16.dp))

            ) {
                // Circle Icon Top Right
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF2E3D83), CircleShape)
                        //     .absoluteOffset(x = (-20).dp) // Move circle outside to the left
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "MY",
                        modifier = Modifier
                            .align(Alignment.Center) // Center text inside the rectangle
                            .padding(1.dp), // Optional padding to avoid text touching the edges
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Column to place two lines of text with space between them
                Column(
                    modifier = Modifier
                        .align(Alignment.Center) // Center the Column inside the Box
                        .padding(4.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "Commented",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp)) // Space between the texts
                    Text(
                        text = "Understanding the Basics of Kotlin",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Rectangle Comment Bottom Left
                Box(
                    modifier = Modifier
                        .size(250.dp, 40.dp)
                        .background(Color(0xF5FDF0DB), RoundedCornerShape(8.dp))
                        .align(Alignment.BottomEnd) // Position it at the bottom-right corner
                        .padding(8.dp) // Add some padding if needed
                ) {
                    Text(
                        text = "\"I have no idea what is going on\"",
                        modifier = Modifier
                            .align(Alignment.Center) // Center text inside the rectangle
                            .padding(1.dp), // Optional padding to avoid text touching the edges
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                }
            }
        }
    }
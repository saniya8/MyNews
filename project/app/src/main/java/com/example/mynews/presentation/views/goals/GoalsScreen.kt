package com.example.mynews.presentation.views.goals

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mynews.domain.model.Mission
import com.example.mynews.presentation.components.ScreenHeader
import com.example.mynews.presentation.viewmodel.goals.GoalsViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import com.example.mynews.presentation.views.social.FriendItem


@Composable
fun GoalsScreen(
    navController: NavHostController,
    goalsViewModel: GoalsViewModel,
) {
    val streakCount by goalsViewModel.streakCount.observeAsState(0)
    val hasLoggedToday by goalsViewModel.hasLoggedToday
    val missions by goalsViewModel.missions.observeAsState(emptyList())

    // Calculate the number of completed missions
    val completedMissionsCount = missions.count { it.isCompleted }
    val totalMissionsCount = missions.size

    // Sort missions: uncompleted missions at the top, completed missions at the bottom
    val sortedMissions = missions.sortedBy { it.isCompleted }

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
                title = "Achievements",
            )

            Spacer(modifier = Modifier.height(10.dp))


            //Streak card section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                StreakCard(
                    streakCount = streakCount,
                    hasLoggedToday = hasLoggedToday
                )
            }

            // Missions section
            Text(
                text = "Missions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color(0xFFF0F4FF))
                    .clip(RoundedCornerShape(12.dp))
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Display the completed missions counter
                    Text(
                        text = "Completed Missions: $completedMissionsCount/$totalMissionsCount",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    if (missions.isEmpty()) {
                        Text(
                            text = "No missions available.",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {

                        LazyColumn (
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(sortedMissions) { mission ->
                                MissionItem(mission)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StreakCard(
    streakCount: Int,
    hasLoggedToday: Boolean,
    modifier: Modifier = Modifier
) {
    // Animation for scale effect
    val scale = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .scale(scale.value),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = if (hasLoggedToday) {
                            listOf(Color(0xFFFF6F61), Color(0xFFFFA07A)) // Warm gradient for active streak
                        } else {
                            listOf(Color(0xFF2E3D83), Color(0xFF5A6FBB)) // Cool gradient for inactive streak
                        }
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalFireDepartment,
                    contentDescription = "Streak Icon",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$streakCount Day Streak",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    Text(
                        text = if (hasLoggedToday) "Keep it up!" else "Log today to continue!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}


@Composable
fun MissionItem(mission: Mission) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (mission.isCompleted) Color(0xFFCCFFCC) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = mission.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = mission.description,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { mission.currentCount.toFloat() / mission.targetCount },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    color = Color(0xFF2E3D83),
                    trackColor = Color.LightGray
                )
                Text(
                    text = "${mission.currentCount}/${mission.targetCount}",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            if (mission.isCompleted) {
                Text(
                    text = "Completed!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E3D83),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}


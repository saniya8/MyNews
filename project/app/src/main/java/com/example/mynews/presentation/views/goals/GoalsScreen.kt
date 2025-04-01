package com.example.mynews.presentation.views.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mynews.domain.model.Mission
import com.example.mynews.presentation.viewmodel.goals.GoalsViewModel


@Composable
fun GoalsScreen(
    navController: NavHostController,
    goalsViewModel: GoalsViewModel,
) {
    val streakCount by goalsViewModel.streakCount.observeAsState(0)
    val hasLoggedToday by goalsViewModel.hasLoggedToday
    val missions by goalsViewModel.missions.observeAsState(emptyList())

    // these are still hard coded but to be replaced with Firestore data later
    val sampleAchievements = listOf(
        Achievement("1 Week Streak"),
        Achievement("5 Friends Added"),
        Achievement("First Post"),
        Achievement("10 Comments"),
        Achievement("30 Days Active"),
        Achievement("100 Likes"),
        Achievement("Read 5 Articles"),
        Achievement("Sports Enthusiast")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Achievements",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = Color.Black
        )

        // Streak Banner
        Card(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E3D83))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$streakCount Day Streak",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }

        // Missions Section
        Text(
            text = "Missions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
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
                if (missions.isEmpty()) {
                    Text(
                        text = "No missions available.",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    missions.forEach { mission ->
                        MissionItem(mission)
                    }
                }
            }
        }

        // Achievements Section


    }
}

@Composable
fun AchievementItem(achievement: Achievement) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Star, // Replace with actual medal icon
            contentDescription = achievement.name,
            modifier = Modifier.size(48.dp),
            tint = Color(0xFFFFD700) // Gold color
        )
        Text(
            text = achievement.name,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
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

// Data class for Achievements
data class Achievement(val name: String)
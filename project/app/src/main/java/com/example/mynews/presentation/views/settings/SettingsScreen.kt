package com.example.mynews.presentation.views.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.mynews.presentation.viewmodel.settings.SettingsViewModel
import com.example.mynews.ui.theme.NavyBlue
import com.example.mynews.domain.repositories.UserRepository
import com.example.mynews.ui.theme.CaptainBlue
import androidx.compose.material3.TextFieldDefaults


@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToAuthScreen: () -> Unit,
    userRepository: UserRepository,
) {
    val logoutState by settingsViewModel.logoutState.collectAsState()
    val username by settingsViewModel.username.collectAsState()
    var articleLength by remember { mutableStateOf(100) } // Default value
    var articleLengthText by remember { mutableStateOf(articleLength.toString()) }
    var isValidInput by rememberSaveable { mutableStateOf(true) }
    val wordLimit by settingsViewModel.wordLimit.collectAsState()
    var tempInput by rememberSaveable { mutableStateOf(wordLimit.toString()) }

    LaunchedEffect(Unit) {
        val userID = userRepository.getCurrentUserId()
        settingsViewModel.getUsername(userID!!)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Settings",
                    fontWeight = FontWeight.Bold,
                    color = CaptainBlue,
                    fontSize = 25.sp,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    border = BorderStroke(1.dp, Color.Gray), // Thin gray border
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))

                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Username: ${username ?: "Loading..."}",
                            fontSize = 20.sp,
                            color = Color.Gray
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Condensed Word Count:",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(40.dp)
                                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
                                    .background(Color.Transparent) // Optional, adjust as needed
                            ) {
                                OutlinedTextField(
                                    value = tempInput,
                                    onValueChange = { tempInput = it },
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .clip(CircleShape)
                                        .clickable { tempInput = "" }, // Clears input when clicked
                                    shape = CircleShape,
                                    textStyle = TextStyle(fontSize = 16.sp, textAlign = TextAlign.Center, color = Color.Black),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            val number = tempInput.toIntOrNull()
                                            if (number in 50..200) {
                                                println("valid number: $number")
                                                articleLength = number ?: articleLength
                                                articleLengthText = tempInput
                                                isValidInput = true
                                                settingsViewModel.updateWordLimit(articleLength)
                                            } else {
                                                println("invalid number: $number")
                                                tempInput = articleLengthText // Revert if invalid
                                                isValidInput = false
                                            }
                                        }
                                    ),
                                    isError = !isValidInput,
                                    placeholder = { Text("50 - 200", color = Color.Gray) },
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        cursorColor = Color.Black,
                                        focusedPlaceholderColor = Color.Gray,
                                        unfocusedPlaceholderColor = Color.Gray,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent, // Removes border
                                        unfocusedIndicatorColor = Color.Transparent // Removes border
                                    )
                                )

                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    onClick = {
                        settingsViewModel.logout()
                        onNavigateToAuthScreen()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyBlue,
                        contentColor = Color.White,
                    )
                ) {
                    Text("Log Out")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Delete Account",
                    color = NavyBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable { /* Handle account deletion */ }
                        .padding(8.dp),
                    style = TextStyle(textDecoration = TextDecoration.Underline)
                )
            }
        }
    }
}


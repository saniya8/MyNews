package com.example.mynews.presentation.views.settings

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.mynews.presentation.viewmodel.settings.SettingsViewModel
import com.example.mynews.ui.theme.CaptainBlue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import com.example.mynews.presentation.components.ScreenHeader


@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToAuthScreen: () -> Unit,
) {
    val logoutState by settingsViewModel.logoutState.collectAsState()
    val username by settingsViewModel.username.collectAsState()
    val email by settingsViewModel.email.collectAsState()
    var articleLength by remember { mutableStateOf(100) } // Default value
    var articleLengthText by remember { mutableStateOf(articleLength.toString()) }
    var isValidInput by rememberSaveable { mutableStateOf(true) }



    //val wordLimit by settingsViewModel.wordLimit.collectAsState()
    //var tempInputWordLimit by rememberSaveable { mutableStateOf(wordLimit.toString()) }


    LaunchedEffect(Unit) {
        settingsViewModel.fetchUsername()
        settingsViewModel.fetchEmail()
    }

    val focusManager = LocalFocusManager.current



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {

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
                    title = "Settings",
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight() // only takes up as much height as needed
                        //.padding(innerPadding)
                        //.background(Color.Cyan)
                ) {

                    Text(
                        text = "Your Account",
                        fontWeight = FontWeight.Bold,
                        color = CaptainBlue,
                        fontSize = 23.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Your username is: $username",
                        fontWeight = FontWeight.Normal,
                        color = CaptainBlue,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Your email is: $email",
                        fontWeight = FontWeight.Normal,
                        color = CaptainBlue,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                    )


                }

                Spacer(modifier = Modifier.height(70.dp))


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        //.padding(innerPadding)
                        //.background(Color.Green)
                ) {

                    Text(
                        text = "Preferences",
                        fontWeight = FontWeight.Bold,
                        color = CaptainBlue,
                        fontSize = 23.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Set the word limit for your condensed article summaries",
                        fontWeight = FontWeight.Normal,
                        color = CaptainBlue,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    WordLimitInputField(settingsViewModel)

                }

                Spacer(modifier = Modifier.height(60.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        //.padding(innerPadding)
                        //.background(Color.Yellow)
                ) {

                    Text(
                        text = "Manage Account",
                        fontWeight = FontWeight.Bold,
                        color = CaptainBlue,
                        fontSize = 23.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Log out of your account",
                        fontWeight = FontWeight.Normal,
                        color = CaptainBlue,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            settingsViewModel.logout()
                            onNavigateToAuthScreen()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CaptainBlue,
                            contentColor = Color.White,
                        )
                    ) {
                        Text("Log Out")
                    }

                    Spacer(modifier = Modifier.height(25.dp))

                    Text(
                        text = "Delete your account. This will permanently remove all your data",
                        fontWeight = FontWeight.Normal,
                        color = CaptainBlue,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            //settingsViewModel.logout()
                            onNavigateToAuthScreen()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = Color.White,
                        )
                    ) {
                        Text("Delete Account")
                    }




                }
                Spacer(modifier = Modifier.height(10.dp))

            }

        } // end of scaffold
    }

}


@Composable
fun WordLimitInputField(
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val wordLimit by settingsViewModel.wordLimit.collectAsState()
    var originalWordLimit by rememberSaveable { mutableStateOf<Int?>(null) }
    var tempInputWordLimit by rememberSaveable { mutableStateOf("100") }
    var wasFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tempInputWordLimit = wordLimit.toString()
    }

    val isError = tempInputWordLimit.toIntOrNull()?.let { it !in 50..200 } ?: true

    Column {
        OutlinedTextField(
            value = tempInputWordLimit,
            onValueChange = { newValue ->
                // only accept digits
                if (newValue.all { it.isDigit() }) {
                    tempInputWordLimit = newValue

                    val newValueInt = newValue.toIntOrNull()
                    if (newValueInt != null) {
                        settingsViewModel.updateWordLimit(newValueInt)
                    }

                }
                // Non-numeric input is silently ignored
            },
            label = { Text("Set Limit (50 - 200)") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus() // triggers .onfocuschanged for the field to handle
                }
            ),

            singleLine = true,

            isError = isError,
            supportingText = {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (isError) {
                        Text(
                            text = when {
                                tempInputWordLimit.isEmpty() -> "Value is required"
                                tempInputWordLimit.toIntOrNull() != null && tempInputWordLimit.toInt() < 50 -> "Minimum value is 50"
                                tempInputWordLimit.toIntOrNull() != null && tempInputWordLimit.toInt() > 200 -> "Maximum value is 200"
                                else -> "Invalid input"
                            },
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Spacer(modifier = Modifier)
                    }

                    if (originalWordLimit != null && tempInputWordLimit != originalWordLimit.toString()) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopEnd
                        ) {

                            Text(
                                text = "Cancel",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge, // or bodySmall if you want it smaller
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = LocalIndication.current
                                    ) {
                                        tempInputWordLimit = originalWordLimit.toString()
                                        val wordLimitToUse: Int = originalWordLimit ?: wordLimit
                                        settingsViewModel.updateWordLimit(wordLimitToUse)
                                        focusManager.clearFocus()
                                    }
                            )

                        }
                    }

                }


            },


            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .onFocusChanged { focusState ->
                    val nowFocused = focusState.isFocused

                    if (nowFocused && originalWordLimit == null) {
                        // just came into focus
                        originalWordLimit = wordLimit
                    }

                    if (wasFocused && !nowFocused) {

                        val newValueInt = tempInputWordLimit.toIntOrNull()

                        val isValid = newValueInt != null && newValueInt in 50..200

                        if (!isValid) {
                            // Revert to original value
                            val wordLimitToUse: Int = originalWordLimit ?: wordLimit
                            tempInputWordLimit = wordLimitToUse.toString()
                            settingsViewModel.updateWordLimit(wordLimitToUse)
                        } // else
                        // since valid, it's already stored in the viewmodel

                        originalWordLimit = null


                    }
                    wasFocused = nowFocused
                }

        )
    }
}








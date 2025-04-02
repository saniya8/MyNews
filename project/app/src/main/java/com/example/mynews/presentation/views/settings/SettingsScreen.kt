package com.example.mynews.presentation.views.settings

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.navigation.NavHostController
import com.example.mynews.presentation.viewmodel.settings.SettingsViewModel
import com.example.mynews.ui.theme.CaptainBlue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import com.example.mynews.presentation.components.ScreenHeader
import com.example.mynews.presentation.viewmodel.settings.DeleteAccountResult


@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    onNavigateToAuthScreen: () -> Unit,
) {
    val logoutState by settingsViewModel.logoutState.collectAsState()
    val username by settingsViewModel.username.collectAsState()
    val email by settingsViewModel.email.collectAsState()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val deleteAccountState by settingsViewModel.deleteAccountState.collectAsState()
    val isDeletingAccount by settingsViewModel.isDeletingAccount.collectAsState()




    LaunchedEffect(Unit) {
        settingsViewModel.fetchUsername()
        settingsViewModel.fetchEmail()
        settingsViewModel.fetchNumWordsToSummarize()
    }

    // navigate back to auth after logout
    LaunchedEffect(logoutState) {
        if (logoutState == true) {
            onNavigateToAuthScreen()
            settingsViewModel.resetLogoutState() // reset to avoid retriggers
        }
    }

    // navigate back to auth after delete account
    LaunchedEffect(deleteAccountState) {
        if (deleteAccountState == DeleteAccountResult.Success) {
            onNavigateToAuthScreen()
            settingsViewModel.resetDeleteAccountState()
        }
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
                        text = "Choose how many words the condensed articles should summarize from: 50 to 200 words",
                        fontWeight = FontWeight.Normal,
                        color = CaptainBlue,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    NumWordsToSummarizeField(settingsViewModel)

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
                            //onNavigateToAuthScreen() // done in launched effect
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
                        text = "Delete your account",
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
                            showDeleteDialog = true
                            //onNavigateToAuthScreen()
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

            // Show delete confirmation dialog
            if (showDeleteDialog) {
                DeleteAccountDialog(
                    onConfirm = { password ->
                        showDeleteDialog = false
                        settingsViewModel.deleteAccount(password)
                    },
                    onDismiss = {
                        showDeleteDialog = false
                        settingsViewModel.resetDeleteAccountState()
                    },
                    deleteAccountState = deleteAccountState,
                )
            }


        } // end of scaffold
    }

    if (isDeletingAccount) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)) // dims background
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {}, // consumes clicks
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center) // Center the loading circle
                    .size(32.dp),
                color = Color.White,
                strokeWidth = 4.dp
            )
        }
    }

}


@Composable
fun NumWordsToSummarizeField(
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val numWordsToSummarize by settingsViewModel.numWordsToSummarize.collectAsState()
    var originalNumWordsToSummarize by rememberSaveable { mutableStateOf<Int?>(null) }
    var tempInputNumWordsToSummarize by rememberSaveable { mutableStateOf("") }
    var wasFocused by remember { mutableStateOf(false) }

    val hasLoaded by settingsViewModel.hasLoadedNumWordsToSummarize.collectAsState()
    var hasInitializedInput by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(hasLoaded) {
        if (hasLoaded) {
            tempInputNumWordsToSummarize = numWordsToSummarize.toString() // load the value from firestore
            hasInitializedInput = true
            settingsViewModel.resetHasLoadedNumWordsToSummarize() // change hasLoaded to false so it doesnt keep loading from firestore, lets user type freely
        }
    }

    val isError = if (!hasInitializedInput) {
        false
    } else {
        tempInputNumWordsToSummarize.toIntOrNull()?.let { it !in 50..200 } ?: true
    }

    Column {
        OutlinedTextField(
            value = tempInputNumWordsToSummarize,
            onValueChange = { newNumWords ->
                // only accept digits
                if (newNumWords.all { it.isDigit() }) {
                    tempInputNumWordsToSummarize = newNumWords

                    val newNumWordsInt = newNumWords.toIntOrNull()
                    if (newNumWordsInt != null) {
                        settingsViewModel.updateNumWordsToSummarize(newNumWordsInt)
                    }

                }
                // Non-numeric input is silently ignored
            },
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
                                tempInputNumWordsToSummarize.isEmpty() -> "Value is required"
                                tempInputNumWordsToSummarize.toIntOrNull() != null && tempInputNumWordsToSummarize.toInt() < 50 -> "Minimum value is 50"
                                tempInputNumWordsToSummarize.toIntOrNull() != null && tempInputNumWordsToSummarize.toInt() > 200 -> "Maximum value is 200"
                                else -> "Invalid input"
                            },
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Spacer(modifier = Modifier)
                    }

                    if (originalNumWordsToSummarize != null && tempInputNumWordsToSummarize != originalNumWordsToSummarize.toString()) {

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
                                        tempInputNumWordsToSummarize = originalNumWordsToSummarize.toString()
                                        val numWordsToUse: Int = originalNumWordsToSummarize ?: numWordsToSummarize
                                        settingsViewModel.updateNumWordsToSummarize(numWordsToUse)
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

                    if (nowFocused && originalNumWordsToSummarize == null) {
                        // just came into focus
                        originalNumWordsToSummarize = numWordsToSummarize
                    }

                    if (wasFocused && !nowFocused) {

                        val newNumWordsInt = tempInputNumWordsToSummarize.toIntOrNull()

                        val isValid = newNumWordsInt != null && newNumWordsInt in 50..200

                        if (!isValid) {
                            // Revert to original value
                            val numWordsToUse: Int = originalNumWordsToSummarize ?: numWordsToSummarize
                            tempInputNumWordsToSummarize = numWordsToUse.toString()
                            settingsViewModel.updateNumWordsToSummarize(numWordsToUse)
                        } // else
                        // since valid, it's already stored in the viewmodel

                        originalNumWordsToSummarize = null


                    }
                    wasFocused = nowFocused
                }

        )
    }
}

@Composable
fun DeleteAccountDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    deleteAccountState: DeleteAccountResult?
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showIncorrectPasswordError by remember { mutableStateOf(false) }

    // watch for incorrect password state
    LaunchedEffect(deleteAccountState) {
        showIncorrectPasswordError = deleteAccountState == DeleteAccountResult.IncorrectPassword
    }



    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(
            text = "Delete Account",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()

        )},
        text = {
            Column {
                Text(
                    text = "Deleting your account will permanently remove all your data. This action cannot be undone.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Enter Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = null)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                if (showIncorrectPasswordError) {
                    Text(
                        text = "Incorrect password. Please try again.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(password) },
                enabled = password.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}









package com.example.mynews.presentation.views.authentication

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mynews.presentation.viewmodel.RegisterViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mynews.presentation.components.*
import com.example.mynews.ui.theme.*
import android.util.Log

// Source: https://www.youtube.com/watch?v=aCjOmyd_62U&t=0s&ab_channel=KApps

@Composable
fun RegisterScreen(
    onRegisterSuccessNavigation: () -> Unit,
    onNavigateToLoginScreen: () -> Unit,
    registerViewModel: RegisterViewModel = hiltViewModel()
) {

    NavDestinationHelper(
        shouldNavigate = {
            registerViewModel.registerState.isSuccessfullyRegistered

        },
        destination = {
            onRegisterSuccessNavigation()
        }
    )

    // Show error dialog if needed
    if (registerViewModel.showErrorDialog) {
        AlertDialog(
            onDismissRequest = { registerViewModel.showErrorDialog = false },
            title = { Text("Registration Error") },
            text = { Text("Could not register. Please verify that you don't have an account under this email address.\nIf you don't have an account, please verify your internet connection, and try again.") },
            confirmButton = {
                Button(
                    onClick = { registerViewModel.showErrorDialog = false },
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("OK")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ){
            HeaderBackground(
                leftColor = CaptainBlue,
                rightColor = CaptainBlue,
                modifier = Modifier
                    .fillMaxSize()
            )
            Text(
                text = "\nMyNews",
                style = MaterialTheme.typography.h4,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        RegisterContainer(
            emailValue = {
                registerViewModel.registerState.emailInput
            },
            usernameValue = {
                registerViewModel.registerState.usernameInput
            },
            passwordValue = {
                registerViewModel.registerState.passwordInput
            },
            passwordRepeatedValue = {
                registerViewModel.registerState.passwordRepeatedInput
            },
            buttonEnabled = {
                registerViewModel.registerState.isInputValid
            },
            onEmailChanged = registerViewModel::onEmailInputChange,
            onUsernameChanged = registerViewModel::onUsernameInputChange,
            onPasswordChanged = registerViewModel::onPasswordInputChange,
            onPasswordRepeatedChanged = registerViewModel::onPasswordRepeatedInputChange,
            onButtonClick = registerViewModel::onRegisterClick,
            isPasswordShown = {
                registerViewModel.registerState.isPasswordShown
            },
            isPasswordRepeatedShown = {
                registerViewModel.registerState.isPasswordRepeatedShown
            },
            onTrailingPasswordIconClick = {
                registerViewModel.onToggleVisualTransformationPassword()
            },
            onTrailingPasswordRepeatedIconClick = {
                registerViewModel.onToggleVisualTransformationPasswordRepeated()
            },
            errorHint = {
                registerViewModel.registerState.errorMessageInput
            },
            isLoading = {
                registerViewModel.registerState.isLoading
            },
            modifier = Modifier
                .padding(top = 200.dp)
                .fillMaxWidth(0.9f)
                .shadow(5.dp, RoundedCornerShape(15.dp))
                .background(CaptainBlue, RoundedCornerShape(15.dp))
                .padding(10.dp, 15.dp, 10.dp, 5.dp)
                .align(Alignment.TopCenter)
        )
        Row(
            modifier = Modifier
                .padding(bottom = 10.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                "Already have an account?",
                style = MaterialTheme.typography.body2,
            )
            Text(
                modifier = Modifier
                    .padding(start = 5.dp)
                    .clickable {
                        onNavigateToLoginScreen()
                    },
                text = "Login",
                color = BrightBlue,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Composable
fun RegisterContainer(
    emailValue:() -> String,
    usernameValue:() -> String,
    passwordValue:() -> String,
    passwordRepeatedValue:() -> String,
    buttonEnabled:() -> Boolean,
    onEmailChanged:(String)->Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged:(String)->Unit,
    onPasswordRepeatedChanged:(String)->Unit,
    onButtonClick:()->Unit,
    isPasswordShown: ()->Boolean,
    isPasswordRepeatedShown: ()->Boolean,
    onTrailingPasswordIconClick: ()->Unit,
    onTrailingPasswordRepeatedIconClick: ()->Unit,
    errorHint:() -> String?,
    isLoading:() -> Boolean,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        TextEntryModule(
            modifier = Modifier
                .fillMaxWidth(),
            description = "Email address",
            hint = "Enter valid email",
            leadingIcon = Icons.Default.Email,
            textValue = emailValue(),
            textColour = Color.White,
            cursorColour = BlueGrey,
            onValueChanged = onEmailChanged,
            trailingIcon = null,
            onTrailingIconClick = null
        )

        TextEntryModule(
            modifier = Modifier
                .fillMaxWidth(),
            description = "Username",
            hint = "Choose a username",
            leadingIcon = Icons.Default.AccountCircle,
            textValue = usernameValue(),
            textColour = Color.White,
            cursorColour = BlueGrey,
            onValueChanged = onUsernameChanged,
            trailingIcon = null,
            onTrailingIconClick = null
        )

        TextEntryModule(
            modifier = Modifier
                .fillMaxWidth(),
            description = "Password",
            hint = "Enter Password",
            leadingIcon = Icons.Default.VpnKey,
            textValue = passwordValue(),
            textColour = Color.White,
            cursorColour = BlueGrey,
            onValueChanged = onPasswordChanged,
            keyboardType = KeyboardType.Password,
            visualTransformation = if (isPasswordShown()) {
                VisualTransformation.None
            } else PasswordVisualTransformation(),
            trailingIcon = Icons.Default.RemoveRedEye,
            onTrailingIconClick = {
                onTrailingPasswordIconClick()
            }
        )
        TextEntryModule(
            modifier = Modifier
                .fillMaxWidth(),
            description = "Password repeated",
            hint = "Enter password again",
            leadingIcon = Icons.Default.VpnKey,
            textValue = passwordRepeatedValue(),
            textColour = Color.White,
            cursorColour = BlueGrey,
            onValueChanged = onPasswordRepeatedChanged,
            keyboardType = KeyboardType.Password,
            visualTransformation = if (isPasswordRepeatedShown()) {
                VisualTransformation.None
            } else PasswordVisualTransformation(),
            trailingIcon = Icons.Default.RemoveRedEye,
            onTrailingIconClick = {
                onTrailingPasswordRepeatedIconClick()
            }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            AuthButton(
                text = "Register",
                backgroundColour = Lavender,
                contentColour = CaptainBlue,
                enabled = buttonEnabled(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .shadow(5.dp, RoundedCornerShape(25.dp)),
                onButtonClick = onButtonClick,
                isLoading = isLoading()
            )
            Text(
                errorHint() ?: "",
                style = MaterialTheme.typography.caption,
                color = Color.White

            )
        }
    }
}
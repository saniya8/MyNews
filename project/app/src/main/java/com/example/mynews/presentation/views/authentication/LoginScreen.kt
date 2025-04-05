package com.example.mynews.presentation.views.authentication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mynews.presentation.components.*
import com.example.mynews.presentation.viewmodel.authentication.LoginViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mynews.presentation.theme.BlueGrey
import com.example.mynews.presentation.theme.BrightBlue
import com.example.mynews.presentation.theme.CaptainBlue
import com.example.mynews.presentation.theme.SkyBlue

// Source: https://www.youtube.com/watch?v=aCjOmyd_62U&t=0s&ab_channel=KApps

@Composable
fun LoginScreen(
    onLoginSuccessNavigation: () -> Unit,
    onNavigateToRegisterScreen: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel()
) {

    // Show error dialog if needed
    if (loginViewModel.showErrorDialog) {
        AlertDialog(
            onDismissRequest = { loginViewModel.showErrorDialog = false },
            title = { Text("Login Error") },
            text = { Text("Could not login. Please check your email and password, and your internet connection.") },
            confirmButton = {
                Button(
                    onClick = { loginViewModel.showErrorDialog = false },
                    shape = RoundedCornerShape(25.dp),
                ) {
                    Text("OK")
                }
            }
        )
    }

    NavDestinationHelper(
        shouldNavigate = {
            loginViewModel.loginState.isSuccessfullyLoggedIn
        },
        destination = {
            onLoginSuccessNavigation()
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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

        LoginContainer(
            emailValue = {
                loginViewModel.loginState.emailInput
            },
            passwordValue = {
                loginViewModel.loginState.passwordInput
            },
            buttonEnabled = {
                loginViewModel.loginState.isInputValid
            },
            onEmailChanged = loginViewModel::onEmailInputChange,
            onPasswordChanged = loginViewModel::onPasswordInputChange,
            onLoginButtonClick = loginViewModel::onLoginClick,
            isPasswordShown = {
                loginViewModel.loginState.isPasswordShown
            },
            onTrailingPasswordIconClick = loginViewModel::onToggleVisualTransformation,
            errorHint = {
                loginViewModel.loginState.errorMessageInput
            },
            isLoading = {
                loginViewModel.loginState.isLoading
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
                .padding(bottom = 20.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                "No account yet?",
                style = MaterialTheme.typography.body2,
                fontSize = 16.sp,
            )
            Text(
                "Register",
                modifier = Modifier
                    .padding(start = 5.dp)
                    .clickable {
                        onNavigateToRegisterScreen()
                    },
                color = BrightBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                style = MaterialTheme.typography.body2
            )
        }
    }

}

@Composable
fun LoginContainer(
    emailValue:() -> String,
    passwordValue:()-> String,
    buttonEnabled:() -> Boolean,
    onEmailChanged:(String) -> Unit,
    onPasswordChanged:(String) -> Unit,
    onLoginButtonClick:()->Unit,
    isPasswordShown:()->Boolean,
    onTrailingPasswordIconClick: () -> Unit,
    errorHint:()->String?,
    isLoading:()->Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ){
        FieldEntry(
            modifier = Modifier
                .fillMaxWidth(),
            description = "Email address",
            hint = "Enter valid email",
            textValue = emailValue(),
            textColour = Color.White,
            cursorColour = BlueGrey,
            onValueChanged = onEmailChanged,
            trailingIcon = null,
            onTrailingIconClick = null,
            leadingIcon = Icons.Default.Email
        )
        FieldEntry(
            modifier = Modifier
                .fillMaxWidth(),
            description = "Password",
            hint = "Enter password",
            textValue = passwordValue(),
            textColour = Color.White,
            cursorColour = BlueGrey,
            onValueChanged = onPasswordChanged,
            trailingIcon = Icons.Default.RemoveRedEye,
            onTrailingIconClick = {
                onTrailingPasswordIconClick()
            },
            leadingIcon = Icons.Default.VpnKey,
            visualTransformation = if(isPasswordShown()){
                VisualTransformation.None
            } else PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ){
            AuthButton(
                text = "Login",
                backgroundColour = SkyBlue,
                contentColour = CaptainBlue,
                enabled = buttonEnabled(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .shadow(5.dp, RoundedCornerShape(25.dp)),
                isLoading = isLoading(),
                onButtonClick = onLoginButtonClick
            )
            Text(
                errorHint() ?: "",
                style = MaterialTheme.typography.caption,
                color = Color.White
            )
        }
    }
}
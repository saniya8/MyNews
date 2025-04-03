package com.example.mynews.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mynews.ui.theme.*

// Purpose: Authentication Button on the LoginScreen and RegisterScreen

@Composable
fun AuthButton(
    text: String,
    backgroundColour: Color,
    contentColour: Color,
    enabled: Boolean = true,
    onButtonClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
    ) {

    Button(
        modifier = modifier,
        onClick = {
            onButtonClick()
        },
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColour,
            contentColor = contentColour,
            disabledBackgroundColor = backgroundColour,
            disabledContentColor = contentColour,
        ),

        enabled = enabled
    ) {
        if(isLoading) { // loading, waiting for result
            /*CircularProgressIndicator(
                color = contentColour,
                modifier = Modifier.size(20.dp),
            )*/
            LoadingIndicator(
                color = contentColour,
                size = 20.dp
            )
        } else { // not loading
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                //style = MaterialTheme.typography.body1,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthButtonPreview() {
    AuthButton(
        text = "Login",
        backgroundColour = SkyBlue,
        contentColour = Color.White,
        onButtonClick = { /*TODO*/ },
        isLoading = true,
        modifier = Modifier
            .fillMaxWidth()
    )
}


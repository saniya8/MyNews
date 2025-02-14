package com.example.mynews.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mynews.ui.theme.*

//Purpose: Used to create the fields in the LoginScreen and RegisterScreen

@Composable
fun FieldEntry(
    description: String,
    hint: String,
    leadingIcon: ImageVector,
    textValue: String,
    keyboardType: KeyboardType = KeyboardType.Ascii,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textColour: Color,
    cursorColour: Color,
    onValueChanged: (String) -> Unit,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick:(()->Unit)?,
    modifier: Modifier = Modifier
    ) {

    Column(
        modifier = modifier
    ) {
        Text(
            text = description,
            color = textColour,
            style = MaterialTheme.typography.body2
        )

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 3.dp)
                .border(0.5.dp, textColour, RoundedCornerShape(25.dp))
                .height(50.dp)
                .shadow(3.dp, RoundedCornerShape(25.dp)),
            value = textValue,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White,
                cursorColor = cursorColour,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            onValueChange = onValueChanged,
            shape = RoundedCornerShape(25.dp),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = cursorColour
                )
            },
            trailingIcon = {
                if(trailingIcon != null){
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = cursorColour,
                        modifier = Modifier
                            .clickable {
                                if(onTrailingIconClick != null) onTrailingIconClick()
                            }
                    )
                }
            },
            placeholder = {
                Text(
                    hint,
                    style = MaterialTheme.typography.body2
                )
            },
            textStyle = MaterialTheme.typography.body2,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FieldEntryModulePreview(){
    FieldEntry(
        description = "Email address",
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp,0.dp,10.dp,5.dp),
        hint = "Enter valid email",
        leadingIcon = Icons.Default.Email,
        textValue = "Text Input",
        textColour = Color.Black,
        cursorColour = NavyBlue,
        onValueChanged = {},
        trailingIcon = Icons.Filled.RemoveRedEye,
        onTrailingIconClick = {},
        visualTransformation = PasswordVisualTransformation() // changes textValue to be hidden
    )
}


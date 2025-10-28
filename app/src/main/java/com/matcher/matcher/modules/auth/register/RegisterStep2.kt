package com.matcher.matcher.modules.auth.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.matcher.matcher.R

@Composable
fun RegisterStep2(
    password: String,
    confirmPassword: String,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    showHeader: Boolean = true
) {
    val passwordVisible = remember { mutableStateOf(false) }
    val confirmVisible = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        if (showHeader) {
            Text(
                text = "Güçlü bir şifre belirleyin.",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Şifre en az 6 karakterden oluşmalıdır.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(text = R.string.password.let { id -> stringResource(id) }) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Icon(
                        painter = painterResource(id = if (passwordVisible.value) R.drawable.ic_eye_closed else R.drawable.ic_eye),
                        contentDescription = null,
                        tint = Color.Black.copy(alpha = 0.7f)
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.primaryColor),
                unfocusedBorderColor = colorResource(id = R.color.whiteButtonStrokeColor),
                cursorColor = colorResource(id = R.color.primaryColor),
                focusedLabelColor = colorResource(id = R.color.primaryColor),
                unfocusedLabelColor = Color.Black.copy(alpha = 0.6f),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text(text = "Şifreyi Doğrula") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (confirmVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmVisible.value = !confirmVisible.value }) {
                    Icon(
                        painter = painterResource(id = if (confirmVisible.value) R.drawable.ic_eye_closed else R.drawable.ic_eye),
                        contentDescription = null,
                        tint = Color.Black.copy(alpha = 0.7f)
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.primaryColor),
                unfocusedBorderColor = colorResource(id = R.color.whiteButtonStrokeColor),
                cursorColor = colorResource(id = R.color.primaryColor),
                focusedLabelColor = colorResource(id = R.color.primaryColor),
                unfocusedLabelColor = Color.Black.copy(alpha = 0.6f),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}



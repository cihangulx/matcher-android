package com.matcher.matcher.modules.auth.forgot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.matcher.matcher.modules.auth.register.RegisterStep2

@Composable
fun ForgotStep3(
    password: String,
    confirmPassword: String,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        RegisterStep2(
            password = password,
            confirmPassword = confirmPassword,
            onPasswordChange = onPasswordChange,
            onConfirmPasswordChange = onConfirmPasswordChange,
            showHeader = false
        )
    }
}



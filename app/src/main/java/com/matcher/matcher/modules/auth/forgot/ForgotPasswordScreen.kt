package com.matcher.matcher.modules.auth.forgot

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.matcher.matcher.R
import com.matcher.matcher.modules.auth.components.BottomActions
import com.matcher.matcher.modules.auth.components.StepView
import com.matcher.matcher.models.auth.ForgotPasswordRequest
import com.matcher.matcher.network.ApiClient
import com.matcher.matcher.network.model.ApiException
import com.matcher.matcher.utils.helpers.ToastHelper
import kotlinx.coroutines.launch
import android.app.Activity

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit = {},
    onFinished: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val stepState = remember { mutableStateOf(1) }
    val emailState = remember { mutableStateOf("") }
    val codeState = remember { mutableStateOf("") }
    val newPasswordState = remember { mutableStateOf("") }
    val confirmPasswordState = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    
    fun sendForgotPasswordRequest() {
        if (emailState.value.isBlank()) {
            (context as? Activity)?.let { activity ->
                ToastHelper.showWarning(activity, "Email adresi gereklidir")
            }
            return
        }
        
        coroutineScope.launch {
            isLoading.value = true
            try {
                val authService = ApiClient.getInstance(context).authService
                val request = ForgotPasswordRequest(email = emailState.value.trim())
                
                val response = authService.forgotPassword(request)
                
                if (response.success) {
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showSuccess(activity, "Şifre sıfırlama kodu email adresinize gönderildi")
                    }
                    stepState.value = 2 // Bir sonraki adıma geç
                } else {
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showError(activity, response.message ?: "Kod gönderilemedi")
                    }
                }
            } catch (e: Exception) {
                val errorMessage = ApiException.getErrorMessage(e)
                (context as? Activity)?.let { activity ->
                    ToastHelper.showError(activity, errorMessage)
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.page_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepView(currentStep = stepState.value, totalSteps = 3)

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (stepState.value) {
                    1 -> ForgotStep1(
                        email = emailState.value,
                        onEmailChange = { emailState.value = it }
                    )
                    2 -> ForgotStep2(code = codeState.value, onCodeChange = { codeState.value = it })
                    else -> ForgotStep3(
                        password = newPasswordState.value,
                        confirmPassword = confirmPasswordState.value,
                        onPasswordChange = { newPasswordState.value = it },
                        onConfirmPasswordChange = { confirmPasswordState.value = it }
                    )
                }
            }

            BottomActions(
                onBack = {
                    if (stepState.value > 1) stepState.value -= 1 else onBack()
                },
                onContinue = {
                    when (stepState.value) {
                        1 -> sendForgotPasswordRequest() // API çağrısı yap
                        2 -> stepState.value = 3 // Kod doğrulama (şimdilik geç)
                        3 -> onFinished() // Şifre değiştirme tamamlandı
                    }
                },
                context = context,
                isLoading = isLoading.value
            )
        }
    }
}

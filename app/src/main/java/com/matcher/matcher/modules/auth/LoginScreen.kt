package com.matcher.matcher.modules.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.matcher.matcher.network.ApiClient
import com.matcher.matcher.utils.PreferencesManager
import com.matcher.matcher.network.model.ApiException
import com.matcher.matcher.models.auth.request.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.matcher.matcher.modules.terms.TermsActivity
import com.matcher.matcher.network.Routes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.matcher.matcher.R
import com.matcher.matcher.models.user.User
import com.matcher.matcher.utils.helpers.ToastHelper
import android.app.Activity

@Composable
fun LoginScreen(onLogin: () -> Unit = {}, onForgotClick: () -> Unit = {}, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }
    
    val preferencesManager = remember { PreferencesManager.getInstance(context) }
    val apiClient = remember { ApiClient.getInstance(context) }
    
    val performLogin: () -> Unit = {
        if (emailState.value.isBlank() || passwordState.value.isBlank()) {
            (context as? Activity)?.let { activity ->
                ToastHelper.showWarning(activity, "Lütfen tüm alanları doldurun")
            }
        } else {
            isLoading.value = true
        }
    }
    
    LaunchedEffect(isLoading.value) {
        if (isLoading.value) {
            try {
                val loginRequest = LoginRequest(
                    email = emailState.value.trim(),
                    password = passwordState.value
                )
                
                val response = withContext(Dispatchers.IO) {
                    apiClient.authService.login(loginRequest)
                }
                
                if (response.success && response.data != null) {
                    withContext(Dispatchers.IO) {
                        preferencesManager.saveAuthToken(response.data.token)
                    }

                    withContext(Dispatchers.IO) {
                        apiClient.authService.profile().data?.let {
                            User.current = it
                        }
                    }

                    onLogin()
                } else {
                    val errorMessage = response.message ?: "Giriş başarısız"
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showError(activity, errorMessage)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.page_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(0.dp))
            Text(
                text = stringResource(id = R.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                label = { Text(text = stringResource(id = R.string.email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                label = { Text(text = stringResource(id = R.string.password)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible.value) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                        Icon(
                            painter = painterResource(id = if (passwordVisible.value) R.drawable.ic_eye_closed else R.drawable.ic_eye),
                            contentDescription = null,
                            tint = Color.Black.copy(alpha = 0.7f)
                        )
                    }
                },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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

            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
            ) {
                Text(
                    text = "Şifremi Unuttum",
                    color = colorResource(id = R.color.primaryColor),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(0.dp)
                        .clickable { onForgotClick() }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onBack,
                        color = Color.White,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colorResource(id = R.color.whiteButtonStrokeColor)),
                        modifier = Modifier.height(60.dp)
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .height(60.dp)
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.left_arrow),
                                contentDescription = null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Button(
                            onClick = performLogin,
                            enabled = !isLoading.value,
                            modifier = Modifier
                                .matchParentSize(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.primaryDarkColor),
                                contentColor = Color.White,
                                disabledContainerColor = colorResource(id = R.color.primaryDarkColor).copy(alpha = 0.6f),
                                disabledContentColor = Color.White.copy(alpha = 0.6f)
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isLoading.value) "Giriş yapılıyor..." else stringResource(id = R.string.login_button)
                            )
                        }
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.right_arrow_white),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val linkColor = colorResource(id = R.color.primaryDarkColor)
                val terms = stringResource(id = R.string.terms_of_use)
                val privacy = stringResource(id = R.string.privacy_policy)
                val consent = stringResource(id = R.string.consent_text, terms, privacy)

                val annotatedText = buildAnnotatedString {
                    val termsStart = consent.indexOf(terms)
                    val privacyStart = consent.indexOf(privacy)
                    append(consent)
                    if (termsStart >= 0) {
                        addStyle(SpanStyle(color = linkColor, fontWeight = FontWeight.Medium), termsStart, termsStart + terms.length)
                        addStringAnnotation(tag = "TERMS", annotation = "terms", start = termsStart, end = termsStart + terms.length)
                    }
                    if (privacyStart >= 0) {
                        addStyle(SpanStyle(color = linkColor, fontWeight = FontWeight.Medium), privacyStart, privacyStart + privacy.length)
                        addStringAnnotation(tag = "PRIVACY", annotation = "privacy", start = privacyStart, end = privacyStart + privacy.length)
                    }
                }

                ClickableText(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = { offset ->
                        
                        annotatedText.getStringAnnotations(
                            tag = "TERMS",
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let {
                            TermsActivity.start(
                                context = context,
                                url = Routes.BASE_URL + Routes.TERMS,
                                title = "Kullanım Sözleşmesi"
                            )
                        }
                        
                        annotatedText.getStringAnnotations(
                            tag = "PRIVACY",
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let {
                            TermsActivity.start(
                                context = context,
                                url = Routes.BASE_URL + Routes.PRIVACY,
                                title = "Gizlilik Politikası"
                            )
                        }
                    }
                )
            }
        }
    }
}



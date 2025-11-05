package com.flort.evlilik.modules.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.flort.evlilik.models.user.request.UpdateInfoRequest
import com.flort.evlilik.network.ApiClient
import com.flort.evlilik.network.model.ApiException
import com.flort.evlilik.utils.helpers.ToastHelper
import android.app.Activity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flort.evlilik.R
import com.flort.evlilik.modules.auth.components.Gender
import com.flort.evlilik.models.user.User
import androidx.activity.ComponentActivity

@Composable
fun UpdateProfileScreen(
    onBack: () -> Unit = {},
    onSave: () -> Unit = {},
    initialName: String = "",
    initialAge: String = "",
    initialGender: Gender? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.page_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        val nameState = remember { mutableStateOf(initialName) }
        val ageState = remember { mutableStateOf(initialAge) }
        val genderState = remember { mutableStateOf(initialGender) }
        var isLoading by remember { mutableStateOf(false) }
        
        LaunchedEffect(Unit) {
            val currentUser = User.current
            if (currentUser != null) {
                nameState.value = currentUser.name ?: ""
                ageState.value = currentUser.age?.toString() ?: ""
                genderState.value = when (currentUser.gender) {
                    1 -> Gender.MALE
                    2 -> Gender.FEMALE
                    else -> null
                }
            }
        }
        
        fun updateProfile() {
            if (nameState.value.isBlank()) {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showWarning(activity, "İsim gereklidir")
                }
                return
            }
            
            if (nameState.value.trim().length < 2) {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showWarning(activity, "İsim en az 2 karakter olmalıdır")
                }
                return
            }
            
            val ageInt = ageState.value.toIntOrNull()
            if (ageInt != null && (ageInt < 18 || ageInt > 100)) {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showWarning(activity, "Yaş 18-100 arasında olmalıdır")
                }
                return
            }
            
            isLoading = true
            coroutineScope.launch {
                try {
                    val userService = ApiClient.getInstance(context).userService
                    val request = UpdateInfoRequest(
                        name = nameState.value.trim(),
                        age = ageInt,
                        gender = when (genderState.value) {
                            Gender.MALE -> 1
                            Gender.FEMALE -> 2
                            else -> null
                        }
                    )
                    
                    val response = userService.updateInfo(request)
                    
                    if (response.success) {
                        val currentUser = User.current
                        if (currentUser != null) {
                            currentUser.name = nameState.value.trim()
                            if (ageInt != null) currentUser.age = ageInt
                            when (genderState.value) {
                                Gender.MALE -> currentUser.gender = 1
                                Gender.FEMALE -> currentUser.gender = 2
                                else -> currentUser.gender = null
                            }
                        }
                        
                        (context as? Activity)?.let { activity ->
                            ToastHelper.showSuccess(activity, "Profil başarıyla güncellendi!")
                        }
                        
                        onSave()
                        
                        try {
                            (context as ComponentActivity).finish()
                        } catch (e: Exception) {
                        }
                    } else {
                        (context as? Activity)?.let { activity ->
                            ToastHelper.showError(activity, response.message ?: "Bilinmeyen hata oluştu")
                        }
                    }
                    
                } catch (e: Exception) {
                    val errorMessage = ApiException.getErrorMessage(e)
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showError(activity, errorMessage)
                    }
                } finally {
                    isLoading = false
                }
            }
        }

        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // Başlık
                Text(
                    text = "Profili Güncelle",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "İsmini ve yaşını güncelle, seni daha iyi tanıyalım.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    label = { Text(text = "İsim") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
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
                    value = ageState.value,
                    onValueChange = { ageState.value = it },
                    label = { Text(text = "Yaş") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
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

                Spacer(modifier = Modifier.height(8.dp))

                val ageInt = ageState.value.toIntOrNull()
                val isUnderage = ageInt != null && ageInt < 18
                if (isUnderage) {
                    Text(
                        text = "18 yaşından küçükler kayıt olamaz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD32F2F)
                    )
                } else {
                    Text(
                        text = "En az 18 yaşında olmalısın.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Cinsiyet",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    GenderCard(
                        text = "Erkek",
                        selected = genderState.value == Gender.MALE,
                        onClick = { genderState.value = Gender.MALE },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    GenderCard(
                        text = "Kadın",
                        selected = genderState.value == Gender.FEMALE,
                        onClick = { genderState.value = Gender.FEMALE },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            BottomActions(
                onBack = onBack,
                onSave = { updateProfile() },
                isLoading = isLoading,
                showBackButton = false
            )
        }
    }
}

@Composable
private fun GenderCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) colorResource(id = R.color.primaryColor) else colorResource(id = R.color.whiteButtonStrokeColor)
    val bgColor = if (selected) colorResource(id = R.color.primaryColor).copy(alpha = 0.08f) else Color.White
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun BottomActions(
    onBack: () -> Unit,
    onSave: () -> Unit,
    isLoading: Boolean = false,
    showBackButton: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showBackButton) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onBack,
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colorResource(id = R.color.whiteButtonStrokeColor)),
                    modifier = Modifier.height(60.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.left_arrow),
                            contentDescription = null
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                ) {
                    Button(
                        onClick = onSave,
                        enabled = !isLoading,
                        modifier = Modifier.matchParentSize(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.primaryDarkColor),
                            contentColor = Color.White,
                            disabledContainerColor = colorResource(id = R.color.primaryDarkColor).copy(alpha = 0.6f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Kaydediliyor...")
                        } else {
                            Text(text = "Kaydet")
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.right_arrow_white),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 18.dp)
                    )
                }
            }
        } else {
            Button(
                onClick = onSave,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.primaryDarkColor),
                    contentColor = Color.White,
                    disabledContainerColor = colorResource(id = R.color.primaryDarkColor).copy(alpha = 0.6f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Kaydediliyor...")
                } else {
                    Text(text = "Kaydet")
                }
            }
        }
    }
}
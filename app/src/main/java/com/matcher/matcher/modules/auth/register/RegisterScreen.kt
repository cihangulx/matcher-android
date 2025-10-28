package com.matcher.matcher.modules.auth.register

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.matcher.matcher.R
import com.matcher.matcher.modules.auth.components.Gender
import com.matcher.matcher.modules.auth.components.StepView
import com.matcher.matcher.modules.auth.components.BottomActions
import com.matcher.matcher.models.auth.CheckEmailRequest
import com.matcher.matcher.models.auth.request.RegisterRequest
import com.matcher.matcher.network.ApiClient
import com.matcher.matcher.utils.PreferencesManager
import com.matcher.matcher.network.model.ApiException
import com.matcher.matcher.models.user.User
import com.matcher.matcher.utils.helpers.ToastHelper
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.app.Activity
import com.matcher.matcher.network.service.FileUploadService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RegisterScreen(
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    currentStep: Int = 1,
    totalSteps: Int = 3
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.page_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        val stepState = remember { mutableStateOf(1) }
        val emailState = remember { mutableStateOf("cihangulx@gmail.com") }
        val passwordState = remember { mutableStateOf("112233") }
        val confirmPasswordState = remember { mutableStateOf("112233") }
        val isLoading = remember { mutableStateOf(false) }
        val nameState = remember { mutableStateOf("C") }
        val ageState = remember { mutableStateOf("22") }
        val genderState = remember { mutableStateOf<Gender?>(Gender.MALE) }
        val selectedPhotoSlot = remember { mutableStateOf(0) }
        val selectedImages = remember { mutableStateOf<List<android.net.Uri?>>(listOf(null, null, null)) }
        val total = 4 // 1) E-posta, 2) Şifre, 3) İsim & Yaş, 4) Fotoğraf
        
        fun checkEmailAvailability() {
            if (emailState.value.isBlank()) {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showWarning(activity, "Email adresi gereklidir")
                }
                return
            }
            
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
            if (!emailState.value.trim().matches(emailPattern)) {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showWarning(activity, "Geçerli bir email adresi girin")
                }
                return
            }
            
            coroutineScope.launch {
                isLoading.value = true
                try {
                    val authService = ApiClient.getInstance(context).authService
                    val request = CheckEmailRequest(email = emailState.value.trim())
                    
                    val response = authService.checkEmail(request)
                    
                    if (response.success && response.data != null) {
                        if (response.data.exists) {
                            (context as? Activity)?.let { activity ->
                                ToastHelper.showError(activity, "Bu email adresi zaten kayıtlı")
                            }
                        } else {
                            stepState.value = 2
                        }
                    } else {
                        (context as? Activity)?.let { activity ->
                            ToastHelper.showError(activity, response.message ?: "Email kontrolü yapılamadı")
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
        
        fun validatePasswordStep() {
            if (passwordState.value.isBlank()) {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showWarning(activity, "Şifre gereklidir")
                }
                return
            }
            
            if (passwordState.value.length < 6) {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showWarning(activity, "Şifre en az 6 karakter olmalıdır")
                }
                return
            }
            
            if (confirmPasswordState.value.isBlank()) {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showWarning(activity, "Şifre tekrarı gereklidir")
                }
                return
            }
            
            if (passwordState.value != confirmPasswordState.value) {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showError(activity, "Şifreler eşleşmiyor")
                }
                return
            }
            
            stepState.value = 3
        }
        
        fun validateProfileStep() {
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
            
            if (ageState.value.isBlank()) {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showWarning(activity, "Yaş gereklidir")
                }
                return
            }
            
            val age = ageState.value.toIntOrNull()
            if (age == null || age < 18) {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showWarning(activity, "En az 18 yaşında olmalısınız")
                }
                return
            }
            
            if (genderState.value == null) {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showWarning(activity, "Cinsiyet seçimi gereklidir")
                }
                return
            }
            
            stepState.value = 4
        }
        
        fun performRegister() {
            coroutineScope.launch {
                isLoading.value = true
                try {
                    val uploadedUrls = mutableListOf<String?>()
                    val fileUploadService = FileUploadService(context)
                    
                    for (i in selectedImages.value.indices) {
                        val imageUri = selectedImages.value[i]
                        if (imageUri != null) {
                            val result = fileUploadService.uploadImage(imageUri, "image")
                            
                            result.fold(
                                onSuccess = { imageUrl ->
                                    uploadedUrls.add(imageUrl)
                                },
                                onFailure = { error ->
                                    uploadedUrls.add(null)
                                }
                            )
                        } else {
                            uploadedUrls.add(null)
                        }
                    }
                    
                    val gallery = uploadedUrls.mapIndexedNotNull { index, url ->
                        if (url != null) {
                            com.matcher.matcher.models.auth.GalleryItem(
                                index = index,
                                url = url,
                                isMain = index == 0
                            )
                        } else null
                    }
                    
                    
                    val authService = ApiClient.getInstance(context).authService
                    val request = RegisterRequest(
                        name = nameState.value.trim(),
                        email = emailState.value.trim(),
                        password = passwordState.value,
                        phone = null,
                        age = ageState.value.toIntOrNull(),
                        gender = when (genderState.value) {
                            Gender.MALE -> 1
                            Gender.FEMALE -> 2
                            null -> 0
                        },
                        gallery = gallery.ifEmpty { null }
                    )
                    
                    val response = authService.register(request)
                    
                    if (response.success && response.data != null) {
                        withContext(Dispatchers.IO) {
                            val preferencesManager = PreferencesManager.getInstance(context)
                            preferencesManager.saveAuthToken(response.data.token)
                        }
                        
                        val validateSuccess = withContext(Dispatchers.IO) {
                            User.updateCurrentUser(context)
                        }
                        
                        if (validateSuccess) {
                            (context as? Activity)?.let { activity ->
                                ToastHelper.showSuccess(activity, "Hoş geldiniz!")
                            }
                            
                            onNavigate("main")
                        } else {
                            (context as? Activity)?.let { activity ->
                                ToastHelper.showWarning(activity, "Kayıt başarılı ancak oturum açılamadı. Lütfen giriş yapın.")
                            }
                            onNavigate("account_select")
                        }
                    } else {
                        (context as? Activity)?.let { activity ->
                            ToastHelper.showError(activity, response.message ?: "Kayıt yapılamadı")
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepView(currentStep = stepState.value, totalSteps = total)

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (stepState.value == 1) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RegisterStep1(
                            email = emailState.value,
                            onEmailChange = { emailState.value = it }
                        )
                    }
                } else if (stepState.value == 2) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RegisterStep2(
                            password = passwordState.value,
                            confirmPassword = confirmPasswordState.value,
                            onPasswordChange = { passwordState.value = it },
                            onConfirmPasswordChange = { confirmPasswordState.value = it }
                        )
                    }
                } else if (stepState.value == 3) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RegisterStep3(
                            name = nameState.value,
                            age = ageState.value,
                            onNameChange = { nameState.value = it },
                            onAgeChange = { ageState.value = it },
                            gender = genderState.value,
                            onGenderChange = { genderState.value = it }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RegisterStep4(
                            selectedIndex = selectedPhotoSlot.value,
                            onSlotClick = { selectedPhotoSlot.value = it },
                            selectedImages = selectedImages.value,
                            onImageSelected = { index, uri ->
                                val newImages = selectedImages.value.toMutableList()
                                newImages[index] = uri
                                selectedImages.value = newImages
                            }
                        )
                    }
                }
            }

            BottomActions(
                context = context,
                onBack = {
                    if (stepState.value > 1) {
                        stepState.value = stepState.value - 1
                    } else {
                        onBack()
                    }
                },
                onContinue = {
                    when (stepState.value) {
                        1 -> checkEmailAvailability() // Email kontrolü yap
                        2 -> validatePasswordStep() // Şifre validasyonu
                        3 -> validateProfileStep() // Profil bilgileri validasyonu
                        4 -> {
                            performRegister()
                        }
                    }
                },
                isLoading = isLoading.value,
                continueButtonText = if (stepState.value == 4) "Kaydı Tamamla" else "Devam Et"
            )
        }
    }
}




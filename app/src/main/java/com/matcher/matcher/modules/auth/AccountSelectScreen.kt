package com.matcher.matcher.modules.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.remember
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.core.content.ContextCompat
import com.matcher.matcher.R
import com.matcher.matcher.modules.auth.components.GoogleShimmerButton
import com.matcher.matcher.modules.terms.TermsActivity
import com.matcher.matcher.network.Routes
import com.matcher.matcher.utils.helpers.ToastHelper
import android.app.Activity
import androidx.compose.runtime.rememberCoroutineScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException as GoogleApiException
import com.google.android.gms.tasks.Task
import com.matcher.matcher.models.auth.request.GoogleLoginRequest
import com.matcher.matcher.network.ApiClient
import com.matcher.matcher.utils.PreferencesManager
import com.matcher.matcher.models.user.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AccountSelectScreen(
    onLoginClick: () -> Unit = {}, 
    onRegisterClick: () -> Unit = {}, 
    onTermsClick: () -> Unit = {},
    onGoogleSignInResult: ((Task<GoogleSignInAccount>) -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    
    val apiClient = remember { ApiClient.getInstance(context) }
    val preferencesManager = remember { PreferencesManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    
    androidx.compose.runtime.LaunchedEffect(onGoogleSignInResult) {
        if (onGoogleSignInResult != null) {
        }
    }
    
    fun performGoogleLogin() {
        coroutineScope.launch {
            try {
                val activity = context as? Activity
                if (activity == null) {
                    ToastHelper.showError(context as Activity, "Activity bulunamadı")
                    return@launch
                }
                
                val signInIntent = googleSignInClient.signInIntent
                activity.startActivityForResult(signInIntent, 1001)
            } catch (e: Exception) {
                val errorMessage = "Google giriş başlatma hatası: ${e.message}"
                ToastHelper.showError(context as Activity, errorMessage)
                
                e.printStackTrace()
            }
        }
    }
    
    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        coroutineScope.launch {
            try {
                val account = task.getResult(GoogleApiException::class.java)
                if (account != null) {
                    val email = account.email ?: ""
                    val name = account.displayName ?: ""
                    val idToken = account.idToken
                    
                    if (email.isNotEmpty() && name.isNotEmpty()) {
                        val googleLoginRequest = GoogleLoginRequest(
                            token = idToken ?: "",
                            email = email,
                            name = name,
                            age = null
                        )
                        
                        val response = withContext(Dispatchers.IO) {
                            apiClient.authService.googleLogin(googleLoginRequest)
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
                            
                            ToastHelper.showSuccess(context as Activity, "Google ile giriş başarılı!")
                            onLoginClick()
                        } else {
                            val apiErrorMessage = response.message ?: "Google giriş başarısız"
                            ToastHelper.showError(context as Activity, "API Hatası: $apiErrorMessage")
                            
                        }
                    } else {
                        ToastHelper.showError(context as Activity, "Google hesap bilgileri alınamadı")
                    }
                } else {
                    ToastHelper.showError(context as Activity, "Google giriş iptal edildi")
                }
            } catch (e: GoogleApiException) {
                val errorMessage = when (e.statusCode) {
                    com.google.android.gms.common.ConnectionResult.NETWORK_ERROR -> "İnternet bağlantısı hatası"
                    com.google.android.gms.common.ConnectionResult.SERVICE_DISABLED -> "Google Play Services devre dışı"
                    com.google.android.gms.common.ConnectionResult.SERVICE_INVALID -> "Google Play Services geçersiz"
                    com.google.android.gms.common.ConnectionResult.SERVICE_MISSING -> "Google Play Services bulunamadı"
                    com.google.android.gms.common.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> "Google Play Services güncelleme gerekli"
                    com.google.android.gms.common.ConnectionResult.SIGN_IN_REQUIRED -> "Google hesabı ile giriş gerekli"
                    com.google.android.gms.common.ConnectionResult.TIMEOUT -> "Google giriş zaman aşımı"
                    7 -> "Google hesabı bulunamadı"
                    8 -> "Google hesap seçimi iptal edildi"
                    10 -> "Google hesap seçimi başarısız"
                    else -> "Google giriş hatası (Kod: ${e.statusCode})"
                }
                
                ToastHelper.showError(context as Activity, errorMessage)
                
            } catch (e: Exception) {
                val errorMessage = "Beklenmeyen hata: ${e.message}"
                ToastHelper.showError(context as Activity, errorMessage)
                
                e.printStackTrace()
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.primaryColor)),
        contentAlignment = Alignment.Center
    ) {
        val exoPlayer = remember(context) {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(Uri.parse("android.resource://${context.packageName}/${R.raw.new_welcome}"))
                setMediaItem(mediaItem)
                repeatMode = ExoPlayer.REPEAT_MODE_ALL
                prepare()
                playWhenReady = true
            }
        }

        AndroidView(
            factory = { ctx ->
                val view = PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)
                    setShutterBackgroundColor(ContextCompat.getColor(ctx, R.color.primaryColor))
                    alpha = 0f
                }

                val listener = object : Player.Listener {
                    override fun onRenderedFirstFrame() {
                        view.animate().alpha(1f).setDuration(300).start()
                    }
                }
                exoPlayer.addListener(listener)

                view.setTag(R.id.tag_player_listener, listener)
                view
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.primaryColor).copy(alpha = 0.3f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.2f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.weight(0.8f))
        }

        DisposableEffect(Unit) {
            onDispose {
                val playerViewTag = R.id.tag_player_listener
                exoPlayer.release()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                modifier = Modifier
                    .padding(top = 48.dp, end = 16.dp)
                    .clickable { onLoginClick() },
                color = Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "GİRİŞ YAP",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            val linkColor = Color(0xFF38FCD5)
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color(0xFF151A30),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp)
                        .clickable { onRegisterClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.login_mail_icon),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 16.dp)
                                .size(24.dp)
                        )
                        Text(
                            text = "E-Posta ile kaydol",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                AndroidView(
                    factory = { ctx ->
                        GoogleShimmerButton(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            setOnGoogleClickListener {
                                performGoogleLogin()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp)
                )

                Spacer(modifier = Modifier.size(28.dp))

                ClickableText(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
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
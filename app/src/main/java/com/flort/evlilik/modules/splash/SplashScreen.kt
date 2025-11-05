package com.flort.evlilik.modules.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.flort.evlilik.R
import com.flort.evlilik.models.user.User
import com.flort.evlilik.network.ApiClient
import com.flort.evlilik.utils.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(onNavigate: (String) -> Unit = {}) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager.getInstance(context) }
    val apiClient = remember { ApiClient.getInstance(context) }
    
    LaunchedEffect(Unit) {
        try {
            // Minimum splash süresi
            delay(1500)
            
            // Token kontrolü - IO thread'de yap
            val hasToken = withContext(Dispatchers.IO) {
                preferencesManager.isTokenValid()
            }
            
            if (hasToken) {
                // Token varsa sunucuda doğrula - IO thread'de yap
                val isValidToken = withContext(Dispatchers.IO) {
                    try {
                        val response = apiClient.authService.validateToken()

                        if (response.success && response.data != null) {
                            User.current = response.data
                        }

                        response.success
                    } catch (e: Exception) {
                        // Ağ hatası durumunda token'ı temizle
                        //preferencesManager.clearAuthToken()
                        false
                    }
                }
                
                if (isValidToken) {
                    // Token geçerli, ana sayfaya yönlendir (main thread'de)
                    onNavigate("main")
                } else {
                    // Token geçersiz, giriş sayfasına yönlendir (main thread'de)
                    onNavigate("account_select")
                }
            } else {
                // Token yok, giriş sayfasına yönlendir (main thread'de)
                onNavigate("account_select")
            }
        } catch (e: Exception) {
            // Genel hata durumunda giriş sayfasına yönlendir (main thread'de)
            onNavigate("account_select")
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.primaryColor)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )
    }
}
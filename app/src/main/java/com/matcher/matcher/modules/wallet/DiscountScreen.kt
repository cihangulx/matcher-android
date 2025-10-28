package com.matcher.matcher.modules.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.matcher.matcher.R
import com.matcher.matcher.models.packages.TokenPackage
import com.matcher.matcher.models.user.User
import com.matcher.matcher.network.ApiClient
import com.matcher.matcher.utils.helpers.BillingHelper
import com.matcher.matcher.utils.helpers.BillingPurchaseResult
import com.matcher.matcher.utils.helpers.ToastHelper
import com.matcher.matcher.utils.helpers.TEST_MODE_BILLING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import com.matcher.matcher.models.packages.request.PurchaseTokenRequest

class DiscountActivity : ComponentActivity() {
    
    private lateinit var billingHelper: BillingHelper
    private val TAG = "DiscountActivity"
    
    companion object {
        const val RESULT_PURCHASE_SUCCESS = 1001
        const val RESULT_PURCHASE_CANCELLED = 1002
        
        fun start(context: Context) {
            val intent = Intent(context, DiscountActivity::class.java)
            context.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // BillingHelper'ı başlat
        initBillingHelper()
        
        setContent {
            Surface(color = Color.White) {
                BackHandler {
                    finish()
                }

                DiscountScreen(
                    onPurchase = { pkg -> purchaseTokenPackage(pkg) },
                    onBack = { finish() }
                )
            }
        }
    }
    
    /**
     * BillingHelper'ı başlatır
     */
    private fun initBillingHelper() {
        billingHelper = BillingHelper(this) { result ->
            handlePurchaseResult(result)
        }
        billingHelper.connect()
    }
    
    /**
     * Token paketi satın alma işlemini başlatır
     */
    fun purchaseTokenPackage(tokenPackage: TokenPackage) {
        Log.d(TAG, "İndirimli paket satın alma başlatılıyor: ${tokenPackage.name}")
        
        if (TEST_MODE_BILLING) {
            // Test modu: Google Play'e gitmeden direkt API'ye doğrulama gönder
            Log.d(TAG, "Test modu aktif - API'ye direkt doğrulama gönderiliyor")
            testTokenPurchase(tokenPackage)
        } else {
            // Normal mod: Google Play üzerinden satın alma yap
            Log.d(TAG, "Normal mod - Google Play üzerinden satın alma yapılıyor")
            billingHelper.purchaseTokenPackage(tokenPackage)
        }
    }
    
    /**
     * Test amaçlı token satın alma - Google Play olmadan API'ye istek gönder
     */
    private fun testTokenPurchase(tokenPackage: TokenPackage) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "TEST: İndirimli token satın alma simülasyonu başlatılıyor...")
                
                ToastHelper.showSuccess(this@DiscountActivity, "TEST: İndirimli token ödeme işleniyor...")
                
                val apiClient = ApiClient.getInstance(this@DiscountActivity)
                
                // Test verisi ile API'ye istek gönder
                val response = apiClient.packageService.purchaseToken(
                    PurchaseTokenRequest(
                        sku = tokenPackage.sku ?: "", // TokenPackage'den SKU al
                        paymentMethod = "google",
                        paymentData = mapOf(
                            "purchaseToken" to "TEST_DISCOUNT_TOKEN_${System.currentTimeMillis()}",
                            "productId" to (tokenPackage.sku ?: ""),
                            "orderId" to "TEST_DISCOUNT_ORDER_${System.currentTimeMillis()}",
                            "purchaseTime" to System.currentTimeMillis()
                        ),
                        couponCode = null
                    )
                )
                
                withContext(Dispatchers.Main) {
                    if (response.success) {
                        val responseData = response.data
                        
                        Log.d(TAG, "TEST: İndirimli token satın alma başarılı")
                        val message = "TEST: İndirimli token başarıyla eklendi! Yeni bakiye: ${responseData?.newBalance ?: "N/A"}"
                        ToastHelper.showSuccess(this@DiscountActivity, message)
                        
                        // Kullanıcı bilgilerini güncelle
                        updateUserProfile()
                        
                        // Satın alma başarılı result döndür
                        setResult(RESULT_PURCHASE_SUCCESS)
                        finish()
                    } else {
                        val errorMessage = response.message ?: "TEST: İndirimli token doğrulama hatası"
                        Log.e(TAG, "TEST: İndirimli token Backend doğrulama hatası: $errorMessage")
                        ToastHelper.showError(this@DiscountActivity, errorMessage)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "TEST: İndirimli token Backend doğrulama hatası: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    ToastHelper.showError(this@DiscountActivity, "TEST: Bağlantı hatası: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Satın alma sonucunu işler
     */
    private fun handlePurchaseResult(result: BillingPurchaseResult) {
        when (result) {
            is BillingPurchaseResult.Success -> {
                Log.d(TAG, "İndirimli paket satın alma başarılı: ${result.productId}")
                // Backend'e doğrulama için gönder
                verifyPurchaseWithBackend(result)
            }
            is BillingPurchaseResult.Error -> {
                Log.e(TAG, "İndirimli paket satın alma hatası: ${result.message}")
                ToastHelper.showError(this, result.message)
            }
            is BillingPurchaseResult.Cancelled -> {
                Log.d(TAG, "İndirimli paket satın alma iptal edildi")
                ToastHelper.showInfo(this, "Satın alma iptal edildi")
            }
            is BillingPurchaseResult.Pending -> {
                Log.d(TAG, "İndirimli paket satın alma beklemede")
                ToastHelper.showInfo(this, "Ödeme işlemi devam ediyor...")
            }
        }
    }
    
    /**
     * Backend'e token doğrulama isteği gönderir
     */
    private fun verifyPurchaseWithBackend(result: BillingPurchaseResult.Success) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "İndirimli token Backend doğrulama başlatılıyor...")
                
                ToastHelper.showSuccess(this@DiscountActivity, "İndirimli token ödeme işleniyor...")
                
                val apiClient = ApiClient.getInstance(this@DiscountActivity)
                
                // Token paketi satın alma
                val response = apiClient.packageService.purchaseToken(
                    PurchaseTokenRequest(
                        sku = result.productId, // Google Play productId = SKU
                        paymentMethod = "google",
                        paymentData = mapOf(
                            "purchaseToken" to result.purchaseToken,
                            "productId" to result.productId,
                            "orderId" to result.orderId,
                            "purchaseTime" to result.purchaseTime
                        ),
                        couponCode = null // TODO: Kupon kodu eklenebilir
                    )
                )
                
                withContext(Dispatchers.Main) {
                    if (response.success) {
                        val responseData = response.data
                        
                        // Backend doğruladı - Satın almayı tüketelim (consume)
                        billingHelper.consumePurchase(result.purchase) { consumed ->
                            if (consumed) {
                                Log.d(TAG, "İndirimli token satın alma tüketildi")
                                
                                val message = "İndirimli token başarıyla eklendi! Yeni bakiye: ${responseData?.newBalance ?: "N/A"}"
                                ToastHelper.showSuccess(this@DiscountActivity, message)
                                
                                // Kullanıcı bilgilerini güncelle
                                updateUserProfile()
                                
                                // Satın alma başarılı result döndür
                                setResult(RESULT_PURCHASE_SUCCESS)
                                finish()
                            } else {
                                Log.e(TAG, "İndirimli token satın alma tüketilemedi")
                                ToastHelper.showError(
                                    this@DiscountActivity,
                                    "Tüketme hatası"
                                )
                            }
                        }
                    } else {
                        val errorMessage = response.message ?: "İndirimli token doğrulama hatası"
                        Log.e(TAG, "İndirimli token Backend doğrulama hatası: $errorMessage")
                        ToastHelper.showError(this@DiscountActivity, errorMessage)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "İndirimli token Backend doğrulama hatası: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    ToastHelper.showError(
                        this@DiscountActivity,
                        "Bağlantı hatası: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Kullanıcı profil bilgilerini günceller
     */
    private fun updateUserProfile() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Kullanıcı profil bilgileri güncelleniyor...")
                
                val apiClient = ApiClient.getInstance(this@DiscountActivity)
                val response = apiClient.authService.profile()
                
                if (response.success && response.data != null) {
                    // Kullanıcı bilgilerini güncelle
                    User.updateCurrentUser(this@DiscountActivity)
                    Log.d(TAG, "Kullanıcı profil bilgileri güncellendi")
                } else {
                    Log.e(TAG, "Kullanıcı profil bilgileri güncellenemedi: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Kullanıcı profil güncelleme hatası: ${e.message}", e)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Billing bağlantısını kes
        billingHelper.disconnect()
    }
}

@Composable
fun DiscountScreen(onPurchase: (TokenPackage) -> Unit = {}, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val discountPackages = remember { mutableStateOf<List<TokenPackage>>(emptyList()) }
    val isLoading = remember { mutableStateOf(false) }
    
    // İndirimli paketleri çek
    LaunchedEffect(Unit) {
        isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                val packageService = ApiClient.getInstance(context).packageService
                val response = packageService.getDiscountPackages()
                if (response.success && response.data != null) {
                    discountPackages.value = response.data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        isLoading.value = false
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Üst: Geri butonu
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onBack,
                color = Color(0xFF171A20),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2F39))
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .height(44.dp)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painter = painterResource(id = R.drawable.left_arrow_white), contentDescription = null)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Sana özel indirim yaptık, sakın kaçırma",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            discountPackages.value.forEach { pkg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171A20)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = pkg.name ?: "",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pkg.desc ?: "",
                                color = colorResource(id = R.color.primaryColor),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            // Eski fiyat varsa göster
                            if (pkg.oldPrice != null && pkg.oldPrice > 0 && pkg.oldPrice > (pkg.currentPrice ?: 0.0)) {
                                Text(
                                    text = "₺${String.format("%.2f", pkg.oldPrice)}",
                                    color = Color(0xFF9CA3AF),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.End,
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Button(
                                onClick = { onPurchase(pkg) },
                                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.primaryDarkColor))
                            ) {
                                Text(
                                    text = "₺${String.format("%.2f", pkg.currentPrice ?: 0.0)}",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

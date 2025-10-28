package com.matcher.matcher.modules.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import com.matcher.matcher.models.packages.TokenPackage
import com.matcher.matcher.models.packages.request.PurchaseTokenRequest
import com.matcher.matcher.models.user.User
import com.matcher.matcher.network.ApiClient
import com.matcher.matcher.utils.helpers.BillingHelper
import com.matcher.matcher.utils.helpers.BillingPurchaseResult
import com.matcher.matcher.utils.helpers.ToastHelper
import com.matcher.matcher.utils.helpers.TEST_MODE_BILLING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletActivity : ComponentActivity() {
    
    private lateinit var billingHelper: BillingHelper
    private val TAG = "WalletActivity"
    
    companion object {
        const val RESULT_PURCHASE_SUCCESS = 1001
        const val RESULT_PURCHASE_CANCELLED = 1002
        
        fun start(context: Context) {
            val intent = Intent(context, WalletActivity::class.java)
            context.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // BillingHelper'ı başlat
        initBillingHelper()
        
        setContent {
            Surface(color = Color.White) {
                val showDiscount = remember { mutableStateOf(false) }
                val hasDiscountPackages = remember { mutableStateOf(false) }

                BackHandler {
                    if (!showDiscount.value && hasDiscountPackages.value) {
                        showDiscount.value = true
                    } else {
                        finish()
                    }
                }

                if (showDiscount.value) {
                    // DiscountActivity'yi başlat
                    DiscountActivity.start(this@WalletActivity)
                    finish()
                } else {
                    WalletScreen(
                        onBack = { hasDiscount ->
                            hasDiscountPackages.value = hasDiscount
                            if (hasDiscount) {
                                showDiscount.value = true
                            } else {
                                finish()
                            }
                        },
                        onPurchase = { pkg -> purchaseTokenPackage(pkg) }
                    )
                }
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
        Log.d(TAG, "Satın alma başlatılıyor: ${tokenPackage.name}")
        
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
                Log.d(TAG, "TEST: Token satın alma simülasyonu başlatılıyor...")
                
                ToastHelper.showSuccess(this@WalletActivity, "TEST: Token ödeme işleniyor...")
                
                val apiClient = ApiClient.getInstance(this@WalletActivity)
                
                // Test verisi ile API'ye istek gönder
                val response = apiClient.packageService.purchaseToken(
                    PurchaseTokenRequest(
                        sku = tokenPackage.sku ?: "", // TokenPackage'den SKU al
                        paymentMethod = "google",
                        paymentData = mapOf(
                            "purchaseToken" to "TEST_TOKEN_${System.currentTimeMillis()}",
                            "productId" to (tokenPackage.sku ?: ""),
                            "orderId" to "TEST_ORDER_${System.currentTimeMillis()}",
                            "purchaseTime" to System.currentTimeMillis()
                        ),
                        couponCode = null
                    )
                )
                
                withContext(Dispatchers.Main) {
                    if (response.success) {
                        val responseData = response.data
                        
                        Log.d(TAG, "TEST: Token satın alma başarılı")
                        val message = "TEST: Token başarıyla eklendi! Yeni bakiye: ${responseData?.newBalance ?: "N/A"}"
                        ToastHelper.showSuccess(this@WalletActivity, message)
                        
                        // Kullanıcı bilgilerini güncelle
                        updateUserProfile()
                        
                        // Satın alma başarılı result döndür
                        setResult(RESULT_PURCHASE_SUCCESS)
                        finish()
                    } else {
                        val errorMessage = response.message ?: "TEST: Token doğrulama hatası"
                        Log.e(TAG, "TEST: Token Backend doğrulama hatası: $errorMessage")
                        ToastHelper.showError(this@WalletActivity, errorMessage)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "TEST: Token Backend doğrulama hatası: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    ToastHelper.showError(this@WalletActivity, "TEST: Bağlantı hatası: ${e.message}")
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
                Log.d(TAG, "Satın alma başarılı: ${result.productId}")
                // Backend'e doğrulama için gönder
                verifyPurchaseWithBackend(result)
            }
            is BillingPurchaseResult.Error -> {
                Log.e(TAG, "Satın alma hatası: ${result.message}")
                ToastHelper.showError(this, result.message)
            }
            is BillingPurchaseResult.Cancelled -> {
                Log.d(TAG, "Satın alma iptal edildi")
                ToastHelper.showInfo(this, "Satın alma iptal edildi")
            }
            is BillingPurchaseResult.Pending -> {
                Log.d(TAG, "Satın alma beklemede")
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
                Log.d(TAG, "Token Backend doğrulama başlatılıyor...")
                
                ToastHelper.showSuccess(this@WalletActivity, "Token ödeme işleniyor...")
                
                val apiClient = ApiClient.getInstance(this@WalletActivity)
                
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
                                Log.d(TAG, "Token satın alma tüketildi")
                                
                                val message = "Token başarıyla eklendi! Yeni bakiye: ${responseData?.newBalance ?: "N/A"}"
                                ToastHelper.showSuccess(this@WalletActivity, message)
                                
                                // Kullanıcı bilgilerini güncelle
                                updateUserProfile()
                                
                                // Satın alma başarılı result döndür
                                setResult(RESULT_PURCHASE_SUCCESS)
                                finish()
                            } else {
                                Log.e(TAG, "Token satın alma tüketilemedi")
                                ToastHelper.showError(
                                    this@WalletActivity,
                                    "Tüketme hatası"
                                )
                            }
                        }
                    } else {
                        val errorMessage = response.message ?: "Token doğrulama hatası"
                        Log.e(TAG, "Token Backend doğrulama hatası: $errorMessage")
                        ToastHelper.showError(this@WalletActivity, errorMessage)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Token Backend doğrulama hatası: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    ToastHelper.showError(
                        this@WalletActivity,
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
                
                val apiClient = ApiClient.getInstance(this@WalletActivity)
                val response = apiClient.authService.profile()
                
                if (response.success && response.data != null) {
                    // Kullanıcı bilgilerini güncelle
                    User.updateCurrentUser(this@WalletActivity)
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



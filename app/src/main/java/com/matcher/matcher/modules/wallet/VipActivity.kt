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
import androidx.lifecycle.lifecycleScope
import com.matcher.matcher.models.packages.request.PurchaseVipRequest
import com.matcher.matcher.models.user.User
import com.matcher.matcher.network.ApiClient
import com.matcher.matcher.utils.helpers.BillingHelper
import com.matcher.matcher.utils.helpers.BillingPurchaseResult
import com.matcher.matcher.utils.helpers.ToastHelper
import com.matcher.matcher.utils.helpers.TEST_MODE_BILLING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VipActivity : ComponentActivity() {
    
    private lateinit var billingHelper: BillingHelper
    private val TAG = "VipActivity"
    
    companion object {
        const val RESULT_PURCHASE_SUCCESS = 1001
        const val RESULT_PURCHASE_CANCELLED = 1002
        
        fun start(context: Context) {
            val intent = Intent(context, VipActivity::class.java)
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

                // VipScreenActivity'yi başlat
                VipScreenActivity.start(this@VipActivity)
                finish()
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
     * VIP paketi satın alma işlemini başlatır
     */
    private fun purchaseVipPackage(packageId: String) {
        Log.d(TAG, "VIP paketi satın alma başlatılıyor: $packageId")
        
        if (TEST_MODE_BILLING) {
            // Test modu: Google Play'e gitmeden direkt API'ye doğrulama gönder
            Log.d(TAG, "Test modu aktif - API'ye direkt doğrulama gönderiliyor")
            testVipPurchase(packageId)
        } else {
            // Normal mod: Google Play üzerinden satın alma yap
            Log.d(TAG, "Normal mod - Google Play üzerinden satın alma yapılıyor")
            
            // Package ID'den product ID'yi çıkar
            val productId = getProductIdFromVipPackageId(packageId)
            
            if (productId.isBlank()) {
                ToastHelper.showError(this, "VIP paket bilgisi bulunamadı")
                return
            }
            
            billingHelper.purchaseVipPackage(productId)
        }
    }
    
    /**
     * Test amaçlı VIP satın alma - Google Play olmadan API'ye istek gönder
     */
    private fun testVipPurchase(packageId: String) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "TEST: VIP satın alma simülasyonu başlatılıyor...")
                
                ToastHelper.showSuccess(this@VipActivity, "TEST: VIP ödeme işleniyor...")
                
                val apiClient = ApiClient.getInstance(this@VipActivity)
                
                // Önce VIP paketlerini çek ve packageId'ye göre SKU bul
                val vipPackagesResponse = apiClient.packageService.getVipPackages()
                if (!vipPackagesResponse.success || vipPackagesResponse.data.isNullOrEmpty()) {
                    ToastHelper.showError(this@VipActivity, "VIP paketleri yüklenemedi")
                    return@launch
                }
                
                val selectedPackage = vipPackagesResponse.data.find { it._id == packageId }
                if (selectedPackage == null) {
                    ToastHelper.showError(this@VipActivity, "VIP paketi bulunamadı")
                    return@launch
                }
                
                // Test verisi ile API'ye istek gönder
                val response = apiClient.packageService.purchaseVip(
                    PurchaseVipRequest(
                        sku = selectedPackage.sku ?: "", // Gerçek VIP paket SKU'su
                        paymentMethod = "google",
                        paymentData = mapOf(
                            "purchaseToken" to "TEST_VIP_TOKEN_${System.currentTimeMillis()}",
                            "productId" to (selectedPackage.sku ?: ""),
                            "orderId" to "TEST_VIP_ORDER_${System.currentTimeMillis()}",
                            "purchaseTime" to System.currentTimeMillis()
                        ),
                        couponCode = null
                    )
                )
                
                withContext(Dispatchers.Main) {
                    if (response.success) {
                        val responseData = response.data
                        
                        Log.d(TAG, "TEST: VIP satın alma başarılı")
                        val message = "TEST: VIP paketi başarıyla eklendi!"
                        ToastHelper.showSuccess(this@VipActivity, message)
                        
                        // Kullanıcı bilgilerini güncelle
                        updateUserProfile()
                        
                        // Satın alma başarılı result döndür
                        setResult(RESULT_PURCHASE_SUCCESS)
                        finish()
                    } else {
                        val errorMessage = response.message ?: "TEST: VIP doğrulama hatası"
                        Log.e(TAG, "TEST: VIP Backend doğrulama hatası: $errorMessage")
                        ToastHelper.showError(this@VipActivity, errorMessage)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "TEST: VIP Backend doğrulama hatası: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    ToastHelper.showError(this@VipActivity, "TEST: Bağlantı hatası: ${e.message}")
                }
            }
        }
    }
    
    /**
     * VIP package ID'den product ID'yi çıkarır
     */
    private fun getProductIdFromVipPackageId(packageId: String): String {
        // Bu mapping'i gerçek uygulamada bir service'den almalıyız
        return when (packageId) {
            "65def456..." -> "vip_package_monthly"
            "65def457..." -> "vip_package_yearly"
            "65def458..." -> "vip_package_weekly"
            else -> ""
        }
    }
    
    /**
     * Satın alma sonucunu işler
     */
    private fun handlePurchaseResult(result: BillingPurchaseResult) {
        when (result) {
            is BillingPurchaseResult.Success -> {
                Log.d(TAG, "VIP satın alma başarılı: ${result.productId}")
                // Backend'e doğrulama için gönder
                verifyVipPurchaseWithBackend(result)
            }
            is BillingPurchaseResult.Error -> {
                Log.e(TAG, "VIP satın alma hatası: ${result.message}")
                ToastHelper.showError(this, result.message)
            }
            is BillingPurchaseResult.Cancelled -> {
                Log.d(TAG, "VIP satın alma iptal edildi")
                ToastHelper.showInfo(this, "Satın alma iptal edildi")
            }
            is BillingPurchaseResult.Pending -> {
                Log.d(TAG, "VIP satın alma beklemede")
                ToastHelper.showInfo(this, "Ödeme işlemi devam ediyor...")
            }
        }
    }
    
    /**
     * Backend'e VIP doğrulama isteği gönderir
     */
    private fun verifyVipPurchaseWithBackend(result: BillingPurchaseResult.Success) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "VIP Backend doğrulama başlatılıyor...")
                
                ToastHelper.showSuccess(this@VipActivity, "VIP ödeme işleniyor...")
                
                val apiClient = ApiClient.getInstance(this@VipActivity)
                
                val response = apiClient.packageService.purchaseVip(
                    PurchaseVipRequest(
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
                        
                        // Backend doğruladı - VIP paketi için acknowledge yap (consume değil!)
                        billingHelper.acknowledgePurchase(result.purchase) { acknowledged ->
                            if (acknowledged) {
                                Log.d(TAG, "VIP satın alma onaylandı")
                                
                                val message = "VIP paketi başarıyla eklendi!"
                                ToastHelper.showSuccess(this@VipActivity, message)
                                
                                // Kullanıcı bilgilerini güncelle
                                updateUserProfile()
                                
                                // Satın alma başarılı result döndür
                                setResult(RESULT_PURCHASE_SUCCESS)
                                finish()
                            } else {
                                Log.e(TAG, "VIP satın alma onaylanamadı")
                                ToastHelper.showError(
                                    this@VipActivity,
                                    "Onaylama hatası"
                                )
                            }
                        }
                    } else {
                        val errorMessage = response.message ?: "VIP doğrulama hatası"
                        Log.e(TAG, "VIP Backend doğrulama hatası: $errorMessage")
                        ToastHelper.showError(this@VipActivity, errorMessage)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "VIP Backend doğrulama hatası: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    ToastHelper.showError(
                        this@VipActivity,
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
                
                val apiClient = ApiClient.getInstance(this@VipActivity)
                val response = apiClient.authService.profile()
                
                if (response.success && response.data != null) {
                    // Kullanıcı bilgilerini güncelle
                    User.updateCurrentUser(this@VipActivity)
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


package com.flort.evlilik.utils.helpers

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingResult
import com.flort.evlilik.models.packages.TokenPackage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Google Play Billing Helper
 * 
 * Token paketleri için Google Play üzerinden satın alma işlemlerini yönetir.
 * 
 * Kullanım:
 * ```
 * val billingHelper = BillingHelper(activity) { result ->
 *     when(result) {
 *         is BillingResult.Success -> {
 *             // Satın alma başarılı
 *             ToastHelper.showSuccess(activity, "Satın alma başarılı!")
 *         }
 *         is BillingResult.Error -> {
 *             // Hata oluştu
 *             ToastHelper.showError(activity, result.message)
 *         }
 *         is BillingResult.Cancelled -> {
 *             // Kullanıcı iptal etti
 *         }
 *     }
 * }
 * 
 * // Bağlantıyı başlat
 * billingHelper.connect()
 * 
 * // Token paketi satın al
 * billingHelper.purchaseTokenPackage(tokenPackage)
 * 
 * // Activity destroy olduğunda
 * billingHelper.disconnect()
 * ```
 */

/**
 * Test modu - Google Play Billing'e gitmeden direkt API'ye doğrulama gönder
 * true: Test modu aktif - Google Play'e gitmez, direkt API'ye gönderir
 * false: Normal mod - Google Play üzerinden satın alma yapar
 */
var TEST_MODE_BILLING = false
class BillingHelper(
    private val activity: Activity,
    private val onPurchaseResult: (BillingPurchaseResult) -> Unit
) {
    
    private val TAG = "BillingHelper"
    private var billingClient: BillingClient? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // Satın alma işlemlerini dinle
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            // Satın alma başarılı
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Kullanıcı iptal etti
            Log.d(TAG, "Kullanıcı satın alma işlemini iptal etti")
            onPurchaseResult(BillingPurchaseResult.Cancelled)
        } else {
            // Hata oluştu
            val errorMessage = getBillingErrorMessage(billingResult.responseCode)
            Log.e(TAG, "Billing hatası: $errorMessage (Code: ${billingResult.responseCode})")
            onPurchaseResult(BillingPurchaseResult.Error(errorMessage))
        }
    }
    
    /**
     * Billing Client'ı başlatır ve Google Play'e bağlanır
     */
    fun connect() {
        billingClient = BillingClient.newBuilder(activity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing Client bağlantısı başarılı")
                    // Bekleyen satın almaları kontrol et
                    queryPendingPurchases()
                } else {
                    val errorMessage = getBillingErrorMessage(billingResult.responseCode)
                    Log.e(TAG, "Billing Client bağlantı hatası: $errorMessage")
                    onPurchaseResult(BillingPurchaseResult.Error(errorMessage))
                }
            }
            
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing servisi bağlantısı kesildi. Yeniden bağlanılacak...")
                // Yeniden bağlanmayı dene
            }
        })
    }
    
    /**
     * Token paketi satın alma işlemini başlatır
     */
    fun purchaseTokenPackage(tokenPackage: TokenPackage) {
        val sku = tokenPackage.sku
        
        if (sku.isNullOrBlank()) {
            Log.e(TAG, "Paket SKU'su boş!")
            onPurchaseResult(BillingPurchaseResult.Error("Paket bilgisi eksik"))
            return
        }
        
        if (billingClient?.isReady != true) {
            Log.e(TAG, "Billing Client hazır değil")
            onPurchaseResult(BillingPurchaseResult.Error("Ödeme servisi hazır değil. Lütfen tekrar deneyin."))
            return
        }
        
        scope.launch {
            try {
                // Ürün detaylarını al
                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(sku)
                        .setProductType(BillingClient.ProductType.INAPP) // Tek seferlik satın alma
                        .build()
                )
                
                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()
                
                Log.d(TAG, "🔍 Ürün detayları sorgulanıyor: $sku")
                
                val billingClientInstance = billingClient
                if (billingClientInstance == null) {
                    Log.e(TAG, "Billing Client null!")
                    onPurchaseResult(BillingPurchaseResult.Error("Ödeme servisi bağlantısı kesildi"))
                    return@launch
                }
                
                val (billingResult, productDetailsList) = suspendCancellableCoroutine<Pair<BillingResult, List<ProductDetails>>> { continuation ->
                    billingClientInstance.queryProductDetailsAsync(params) { result, list ->
                        continuation.resume(Pair(result, list ?: emptyList()))
                    }
                }
                
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val productDetails = productDetailsList.firstOrNull()
                    
                    if (productDetails != null) {
                        Log.d(TAG, "✅ Ürün detayları alındı: ${productDetails.productId}")
                        // Satın alma akışını başlat
                        launchPurchaseFlow(productDetails)
                    } else {
                        Log.e(TAG, "❌ Ürün bulunamadı: $sku (productDetailsList boş veya null)")
                        onPurchaseResult(BillingPurchaseResult.Error("Paket bulunamadı"))
                    }
                } else {
                    val errorMessage = getBillingErrorMessage(billingResult.responseCode)
                    Log.e(TAG, "❌ Ürün detayları alınamadı: $errorMessage")
                    onPurchaseResult(BillingPurchaseResult.Error("Ürün bilgileri alınamadı"))
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Satın alma hatası: ${e.message}", e)
                onPurchaseResult(BillingPurchaseResult.Error("Beklenmeyen bir hata oluştu"))
            }
        }
    }
    
    /**
     * VIP paketi satın alma işlemini başlatır
     */
    fun purchaseVipPackage(productId: String) {
        if (productId.isBlank()) {
            Log.e(TAG, "VIP Product ID boş!")
            onPurchaseResult(BillingPurchaseResult.Error("VIP paket bilgisi eksik"))
            return
        }
        
        if (billingClient?.isReady != true) {
            Log.e(TAG, "Billing Client hazır değil")
            onPurchaseResult(BillingPurchaseResult.Error("Ödeme servisi hazır değil. Lütfen tekrar deneyin."))
            return
        }
        
        scope.launch {
            try {
                // VIP ürün detaylarını al
                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS) // Abonelik ürünü
                        .build()
                )
                
                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()
                
                Log.d(TAG, "🔍 VIP ürün detayları sorgulanıyor: $productId")
                
                val billingClientInstance = billingClient
                if (billingClientInstance == null) {
                    Log.e(TAG, "Billing Client null!")
                    onPurchaseResult(BillingPurchaseResult.Error("Ödeme servisi bağlantısı kesildi"))
                    return@launch
                }
                
                val (billingResult, productDetailsList) = suspendCancellableCoroutine<Pair<BillingResult, List<ProductDetails>>> { continuation ->
                    billingClientInstance.queryProductDetailsAsync(params) { result, list ->
                        continuation.resume(Pair(result, list ?: emptyList()))
                    }
                }
                
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val productDetails = productDetailsList.firstOrNull()
                    
                    if (productDetails != null) {
                        Log.d(TAG, "✅ VIP ürün detayları alındı: ${productDetails.productId}")
                        // VIP satın alma akışını başlat
                        launchPurchaseFlow(productDetails)
                    } else {
                        Log.e(TAG, "❌ VIP ürün bulunamadı: $productId (productDetailsList boş veya null)")
                        onPurchaseResult(BillingPurchaseResult.Error("VIP paket bulunamadı"))
                    }
                } else {
                    val errorMessage = getBillingErrorMessage(billingResult.responseCode)
                    Log.e(TAG, "❌ VIP ürün detayları alınamadı: $errorMessage")
                    onPurchaseResult(BillingPurchaseResult.Error("VIP ürün bilgileri alınamadı"))
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "VIP satın alma hatası: ${e.message}", e)
                onPurchaseResult(BillingPurchaseResult.Error("Beklenmeyen bir hata oluştu"))
            }
        }
    }
    
    /**
     * Satın alma akışını başlatır
     */
    private fun launchPurchaseFlow(productDetails: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        
        val billingResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
        
        if (billingResult?.responseCode != BillingClient.BillingResponseCode.OK) {
            val errorMessage = getBillingErrorMessage(billingResult?.responseCode ?: BillingClient.BillingResponseCode.ERROR)
            Log.e(TAG, "Satın alma akışı başlatılamadı: $errorMessage")
            onPurchaseResult(BillingPurchaseResult.Error(errorMessage))
        }
    }
    
    /**
     * Satın alma işlemini handle eder ve backend'e doğrulama için gönderir
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            Log.d(TAG, "Satın alma tamamlandı: ${purchase.products}")
            
            // Satın alma henüz onaylanmamışsa
            if (!purchase.isAcknowledged) {
                // Backend'e doğrulama için gönder
                // Purchase token ve diğer bilgileri backend'e gönder
                onPurchaseResult(
                    BillingPurchaseResult.Success(
                        purchaseToken = purchase.purchaseToken,
                        productId = purchase.products.firstOrNull() ?: "",
                        orderId = purchase.orderId ?: "",
                        purchaseTime = purchase.purchaseTime,
                        purchase = purchase
                    )
                )
            } else {
                Log.d(TAG, "Satın alma zaten onaylanmış")
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Log.d(TAG, "Satın alma beklemede")
            onPurchaseResult(BillingPurchaseResult.Pending)
        }
    }
    
    /**
     * Tüketilebilir ürünü tüketir (consume)
     * Backend doğruladıktan sonra çağrılmalıdır
     */
    fun consumePurchase(purchase: Purchase, onComplete: (Boolean) -> Unit) {
        scope.launch {
            try {
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                val result = withContext(Dispatchers.IO) {
                    billingClient?.consumePurchase(consumeParams)
                }
                
                if (result?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Satın alma tüketildi")
                    onComplete(true)
                } else {
                    Log.e(TAG, "Satın alma tüketilemedi: ${result?.billingResult?.responseCode}")
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Satın alma tüketme hatası: ${e.message}", e)
                onComplete(false)
            }
        }
    }
    
    /**
     * Satın almayı onaylar (acknowledge) - Abonelik ürünleri için
     * Backend doğruladıktan sonra çağrılmalıdır
     */
    fun acknowledgePurchase(purchase: Purchase, onComplete: (Boolean) -> Unit) {
        if (purchase.isAcknowledged) {
            onComplete(true)
            return
        }
        
        scope.launch {
            try {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                val result = withContext(Dispatchers.IO) {
                    billingClient?.acknowledgePurchase(acknowledgePurchaseParams)
                }
                
                if (result?.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Satın alma onaylandı")
                    onComplete(true)
                } else {
                    Log.e(TAG, "Satın alma onaylanamadı: ${result?.responseCode}")
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Satın alma onaylama hatası: ${e.message}", e)
                onComplete(false)
            }
        }
    }
    
    /**
     * Bekleyen satın almaları kontrol eder
     */
    private fun queryPendingPurchases() {
        scope.launch {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            
            val purchasesResult = withContext(Dispatchers.IO) {
                billingClient?.queryPurchasesAsync(params)
            }
            
            purchasesResult?.purchasesList?.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                    Log.d(TAG, "Bekleyen satın alma bulundu: ${purchase.products}")
                    handlePurchase(purchase)
                }
            }
        }
    }
    
    /**
     * Billing bağlantısını keser
     */
    fun disconnect() {
        billingClient?.endConnection()
        billingClient = null
        Log.d(TAG, "Billing Client bağlantısı kesildi")
    }
    
    /**
     * Billing hata kodlarını kullanıcı dostu mesajlara çevirir
     */
    private fun getBillingErrorMessage(responseCode: Int): String {
        return when (responseCode) {
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> 
                "Zaman aşımı. Lütfen tekrar deneyin."
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> 
                "Bu özellik desteklenmiyor."
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> 
                "Bağlantı kesildi. Lütfen tekrar deneyin."
            BillingClient.BillingResponseCode.USER_CANCELED -> 
                "İşlem iptal edildi."
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> 
                "Servis kullanılamıyor. İnternet bağlantınızı kontrol edin."
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> 
                "Ödeme sistemi kullanılamıyor."
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> 
                "Bu ürün mevcut değil."
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> 
                "Geliştirici hatası. Lütfen destek ile iletişime geçin."
            BillingClient.BillingResponseCode.ERROR -> 
                "Bir hata oluştu. Lütfen tekrar deneyin."
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> 
                "Bu ürün zaten satın alınmış."
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> 
                "Bu ürün size ait değil."
            else -> "Bilinmeyen hata: $responseCode"
        }
    }
}

/**
 * Satın alma sonuç tipleri
 */
sealed class BillingPurchaseResult {
    data class Success(
        val purchaseToken: String,
        val productId: String,
        val orderId: String,
        val purchaseTime: Long,
        val purchase: Purchase
    ) : BillingPurchaseResult()
    
    data class Error(val message: String) : BillingPurchaseResult()
    object Cancelled : BillingPurchaseResult()
    object Pending : BillingPurchaseResult()
}


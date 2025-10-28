# Ödeme Sistemi Entegrasyonu

Matcher Android uygulamasının Google Play Billing entegrasyonu ve ödeme sistemi.

> **📱 Toast Sistemi**: Ödeme işlemlerinde kullanıcıya bildirim göstermek için [Toast Sistemi](../TOAST_USAGE_EXAMPLES.md) sayfasına bakın.

## 🏗️ Ödeme Sistemi Mimarisi

### Genel Yapı
```
┌─────────────────────────────────────────────────────────────┐
│                    UI Layer                                 │
├─────────────────────────────────────────────────────────────┤
│  WalletScreen  │  VipScreen  │  DiscountScreen             │
├─────────────────────────────────────────────────────────────┤
│                    Business Layer                           │
├─────────────────────────────────────────────────────────────┤
│  BillingHelper  │  Test Mode  │  Purchase Validation       │
├─────────────────────────────────────────────────────────────┤
│                    Google Play Billing                      │
├─────────────────────────────────────────────────────────────┤
│  Purchase Flow  │  Product Details  │  Purchase Validation  │
└─────────────────────────────────────────────────────────────┘
```

## 🔧 BillingHelper Sınıfı

### Singleton Pattern
```kotlin
class BillingHelper(
    private val activity: Activity,
    private val onPurchaseResult: (BillingPurchaseResult) -> Unit
) {
    private val TAG = "BillingHelper"
    private var billingClient: BillingClient? = null
    private val scope = CoroutineScope(Dispatchers.Main)
}
```

### Test Modu
```kotlin
/**
 * Test modu - Google Play Billing'e gitmeden direkt API'ye doğrulama gönder
 * true: Test modu aktif - Google Play'e gitmez, direkt API'ye gönderir
 * false: Normal mod - Google Play üzerinden satın alma yapar
 */
var TEST_MODE_BILLING = true
```

## 📦 Paket Tipleri

### TokenPackage Modeli
```kotlin
data class TokenPackage(
    val _id: String? = null,
    val name: String? = null,
    val desc: String? = null,
    val active: Boolean? = null,
    val currentPrice: Double? = null,
    val oldPrice: Double? = null,
    val sku: String? = null,
    val tokenAmount: Int? = null, // main, discount, coupon için: jeton miktarı | vip için: gün sayısı
    val type: String? = null, // main, discount, coupon, vip
    val createdAt: String? = null
)
```

### Paket Tipleri
- **main**: Ana token paketleri
- **discount**: İndirimli paketler
- **coupon**: Kupon ile satın alınan paketler
- **vip**: VIP abonelik paketleri

## 🔄 Satın Alma Akışı

### 1. Normal Mod (Google Play Billing)

#### Bağlantı Kurma
```kotlin
fun connect() {
    billingClient = BillingClient.newBuilder(activity)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()
    
    billingClient?.startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Billing Client bağlantısı başarılı")
                queryPendingPurchases()
            } else {
                val errorMessage = getBillingErrorMessage(billingResult.responseCode)
                onPurchaseResult(BillingPurchaseResult.Error(errorMessage))
            }
        }
        
        override fun onBillingServiceDisconnected() {
            Log.w(TAG, "Billing servisi bağlantısı kesildi. Yeniden bağlanılacak...")
        }
    })
}
```

#### Token Paketi Satın Alma
```kotlin
fun purchaseTokenPackage(tokenPackage: TokenPackage) {
    val sku = tokenPackage.sku
    
    if (sku.isNullOrBlank()) {
        onPurchaseResult(BillingPurchaseResult.Error("Paket bilgisi eksik"))
        return
    }
    
    if (billingClient?.isReady != true) {
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
            
            val productDetailsResult = withContext(Dispatchers.IO) {
                billingClient?.queryProductDetails(params)
            }
            
            if (productDetailsResult?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetails = productDetailsResult.productDetailsList?.firstOrNull()
                
                if (productDetails != null) {
                    launchPurchaseFlow(productDetails)
                } else {
                    onPurchaseResult(BillingPurchaseResult.Error("Paket bulunamadı"))
                }
            } else {
                onPurchaseResult(BillingPurchaseResult.Error("Ürün bilgileri alınamadı"))
            }
            
        } catch (e: Exception) {
            onPurchaseResult(BillingPurchaseResult.Error("Beklenmeyen bir hata oluştu"))
        }
    }
}
```

#### VIP Paketi Satın Alma
```kotlin
fun purchaseVipPackage(productId: String) {
    if (productId.isBlank()) {
        onPurchaseResult(BillingPurchaseResult.Error("VIP paket bilgisi eksik"))
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
            
            val productDetailsResult = withContext(Dispatchers.IO) {
                billingClient?.queryProductDetails(params)
            }
            
            if (productDetailsResult?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetails = productDetailsResult.productDetailsList?.firstOrNull()
                
                if (productDetails != null) {
                    launchPurchaseFlow(productDetails)
                } else {
                    onPurchaseResult(BillingPurchaseResult.Error("VIP paket bulunamadı"))
                }
            } else {
                onPurchaseResult(BillingPurchaseResult.Error("VIP ürün bilgileri alınamadı"))
            }
            
        } catch (e: Exception) {
            onPurchaseResult(BillingPurchaseResult.Error("Beklenmeyen bir hata oluştu"))
        }
    }
}
```

### 2. Test Modu

#### Test Modu Aktivasyonu
```kotlin
// BillingHelper.kt dosyasında
var TEST_MODE_BILLING = true // Test modu aktif
```

#### Test Token Satın Alma
```kotlin
private fun testTokenPurchase(tokenPackage: TokenPackage) {
    lifecycleScope.launch {
        try {
            Log.d(TAG, "TEST: Token satın alma simülasyonu başlatılıyor...")
            
            ToastHelper.showSuccess(activity, "TEST: Token ödeme işleniyor...")
            
            val apiClient = ApiClient.getInstance(activity)
            
            // Test verisi ile API'ye istek gönder
            val response = apiClient.packageService.purchaseToken(
                PurchaseTokenRequest(
                    sku = tokenPackage.sku ?: "",
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
                    ToastHelper.showSuccess(activity, "TEST: Token satın alma başarılı!")
                    // UI'yi güncelle
                } else {
                    ToastHelper.showError(activity, "TEST: Token satın alma başarısız: ${response.message}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                ToastHelper.showError(activity, "TEST: Hata oluştu: ${e.message}")
            }
        }
    }
}
```

#### Test VIP Satın Alma
```kotlin
private fun testVipPurchase(packageId: String) {
    lifecycleScope.launch {
        try {
            Log.d(TAG, "TEST: VIP satın alma simülasyonu başlatılıyor...")
            
            ToastHelper.showSuccess(activity, "TEST: VIP ödeme işleniyor...")
            
            val apiClient = ApiClient.getInstance(activity)
            
            // Önce VIP paketlerini çek ve packageId'ye göre SKU bul
            val vipPackagesResponse = apiClient.packageService.getVipPackages()
            if (!vipPackagesResponse.success || vipPackagesResponse.data.isNullOrEmpty()) {
                ToastHelper.showError(activity, "VIP paketleri yüklenemedi")
                return@launch
            }
            
            val selectedPackage = vipPackagesResponse.data.find { it._id == packageId }
            if (selectedPackage == null) {
                ToastHelper.showError(activity, "VIP paketi bulunamadı")
                return@launch
            }
            
            // Test verisi ile API'ye istek gönder
            val response = apiClient.packageService.purchaseVip(
                PurchaseVipRequest(
                    sku = selectedPackage.sku ?: "",
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
                    ToastHelper.showSuccess(activity, "TEST: VIP satın alma başarılı!")
                    // UI'yi güncelle
                } else {
                    ToastHelper.showError(activity, "TEST: VIP satın alma başarısız: ${response.message}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                ToastHelper.showError(activity, "TEST: Hata oluştu: ${e.message}")
            }
        }
    }
}
```

## 📱 UI Entegrasyonu

### WalletActivity
```kotlin
class WalletActivity : ComponentActivity() {
    private lateinit var billingHelper: BillingHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBillingHelper()
    }
    
    private fun initBillingHelper() {
        billingHelper = BillingHelper(this) { result ->
            handlePurchaseResult(result)
        }
        billingHelper.connect()
    }
    
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
    
    private fun handlePurchaseResult(result: BillingPurchaseResult) {
        when (result) {
            is BillingPurchaseResult.Success -> {
                // Backend'e doğrulama için gönder
                validatePurchaseWithBackend(result)
            }
            is BillingPurchaseResult.Error -> {
                ToastHelper.showError(this, result.message)
            }
            is BillingPurchaseResult.Cancelled -> {
                ToastHelper.showInfo(this, "Satın alma iptal edildi")
            }
            is BillingPurchaseResult.Pending -> {
                ToastHelper.showInfo(this, "Satın alma beklemede")
            }
        }
    }
}
```

### VipScreen
```kotlin
@Composable
fun VipScreen(
    vipPackages: List<TokenPackage>,
    onPurchaseVip: (String) -> Unit
) {
    LazyColumn {
        items(vipPackages) { package ->
            VipPackageItem(
                package = package,
                onPurchaseClick = { onPurchaseVip(package._id ?: "") }
            )
        }
    }
}

@Composable
fun VipPackageItem(
    package: TokenPackage,
    onPurchaseClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = package.name ?: "",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Text(
                text = package.desc ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "${package.tokenAmount} gün",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "${package.currentPrice} TL",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Button(
                onClick = onPurchaseClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Satın Al")
            }
        }
    }
}
```

## 🔄 Satın Alma Sonuçları

### BillingPurchaseResult
```kotlin
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
```

### Backend Doğrulama
```kotlin
private fun validatePurchaseWithBackend(result: BillingPurchaseResult.Success) {
    lifecycleScope.launch {
        try {
            val apiClient = ApiClient.getInstance(this@WalletActivity)
            
            val response = apiClient.packageService.purchaseToken(
                PurchaseTokenRequest(
                    sku = result.productId,
                    paymentMethod = "google",
                    paymentData = mapOf(
                        "purchaseToken" to result.purchaseToken,
                        "productId" to result.productId,
                        "orderId" to result.orderId,
                        "purchaseTime" to result.purchaseTime
                    ),
                    couponCode = null
                )
            )
            
            if (response.success) {
                ToastHelper.showSuccess(this@WalletActivity, "Satın alma başarılı!")
                // UI'yi güncelle
                loadWalletData()
                
                // Satın almayı tüket (consume)
                billingHelper.consumePurchase(result.purchase) { success ->
                    if (success) {
                        Log.d(TAG, "Satın alma tüketildi")
                    } else {
                        Log.e(TAG, "Satın alma tüketilemedi")
                    }
                }
            } else {
                ToastHelper.showError(this@WalletActivity, "Satın alma doğrulanamadı: ${response.message}")
            }
        } catch (e: Exception) {
            ToastHelper.showError(this@WalletActivity, "Hata oluştu: ${e.message}")
        }
    }
}
```

## 🚨 Hata Yönetimi

### Billing Hata Kodları
```kotlin
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
```

### Satın Alma Hata Yönetimi
```kotlin
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
```

## 🔧 Ürün Yönetimi

### Tüketilebilir Ürünler (Token Paketleri)
```kotlin
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
```

### Abonelik Ürünleri (VIP Paketleri)
```kotlin
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
```

## 📊 Bekleyen Satın Almalar

### Bekleyen Satın Almaları Kontrol Etme
```kotlin
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
```

## 🔄 Bağlantı Yönetimi

### Bağlantı Kesme
```kotlin
fun disconnect() {
    billingClient?.endConnection()
    billingClient = null
    Log.d(TAG, "Billing Client bağlantısı kesildi")
}
```

### Activity Lifecycle
```kotlin
override fun onDestroy() {
    super.onDestroy()
    billingHelper.disconnect()
}
```

## 🧪 Test Modu Kullanımı

### Test Modunu Aktif Etme
```kotlin
// BillingHelper.kt dosyasında
var TEST_MODE_BILLING = true // Test modu aktif
```

### Test Modunu Deaktif Etme
```kotlin
// BillingHelper.kt dosyasında
var TEST_MODE_BILLING = false // Normal mod aktif
```

### Test Modu Avantajları
- Google Play Console'da ürün tanımlamaya gerek yok
- Hızlı test ve geliştirme
- API entegrasyonunu test etme
- Gerçek para harcamadan test yapma

## 📱 Kullanım Örnekleri

### Token Paketi Satın Alma
```kotlin
val tokenPackage = TokenPackage(
    _id = "package_id",
    name = "100 Token Paketi",
    desc = "100 token içeren paket",
    currentPrice = 9.99,
    sku = "token_100",
    tokenAmount = 100,
    type = "main"
)

// Test modu aktifse
if (TEST_MODE_BILLING) {
    testTokenPurchase(tokenPackage)
} else {
    billingHelper.purchaseTokenPackage(tokenPackage)
}
```

### VIP Paketi Satın Alma
```kotlin
val vipPackage = TokenPackage(
    _id = "vip_package_id",
    name = "1 Aylık VIP",
    desc = "1 aylık VIP üyelik",
    currentPrice = 29.99,
    sku = "vip_monthly",
    tokenAmount = 30,
    type = "vip"
)

// Test modu aktifse
if (TEST_MODE_BILLING) {
    testVipPurchase(vipPackage._id ?: "")
} else {
    billingHelper.purchaseVipPackage(vipPackage.sku ?: "")
}
```

Bu ödeme sistemi entegrasyonu, hem test hem de production ortamında güvenli ve kullanıcı dostu bir satın alma deneyimi sağlar.

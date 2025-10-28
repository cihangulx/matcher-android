package com.matcher.matcher.modules.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.matcher.matcher.R
import com.matcher.matcher.models.user.User
import com.matcher.matcher.models.packages.TokenPackage
import com.matcher.matcher.network.ApiClient
import com.matcher.matcher.models.packages.CouponInfo
import com.matcher.matcher.utils.helpers.BillingHelper
import com.matcher.matcher.utils.helpers.BillingPurchaseResult
import com.matcher.matcher.utils.helpers.ToastHelper
import com.matcher.matcher.utils.helpers.TEST_MODE_BILLING
import com.matcher.matcher.network.model.ApiException
import android.app.Activity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import com.matcher.matcher.models.packages.request.CouponRequest
import com.matcher.matcher.models.packages.request.PurchaseVipRequest

class VipScreenActivity : ComponentActivity() {
    
    private lateinit var billingHelper: BillingHelper
    private val TAG = "VipScreenActivity"
    
    companion object {
        const val RESULT_PURCHASE_SUCCESS = 1001
        const val RESULT_PURCHASE_CANCELLED = 1002
        
        fun start(context: Context) {
            val intent = Intent(context, VipScreenActivity::class.java)
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

                VipScreen(
                    onBack = { finish() },
                    onPurchase = { packageId ->
                        purchaseVipPackage(packageId)
                    }
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
     * VIP paketi satın alma işlemini başlatır
     */
    private fun purchaseVipPackage(packageId: String) {
        Log.d(TAG, "İndirimli VIP paketi satın alma başlatılıyor: $packageId")
        
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
                ToastHelper.showError(this, "İndirimli VIP paket bilgisi bulunamadı")
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
                Log.d(TAG, "TEST: İndirimli VIP satın alma simülasyonu başlatılıyor...")
                
                ToastHelper.showSuccess(this@VipScreenActivity, "TEST: İndirimli VIP ödeme işleniyor...")
                
                val apiClient = ApiClient.getInstance(this@VipScreenActivity)
                
                // Önce VIP paketlerini çek ve packageId'ye göre SKU bul
                val vipPackagesResponse = apiClient.packageService.getVipPackages()
                if (!vipPackagesResponse.success || vipPackagesResponse.data.isNullOrEmpty()) {
                    ToastHelper.showError(this@VipScreenActivity, "İndirimli VIP paketleri yüklenemedi")
                    return@launch
                }
                
                val selectedPackage = vipPackagesResponse.data.find { it._id == packageId }
                if (selectedPackage == null) {
                    ToastHelper.showError(this@VipScreenActivity, "İndirimli VIP paketi bulunamadı")
                    return@launch
                }
                
                // Test verisi ile API'ye istek gönder
                val response = apiClient.packageService.purchaseVip(
                    PurchaseVipRequest(
                        sku = selectedPackage.sku ?: "", // Gerçek VIP paket SKU'su
                        paymentMethod = "google",
                        paymentData = mapOf(
                            "purchaseToken" to "TEST_DISCOUNT_VIP_TOKEN_${System.currentTimeMillis()}",
                            "productId" to (selectedPackage.sku ?: ""),
                            "orderId" to "TEST_DISCOUNT_VIP_ORDER_${System.currentTimeMillis()}",
                            "purchaseTime" to System.currentTimeMillis()
                        ),
                        couponCode = null
                    )
                )
                
                withContext(Dispatchers.Main) {
                    if (response.success) {
                        val responseData = response.data
                        
                        Log.d(TAG, "TEST: İndirimli VIP satın alma başarılı")
                        val message = "TEST: İndirimli VIP paketi başarıyla eklendi!"
                        ToastHelper.showSuccess(this@VipScreenActivity, message)
                        
                        // Kullanıcı bilgilerini güncelle
                        updateUserProfile()
                        
                        // Satın alma başarılı result döndür
                        setResult(RESULT_PURCHASE_SUCCESS)
                        finish()
                    } else {
                        val errorMessage = response.message ?: "TEST: İndirimli VIP doğrulama hatası"
                        Log.e(TAG, "TEST: İndirimli VIP Backend doğrulama hatası: $errorMessage")
                        ToastHelper.showError(this@VipScreenActivity, errorMessage)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "TEST: İndirimli VIP Backend doğrulama hatası: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    ToastHelper.showError(this@VipScreenActivity, "TEST: Bağlantı hatası: ${e.message}")
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
            "65def456..." -> "discount_vip_package_monthly"
            "65def457..." -> "discount_vip_package_yearly"
            "65def458..." -> "discount_vip_package_weekly"
            else -> ""
        }
    }
    
    /**
     * Satın alma sonucunu işler
     */
    private fun handlePurchaseResult(result: BillingPurchaseResult) {
        when (result) {
            is BillingPurchaseResult.Success -> {
                Log.d(TAG, "İndirimli VIP satın alma başarılı: ${result.productId}")
                // Backend'e doğrulama için gönder
                verifyVipPurchaseWithBackend(result)
            }
            is BillingPurchaseResult.Error -> {
                Log.e(TAG, "İndirimli VIP satın alma hatası: ${result.message}")
                ToastHelper.showError(this, result.message)
            }
            is BillingPurchaseResult.Cancelled -> {
                Log.d(TAG, "İndirimli VIP satın alma iptal edildi")
                ToastHelper.showInfo(this, "Satın alma iptal edildi")
            }
            is BillingPurchaseResult.Pending -> {
                Log.d(TAG, "İndirimli VIP satın alma beklemede")
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
                Log.d(TAG, "İndirimli VIP Backend doğrulama başlatılıyor...")
                
                ToastHelper.showSuccess(this@VipScreenActivity, "İndirimli VIP ödeme işleniyor...")
                
                val apiClient = ApiClient.getInstance(this@VipScreenActivity)
                
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
                                Log.d(TAG, "İndirimli VIP satın alma onaylandı")
                                
                                val message = "İndirimli VIP paketi başarıyla eklendi!"
                                ToastHelper.showSuccess(this@VipScreenActivity, message)
                                
                                // Kullanıcı bilgilerini güncelle
                                updateUserProfile()
                                
                                // Satın alma başarılı result döndür
                                setResult(RESULT_PURCHASE_SUCCESS)
                                finish()
                            } else {
                                Log.e(TAG, "İndirimli VIP satın alma onaylanamadı")
                                ToastHelper.showError(
                                    this@VipScreenActivity,
                                    "Onaylama hatası"
                                )
                            }
                        }
                    } else {
                        val errorMessage = response.message ?: "İndirimli VIP doğrulama hatası"
                        Log.e(TAG, "İndirimli VIP Backend doğrulama hatası: $errorMessage")
                        ToastHelper.showError(this@VipScreenActivity, errorMessage)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "İndirimli VIP Backend doğrulama hatası: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    ToastHelper.showError(
                        this@VipScreenActivity,
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
                
                val apiClient = ApiClient.getInstance(this@VipScreenActivity)
                val response = apiClient.authService.profile()
                
                if (response.success && response.data != null) {
                    // Kullanıcı bilgilerini güncelle
                    User.updateCurrentUser(this@VipScreenActivity)
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
fun VipScreen(
    onBack: () -> Unit = {},
    onPurchase: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val code = remember { mutableStateOf("") }
    val tokenBalance = remember { mutableStateOf(User.current?.getBalance() ?: 0) }
    val vipPackages = remember { mutableStateOf<List<TokenPackage>>(emptyList()) }
    val couponInfo = remember { mutableStateOf<CouponInfo?>(null) }
    val isLoading = remember { mutableStateOf(false) }
    val isCouponApplied = remember { mutableStateOf(false) }
    
    // VIP paketlerini yükle
    LaunchedEffect(Unit) {
        isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                // Kullanıcı bilgilerini güncelle
                User.updateCurrentUser(context)
                
                val packageService = ApiClient.getInstance(context).packageService
                
                // VIP paket listesini çek
                val vipResponse = packageService.getVipPackages()
                if (vipResponse.success && vipResponse.data != null) {
                    vipPackages.value = vipResponse.data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        tokenBalance.value = User.current?.getBalance() ?: 0
        isLoading.value = false
    }
    
    // Kupon kodu uygulama fonksiyonu
    fun applyCouponCode() {
        if (code.value.isBlank()) {
            (context as? Activity)?.let { activity ->
                ToastHelper.showWarning(activity, "Lütfen bir kupon kodu girin")
            }
            return
        }
        
        scope.launch {
            isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val packageService = ApiClient.getInstance(context).packageService
                    val response = packageService.applyCoupon(CouponRequest(code.value))
                    
                    withContext(Dispatchers.Main) {
                        if (response.success && response.data != null) {
                            // Kuponlu paketleri göster - sadece VIP paketleri filtrele
                            val vipPackagesOnly = response.data.packages.filter { it.type == "vip" }
                            
                            if (vipPackagesOnly.isNotEmpty()) {
                                vipPackages.value = vipPackagesOnly
                                couponInfo.value = response.data.coupon
                                isCouponApplied.value = true
                                (context as? Activity)?.let { activity ->
                                    ToastHelper.showSuccess(
                                        activity,
                                        "Kupon başarıyla uygulandı: ${response.data.coupon.name}"
                                    )
                                }
                            } else {
                                (context as? Activity)?.let { activity ->
                                    ToastHelper.showError(
                                        activity,
                                        "Bu kupon VIP paketler için geçerli değil"
                                    )
                                }
                            }
                        } else {
                            (context as? Activity)?.let { activity ->
                                ToastHelper.showError(
                                    activity,
                                    response.message ?: "Geçersiz kupon kodu"
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        val errorMessage = ApiException.getErrorMessage(e)
                        (context as? Activity)?.let { activity ->
                            ToastHelper.showError(activity, errorMessage)
                        }
                    }
                    e.printStackTrace()
                }
            }
            isLoading.value = false
        }
    }
    
    // Kupon kaldırma fonksiyonu
    fun removeCoupon() {
        scope.launch {
            isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val packageService = ApiClient.getInstance(context).packageService
                    
                    // VIP paket listesini tekrar çek
                    val vipResponse = packageService.getVipPackages()
                    if (vipResponse.success && vipResponse.data != null) {
                        vipPackages.value = vipResponse.data
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Kupon bilgilerini temizle
            couponInfo.value = null
            isCouponApplied.value = false
            code.value = ""
            isLoading.value = false
            
            (context as? Activity)?.let { activity ->
                ToastHelper.showSuccess(activity, "Kupon kaldırıldı")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp)) {
        // Üst: Geri butonu + İndirim kodu alanı + Uygula
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(64.dp)
        ) {
            Surface(
                onClick = onBack,
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorResource(id = R.color.whiteButtonStrokeColor)),
                modifier = Modifier.height(56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painter = painterResource(id = R.drawable.left_arrow), contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedTextField(
                value = code.value,
                onValueChange = { code.value = it },
                label = { Text(text = stringResource(id = R.string.discount_code)) },
                singleLine = true,
                modifier = Modifier.weight(1f).height(56.dp),
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

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { applyCouponCode() },
                modifier = Modifier.height(56.dp),
                enabled = !isLoading.value,
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.primaryDarkColor))
            ) {
                Text(text = stringResource(id = R.string.apply), color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cüzdan kartı
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.wallet), contentDescription = null, tint = Color.Unspecified)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.wallet_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = stringResource(id = R.string.token_balance, tokenBalance.value),
                        style = MaterialTheme.typography.titleMedium,
                        color = colorResource(id = R.color.primaryColor)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(id = R.string.wallet_description), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6F6F6F))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Kupon bilgisi varsa göster
        if (isCouponApplied.value && couponInfo.value != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.primaryColor).copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.wallet),
                            contentDescription = null,
                            tint = colorResource(id = R.color.primaryColor),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = couponInfo.value?.name ?: "",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.primaryColor)
                            )
                            if (couponInfo.value?.desc?.isNotEmpty() == true) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = couponInfo.value?.desc ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6F6F6F)
                                )
                            }
                        }
                    }
                    IconButton(onClick = { removeCoupon() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.delete_icon),
                            contentDescription = "Kuponu Kaldır",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Başlık
        if (!isCouponApplied.value) {
            Text(
                text = "VIP Paketler",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "Kupon Avantajları",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Alt başlık
        Text(
            text = "Özel ayrıcalıklar",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6F6F6F),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // VIP paketleri listesi
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            vipPackages.value.forEach { pkg ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pkg.name ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pkg.desc ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorResource(id = R.color.primaryColor),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${pkg.tokenAmount ?: 0} Gün",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6F6F6F)
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
                                onClick = { onPurchase(pkg._id ?: "") },
                                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.primaryDarkColor))
                            ) {
                                Text(
                                    text = "₺${String.format("%.2f", pkg.currentPrice ?: 0.0)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


package com.matcher.matcher.modules.wallet

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.matcher.matcher.utils.helpers.ToastHelper
import com.matcher.matcher.network.model.ApiException
import android.app.Activity
import com.matcher.matcher.models.packages.request.CouponRequest

@Composable
fun WalletScreen(
    onBack: (hasDiscount: Boolean) -> Unit = {},
    onPurchase: (TokenPackage) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val code = remember { mutableStateOf("") }
    val tokenBalance = remember { mutableStateOf(User.current?.getBalance() ?: 0) }
    val packages = remember { mutableStateOf<List<TokenPackage>>(emptyList()) }
    val discountPackages = remember { mutableStateOf<List<TokenPackage>>(emptyList()) }
    val couponInfo = remember { mutableStateOf<CouponInfo?>(null) }
    val isLoading = remember { mutableStateOf(false) }
    val isCouponApplied = remember { mutableStateOf(false) }
    
    // Kullanıcı bilgilerini ve paketleri güncelle
    LaunchedEffect(Unit) {
        isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                // Kullanıcı bilgilerini güncelle
                User.updateCurrentUser(context)
                
                val packageService = ApiClient.getInstance(context).packageService
                
                // Ana paket listesini çek
                val mainResponse = packageService.getMainPackages()
                if (mainResponse.success && mainResponse.data != null) {
                    packages.value = mainResponse.data
                }
                
                // İndirimli paket listesini çek
                val discountResponse = packageService.getDiscountPackages()
                if (discountResponse.success && discountResponse.data != null) {
                    discountPackages.value = discountResponse.data
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
                            // Kuponlu paketleri göster
                            packages.value = response.data.packages
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
                    
                    // Ana paket listesini tekrar çek
                    val mainResponse = packageService.getMainPackages()
                    if (mainResponse.success && mainResponse.data != null) {
                        packages.value = mainResponse.data
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
                onClick = { onBack(discountPackages.value.isNotEmpty()) },
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
                text = stringResource(id = R.string.buy_more_tokens),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else {
            Text(
                text = "Kupon Avantajları",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Jeton paketleri listesi
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            packages.value.forEach { pkg ->
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
                        Column {
                            Text(
                                text = pkg.name ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pkg.desc ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorResource(id = R.color.primaryColor)
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

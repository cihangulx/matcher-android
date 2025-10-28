package com.matcher.matcher.modules.main.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matcher.matcher.R
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.matcher.matcher.models.ticket.TicketRequest
import com.matcher.matcher.network.ApiClient
import com.matcher.matcher.models.auth.AppOptions
import com.matcher.matcher.utils.helpers.ToastHelper
import com.matcher.matcher.network.model.ApiException
import android.app.Activity
import android.content.Intent
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // API'nin beklediği enum değerleri
    val konuList = listOf(
        "İstek/Şikayet" to "request",
        "Abonelik" to "subscription", 
        "Diğer" to "other"
    )
    var selectedKonu by remember { mutableStateOf(konuList[0]) }
    var expanded by remember { mutableStateOf(false) }
    var aciklama by remember { mutableStateOf("") }
    var showAltEmail by remember { mutableStateOf(false) }
    var userAltEmail by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var appSettings by remember { mutableStateOf<AppOptions?>(null) }
    var isLoadingSettings by remember { mutableStateOf(true) }
    
    // Uygulama ayarlarını yükleme fonksiyonu
    fun loadAppSettings() {
        coroutineScope.launch {
            try {
                val settingsService = ApiClient.getInstance(context).settingsService
                val response = settingsService.getSettings()
                
                if (response.success) {
                    appSettings = response.data
                } else {
                    // Hata durumunda null bırak, butonlar görünmez olur
                    appSettings = null
                }
            } catch (e: Exception) {
                // Hata durumunda null bırak, butonlar görünmez olur
                appSettings = null
            } finally {
                isLoadingSettings = false
            }
        }
    }
    
    // URL açma fonksiyonu
    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            (context as? Activity)?.let { activity ->
                ToastHelper.showError(activity, "URL açılamadı")
            }
        }
    }
    
    // Sayfa yüklendiğinde ayarları al
    LaunchedEffect(Unit) {
        loadAppSettings()
    }
    
    // API çağrısı fonksiyonu
    fun sendTicket() {
        if (aciklama.isBlank()) {
            (context as? Activity)?.let { activity ->
                ToastHelper.showWarning(activity, "Lütfen açıklama alanını doldurun")
            }
            return
        }
        
        isLoading = true
        coroutineScope.launch {
            try {
                val ticketService = ApiClient.getInstance(context).ticketService
                val request = TicketRequest(
                    title = selectedKonu.second,
                    message = aciklama,
                    email = if (showAltEmail && userAltEmail.isNotBlank()) userAltEmail else null
                )
                
                val response = ticketService.sendTicket(request)
                
                if (response.success) {
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showSuccess(activity, "Mesajınız başarıyla gönderildi!")
                    }
                    
                    // Formu temizle
                    aciklama = ""
                    userAltEmail = ""
                    showAltEmail = false
                    selectedKonu = konuList[0]
                } else {
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showError(activity, response.message ?: "Bilinmeyen hata oluştu")
                    }
                }
                
            } catch (e: Exception) {
                val errorMessage = ApiException.getErrorMessage(e)
                (context as? Activity)?.let { activity ->
                    ToastHelper.showError(activity, errorMessage)
                }
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Konu seçimi
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedKonu.first,
                onValueChange = {},
                readOnly = true,
                label = { Text("Konu") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
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
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                konuList.forEach { konu ->
                    DropdownMenuItem(
                        text = { Text(konu.first) },
                        onClick = {
                            selectedKonu = konu
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Açıklama alanı
        OutlinedTextField(
            value = aciklama,
            onValueChange = { aciklama = it },
            label = { Text("Açıklama") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 5,
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

        Spacer(modifier = Modifier.height(16.dp))

        // Alternatif adres için checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = showAltEmail,
                onCheckedChange = { showAltEmail = it }
            )
            Text("Bana başka bir adresten ulaş.", fontSize = 15.sp)
        }
        if (showAltEmail) {
            Text(
                text = "Bize ulaşmamızı istediğiniz e-posta adresini girin.",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp, top = 2.dp)
            )
            OutlinedTextField(
                value = userAltEmail,
                onValueChange = { userAltEmail = it },
                label = { Text("E-posta adresiniz") },
                placeholder = { Text("user123@email.com") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
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
            if (userAltEmail.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFFF1F1F1),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ulaşmamızı istediğiniz adres: ",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = userAltEmail,
                            color = Color(0xFF007AFF),
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // WhatsApp ve Telegram ile ulaşın butonları (dinamik)
        if (!isLoadingSettings && appSettings != null) {
            val hasWhatsApp = !appSettings?.whatsappUrl.isNullOrBlank()
            val hasTelegram = !appSettings?.telegramUrl.isNullOrBlank()
            
            if (hasWhatsApp || hasTelegram) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // WhatsApp butonu
                    if (hasWhatsApp) {
                        Button(
                            onClick = { 
                                appSettings?.whatsappUrl?.let { url -> 
                                    openUrl(url) 
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.whatsapp),
                                contentDescription = "WhatsApp",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WhatsApp", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    
                    // Spacer sadece her iki buton da varsa
                    if (hasWhatsApp && hasTelegram) {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    
                    // Telegram butonu
                    if (hasTelegram) {
                        Button(
                            onClick = { 
                                appSettings?.telegramUrl?.let { url -> 
                                    openUrl(url) 
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF229ED9)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.telegram),
                                contentDescription = "Telegram",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Telegram", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { sendTicket() },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF007AFF), 
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF007AFF).copy(alpha = 0.6f),
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gönderiliyor...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            } else {
                Text("Gönder", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

package com.flort.evlilik.modules.main.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.app.Activity
import androidx.activity.ComponentActivity
import com.flort.evlilik.utils.helpers.ToastHelper
import com.flort.evlilik.network.model.ApiException
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flort.evlilik.network.ApiClient
import com.flort.evlilik.models.ticket.ReportReason
import com.flort.evlilik.models.ticket.request.ReportRequest
import kotlinx.coroutines.launch
import com.flort.evlilik.R

class ReportActivity : ComponentActivity() {
    companion object {
        private var reportedUserId: String? = null
        private var reportedUserName: String? = null
        
        fun start(context: Context, userId: String, userName: String) {
            reportedUserId = userId
            reportedUserName = userName
            val intent = Intent(context, ReportActivity::class.java)
            context.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ReportScreen(
                userId = reportedUserId ?: "",
                userName = reportedUserName ?: "",
                onBack = { finish() }
            )
        }
    }
}

@Composable
private fun ReportScreen(
    userId: String,
    userName: String,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Arka plan resmi
        Image(
            painter = painterResource(id = R.drawable.page_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds
        )
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Toolbar
            ReportToolbar(
                onBack = onBack,
                title = "Şikayet Et"
            )
            
            // İçerik
            ReportContent(
                userId = userId,
                userName = userName,
                onReportSuccess = onBack
            )
        }
    }
}

@Composable
private fun ReportToolbar(
    onBack: () -> Unit,
    title: String
) {
    val shape: Shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.statusBarsPadding())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .shadow(elevation = 8.dp, shape = shape, clip = false)
                .clip(shape)
                .background(Color.White)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Geri butonu
            Icon(
                painter = painterResource(id = R.drawable.left_arrow),
                contentDescription = "Geri",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBack() },
                tint = Color.Black
            )
            
            // Başlık
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            
            // Boş alan (simetri için)
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun ReportContent(
    userId: String,
    userName: String,
    onReportSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // State'ler
    var reportReasons by remember { mutableStateOf<List<ReportReason>>(emptyList()) }
    var selectedReason by remember { mutableStateOf<ReportReason?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isReporting by remember { mutableStateOf(false) }
    
    // API'den şikayet nedenlerini çek
    LaunchedEffect(Unit) {
        try {
            val ticketService = ApiClient.getInstance(context).ticketService
            val response = ticketService.getReportReasons()
            if (response.success && response.data != null) {
                reportReasons = response.data
            } else {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showError(activity, response.message ?: "Nedenler yüklenemedi")
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
    
    // Şikayet gönderme fonksiyonu
    fun sendReport() {
        if (selectedReason == null) {
            (context as? Activity)?.let { activity ->
                ToastHelper.showWarning(activity, "Lütfen bir sebep seçin")
            }
            return
        }
        
        coroutineScope.launch {
            isReporting = true
            try {
                val ticketService = ApiClient.getInstance(context).ticketService
                val request = ReportRequest(
                    reportedUserId = userId,
                    reason = selectedReason!!.reason
                )
                
                val response = ticketService.sendReport(request)
                
                if (response.success) {
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showSuccess(activity, "Şikayetiniz başarıyla gönderildi")
                    }
                    onReportSuccess()
                } else {
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showError(activity, response.message ?: "Şikayet gönderilemedi")
                    }
                }
            } catch (e: Exception) {
                val errorMessage = ApiException.getErrorMessage(e)
                (context as? Activity)?.let { activity ->
                    ToastHelper.showError(activity, errorMessage)
                }
            } finally {
                isReporting = false
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Ana içerik - scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Report Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_error),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFFE53935)
                )
            }
            
            // Başlık
            Text(
                text = "$userName kullanıcısını şikayet etmek istediğinize emin misiniz?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Açıklama
            Text(
                text = "Şikayet sebebinizi seçin:",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isLoading) {
                // Loading indicator
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.primaryColor)
                    )
                }
            } else {
                // Report reasons listesi
                reportReasons.forEach { reason ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colorResource(id = R.color.primaryColor),
                                    unselectedColor = Color.Gray
                                )
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = reason.reason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Alt boşluk - buton için yer bırak
            Spacer(modifier = Modifier.height(100.dp))
        }
        
        // Sabitlenmiş buton - alt kısımda
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Button(
                onClick = { sendReport() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isReporting && selectedReason != null
            ) {
                if (isReporting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Şikayet Gönder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


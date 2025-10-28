package com.matcher.matcher.modules.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.matcher.matcher.R
import com.matcher.matcher.models.user.User
import com.matcher.matcher.utils.helpers.ToastHelper
import com.matcher.matcher.network.model.ApiException
import android.app.Activity
import com.matcher.matcher.network.repository.GalleryRepository
import com.matcher.matcher.modules.account.components.AddItemCard
import com.matcher.matcher.modules.account.components.GalleryItemCard
import kotlinx.coroutines.launch

@Composable
fun GalleryContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var galleryItems by remember { mutableStateOf<List<com.matcher.matcher.models.auth.GalleryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    
    var itemsToDelete by remember { mutableStateOf<Set<String>>(emptySet()) }
    var newMainImageUrl by remember { mutableStateOf<String?>(null) }
    var originalMainImageUrl by remember { mutableStateOf<String?>(null) }
    var originalGallerySize by remember { mutableStateOf(0) }
    
    val galleryRepository = remember { GalleryRepository(context) }
    
    val hasChanges = itemsToDelete.isNotEmpty() || 
                     newMainImageUrl != originalMainImageUrl ||
                     galleryItems.size != originalGallerySize
    
    LaunchedEffect(Unit) {
        try {
            galleryRepository.getGallery().collect { result ->
                result.fold(
                    onSuccess = { items ->
                        items.forEachIndexed { index, item ->
                        }
                        galleryItems = items
                        val mainImageUrl = items.find { it.isMain == true }?.url
                        newMainImageUrl = mainImageUrl
                        originalMainImageUrl = mainImageUrl
                        originalGallerySize = items.size
                        isLoading = false
                    },
                    onFailure = { error ->
                        isLoading = false
                        
                        (context as? Activity)?.let { activity ->
                            ToastHelper.showError(activity, error.message ?: "Galeri yüklenemedi")
                        }
                    }
                )
            }
        } catch (e: Exception) {
            isLoading = false
            
            val errorMsg = ApiException.getErrorMessage(e)
            (context as? Activity)?.let { activity ->
                ToastHelper.showError(activity, errorMsg)
            }
        }
    }
    
    fun saveChanges() {
        coroutineScope.launch {
            try {
                isSaving = true
                
                var updatedGallery = galleryItems
                    .filter { !itemsToDelete.contains(it.url) }
                
                var finalMainImageUrl = newMainImageUrl
                if (finalMainImageUrl == null && updatedGallery.isNotEmpty()) {
                    finalMainImageUrl = updatedGallery.first().url
                }
                
                updatedGallery = updatedGallery.map { item ->
                    item.copy(isMain = item.url == finalMainImageUrl)
                }
                
                
                galleryRepository.updateGallery(updatedGallery).collect { result ->
                    result.fold(
                        onSuccess = { updatedItems ->
                            galleryItems = updatedItems
                            itemsToDelete = emptySet()
                            originalMainImageUrl = updatedItems.find { it.isMain == true }?.url
                            newMainImageUrl = originalMainImageUrl
                            originalGallerySize = updatedItems.size
                            
                            coroutineScope.launch {
                                val updateSuccess = User.updateCurrentUser(context)
                                isSaving = false
                                
                                (context as? Activity)?.let { activity ->
                                    ToastHelper.showSuccess(activity, "Galeri başarıyla kaydedildi")
                                }
                            }
                        },
                        onFailure = { error ->
                            isSaving = false
                            
                            (context as? Activity)?.let { activity ->
                                ToastHelper.showError(activity, error.message ?: "Galeri güncellenemedi")
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                isSaving = false
                
                val errorMsg = ApiException.getErrorMessage(e)
                (context as? Activity)?.let { activity ->
                    ToastHelper.showError(activity, errorMsg)
                }
            }
        }
    }
    
    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = colorResource(id = R.color.primaryColor)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                item {
                    AddItemCard(
                        isUploading = isUploading,
                        onImageSelected = { uri ->
                            uri?.let { imageUri ->
                                coroutineScope.launch {
                                    try {
                                        isUploading = true
                                        galleryRepository.uploadFile(imageUri, "gallery").collect { result ->
                                        result.fold(
                                            onSuccess = { imageUrl ->
                                                val newItem = com.matcher.matcher.models.auth.GalleryItem(
                                                    index = galleryItems.size,
                                                    url = imageUrl,
                                                    isMain = false
                                                )
                                                galleryItems = galleryItems + newItem
                                                isUploading = false
                                                
                                                (context as? Activity)?.let { activity ->
                                                    ToastHelper.showSuccess(activity, "Resim başarıyla yüklendi")
                                                }
                                            },
                                            onFailure = { error ->
                                                isUploading = false
                                                
                                                (context as? Activity)?.let { activity ->
                                                    ToastHelper.showError(activity, error.message ?: "Resim yüklenemedi")
                                                }
                                            }
                                        )
                                    }
                                    } catch (e: Exception) {
                                        isUploading = false
                                        
                                        val errorMsg = ApiException.getErrorMessage(e)
                                        (context as? Activity)?.let { activity ->
                                            ToastHelper.showError(activity, errorMsg)
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
                    
                    items(galleryItems) { item ->
                        GalleryItemCard(
                            item = item,
                            isMarkedForDeletion = itemsToDelete.contains(item.url),
                            isSelectedAsMain = newMainImageUrl == item.url,
                            onDeleteToggle = { url ->
                                itemsToDelete = if (itemsToDelete.contains(url)) {
                                    val newSet = itemsToDelete - url
                                    if (url == originalMainImageUrl && newMainImageUrl == null) {
                                        newMainImageUrl = originalMainImageUrl
                                    }
                                    newSet
                                } else {
                                    if (newMainImageUrl == url) {
                                        newMainImageUrl = null
                                    }
                                    itemsToDelete + url
                                }
                            },
                            onSetAsMain = { url ->
                                newMainImageUrl = url
                            }
                        )
                    }
                }
            }
        }
        
        val buttonColor = colorResource(id = R.color.primaryDarkColor)
        Button(
            onClick = { 
                saveChanges()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                disabledContainerColor = buttonColor.copy(alpha = 0.30f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSaving && hasChanges
        ) {
            if (isSaving) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Kaydediliyor...",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    text = "Değişiklikleri Kaydet",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        }
    }
}



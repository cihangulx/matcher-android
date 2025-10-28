package com.matcher.matcher.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.matcher.matcher.utils.FileUploadUtils
import com.matcher.matcher.utils.PermissionUtils
import com.matcher.matcher.utils.rememberImagePickerState
import com.matcher.matcher.utils.rememberPermissionState

@Composable
fun SingleImageUploadComponent(
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    onImageRemoved: () -> Unit = {},
    modifier: Modifier = Modifier,
    placeholderText: String = "Resim Seç",
    maxFileSizeMB: Int = 10
) {
    val context = LocalContext.current
    val imagePickerState = rememberImagePickerState()
    val permissionState = rememberPermissionState()
    
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        permissionState.updatePermissions(context)
    }
    
    LaunchedEffect(imagePickerState.selectedImageUri.value) {
        imagePickerState.selectedImageUri.value?.let { uri ->
            val file = FileUploadUtils.uriToFile(context, uri)
            if (file != null) {
                if (!FileUploadUtils.isFileSizeValid(file, maxFileSizeMB)) {
                    errorMessage = "Dosya boyutu ${maxFileSizeMB}MB'dan büyük olamaz"
                    return@LaunchedEffect
                }
                
                if (!FileUploadUtils.isSupportedImageType(file)) {
                    errorMessage = "Desteklenmeyen dosya türü"
                    return@LaunchedEffect
                }
                
                onImageSelected(uri)
                errorMessage = null
            }
        }
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selectedImageUri != null) {
            ImagePreviewCard(
                imageUri = selectedImageUri,
                onRemove = {
                    onImageRemoved()
                    onImageSelected(null)
                }
            )
        } else {
            ImageSelectionCard(
                onClick = {
                    if (permissionState.allPermissionsGranted.value) {
                        showImagePickerDialog = true
                    } else {
                        showPermissionDialog = true
                    }
                },
                placeholderText = placeholderText
            )
        }
        
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
    }
    
    ImagePickerBottomSheet(
        isVisible = showImagePickerDialog,
        onDismiss = { showImagePickerDialog = false },
        onImageSelected = { uri ->
            imagePickerState.setSelectedImage(uri)
        }
    )
    
    if (showPermissionDialog) {
        PermissionRequiredDialog(
            onDismiss = { showPermissionDialog = false },
            onPermissionGranted = {
                showImagePickerDialog = true
                showPermissionDialog = false
            }
        )
    }
}

@Composable
private fun ImagePreviewCard(
    imageUri: Uri,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Seçilen resim",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        CircleShape
                    )
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Resmi sil",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
@Composable
private fun ImageSelectionCard(
    onClick: () -> Unit,
    placeholderText: String
) {
    Card(
        modifier = Modifier
            .size(120.dp)
            .clickable { onClick() }
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📷",
                fontSize = 32.sp,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = placeholderText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Composable
private fun PermissionRequiredDialog(
    onDismiss: () -> Unit,
    onPermissionGranted: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("İzin Gerekli")
        },
        text = {
            Text("Resim seçmek için kamera ve galeri izinlerine ihtiyacımız var.")
        },
        confirmButton = {
            TextButton(onClick = onPermissionGranted) {
                Text("Ayarlara Git")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

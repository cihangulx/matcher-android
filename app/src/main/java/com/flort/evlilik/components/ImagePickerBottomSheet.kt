package com.flort.evlilik.components

import android.Manifest
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.flort.evlilik.R
import com.flort.evlilik.utils.launchCamera
import com.flort.evlilik.utils.launchGallery
import com.flort.evlilik.utils.rememberCameraLauncher
import com.flort.evlilik.utils.rememberGalleryLauncher
import com.flort.evlilik.utils.rememberImageCropLauncher
import com.flort.evlilik.utils.startImageCrop
import com.flort.evlilik.utils.PermissionUtils
import kotlinx.coroutines.delay

@OptIn( ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasGalleryPermission by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        hasCameraPermission = PermissionUtils.hasCameraPermission(context)
        hasGalleryPermission = PermissionUtils.hasGalleryPermission(context)
    }
    
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    
    val cropLauncher = rememberImageCropLauncher(
        onImageCropped = { uri ->
            if (uri != null) {
                onImageSelected(uri)
                onDismiss()
            } else {
            }
        },
        onError = { error ->
            onError(error)
        }
    )
    
    val galleryLauncher = rememberGalleryLauncher(
        onImageSelected = { uri ->
            if (uri != null) {
                startImageCrop(context, uri, cropLauncher)
            }
        },
        onError = onError
    )
    
    val cameraLauncher = rememberCameraLauncher(
        onImageSelected = { uri ->
            cameraUri?.let { 
                startImageCrop(context, it, cropLauncher)
            } ?: run {
                onError("Kamera URI'si bulunamadı")
            }
        },
        onError = { error ->
            onError(error)
        }
    )
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val galleryGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        
        hasCameraPermission = cameraGranted
        hasGalleryPermission = galleryGranted
    }
    
    if (isVisible) {
        val bottomSheetState = rememberModalBottomSheetState()
        
        LaunchedEffect(Unit) {
            delay(100)
            bottomSheetState.expand()
        }
        
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState
        ) {
            ImagePickerSheetContent(
                onCameraClick = {
                    if (hasCameraPermission) {
                        val uri = launchCamera(context, cameraLauncher)
                        if (uri != null) {
                            cameraUri = uri
                        }
                    } else {
                        permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                    }
                },
                onGalleryClick = {
                    if (hasGalleryPermission) {
                        launchGallery(galleryLauncher)
                    } else {
                        permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                    }
                },
                onDismiss = onDismiss,
                hasCameraPermission = hasCameraPermission,
                hasGalleryPermission = hasGalleryPermission
            )
        }
    }
}

@Composable
private fun ImagePickerSheetContent(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDismiss: () -> Unit,
    hasCameraPermission: Boolean,
    hasGalleryPermission: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Resim Seç",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Fotoğraf çekmek veya galeriden seçmek için bir seçenek belirleyin",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                            ImagePickerSheetOption(
                                icon = R.drawable.ic_camera,
                                title = "Kamera",
                                description = if (hasCameraPermission) "Yeni fotoğraf çek" else "Kamera izni verilmedi",
                                onClick = onCameraClick,
                                backgroundColor = if (hasCameraPermission)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                iconTint = if (hasCameraPermission)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                isEnabled = hasCameraPermission
                            )

                            ImagePickerSheetOption(
                                icon = R.drawable.ic_gallery,
                                title = "Galeri",
                                description = if (hasGalleryPermission) "Galeriden seç" else "Galeri izni verilmedi",
                                onClick = onGalleryClick,
                                backgroundColor = if (hasGalleryPermission)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                iconTint = if (hasGalleryPermission)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                isEnabled = hasGalleryPermission
                            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "İptal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ImagePickerSheetOption(
    icon: Int,
    title: String,
    description: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    iconTint: Color,
    isEnabled: Boolean = true
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconTint
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEnabled) 
                        MaterialTheme.colorScheme.onSurface 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = if (isEnabled) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isEnabled) 
                    MaterialTheme.colorScheme.onSurfaceVariant 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

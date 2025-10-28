package com.matcher.matcher.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.matcher.matcher.R
import com.matcher.matcher.utils.launchCamera
import com.matcher.matcher.utils.launchGallery
import com.matcher.matcher.utils.rememberCameraLauncher
import com.matcher.matcher.utils.rememberGalleryLauncher
import com.matcher.matcher.utils.rememberImageCropLauncher
import com.matcher.matcher.utils.startImageCrop
import com.matcher.matcher.utils.PermissionUtils
import com.matcher.matcher.utils.rememberPermissionState

@Composable
fun ImagePickerDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onImageSelected: (android.net.Uri?) -> Unit,
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState()
    
    LaunchedEffect(Unit) {
        permissionState.updatePermissions(context)
    }
    
    var cameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
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
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            permissionState.updatePermissions(context)
        } else {
            onError("İzinler verilmedi")
        }
    }
    
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Resim Seç",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    ImagePickerOption(
                        icon = R.drawable.ic_camera,
                        title = "Kamera",
                        description = "Fotoğraf çek",
                        onClick = {
                            if (permissionState.hasCameraPermission.value) {
                                val uri = launchCamera(context, cameraLauncher)
                                if (uri != null) {
                                    cameraUri = uri
                                }
                            } else {
                                permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ImagePickerOption(
                        icon = R.drawable.ic_gallery,
                        title = "Galeri",
                        description = "Galeriden seç",
                        onClick = {
                            if (permissionState.hasGalleryPermission.value) {
                                launchGallery(galleryLauncher)
                            } else {
                                permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("İptal")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImagePickerOption(
    icon: Int,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

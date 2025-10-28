package com.matcher.matcher.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import com.matcher.matcher.modules.crop.ImageCropActivity

/**
 * Image picker için state holder
 */
class ImagePickerState {
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun setSelectedImage(uri: Uri?) {
        _selectedImageUri.value = uri
    }
    
    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    fun setError(error: String?) {
        _error.value = error
    }
    
    fun clearError() {
        _error.value = null
    }
}

/**
 * Compose'da image picker kullanımı için composable
 */
@Composable
fun rememberImagePickerState(): ImagePickerState {
    return remember { ImagePickerState() }
}

/**
 * Galeri erişimi için launcher
 */
@Composable
fun rememberGalleryLauncher(
    onImageSelected: (Uri?) -> Unit,
    onError: (String) -> Unit = {}
): androidx.activity.result.ActivityResultLauncher<String> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onImageSelected(uri)
        } else {
            onError("Resim seçilemedi")
        }
    }
}

/**
 * Kamera erişimi için launcher
 */
@Composable
fun rememberCameraLauncher(
    onImageSelected: (Uri?) -> Unit,
    onError: (String) -> Unit = {}
): androidx.activity.result.ActivityResultLauncher<Uri> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->        
        if (success) {
            onImageSelected(null)
        } else {
            onError("Fotoğraf çekilemedi")
        }
    }
}

/**
 * Galeriden resim seçme
 */
fun launchGallery(
    launcher: androidx.activity.result.ActivityResultLauncher<String>
) {
    launcher.launch("image/*")
}

/**
 * Kamera ile resim çekme
 */
fun launchCamera(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Uri>
): Uri? {
    
    val tempFile = createTempImageFile(context)
    val photoUri = getFileProviderUri(context, tempFile)
    
    try {
        launcher.launch(photoUri)
        return photoUri
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

/**
 * FileProvider ile güvenli URI oluşturur
 */
fun getFileProviderUri(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

/**
 * Kamera için geçici dosya oluşturur
 */
fun createTempImageFile(context: Context): File {
    val timeStamp = System.currentTimeMillis()
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = File(context.cacheDir, "images")
    
    if (!storageDir.exists()) {
        val created = storageDir.mkdirs()
    }
    
    val tempFile = File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )
    
    return tempFile
}

/**
 * Kamera için geçici dosya oluşturur ve URI döndürür
 */
fun createTempImageFileWithUri(context: Context): Pair<File, Uri> {
    val tempFile = createTempImageFile(context)
    val photoUri = getFileProviderUri(context, tempFile)
    return Pair(tempFile, photoUri)
}

/**
 * Resim crop işlemi için launcher
 */
@Composable
fun rememberImageCropLauncher(
    onImageCropped: (Uri?) -> Unit,
    onError: (String) -> Unit = {}
): androidx.activity.result.ActivityResultLauncher<Intent> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val croppedUriString = result.data?.getStringExtra(ImageCropActivity.RESULT_CROPPED_URI)
            if (croppedUriString != null) {
                val croppedUri = Uri.parse(croppedUriString)
                onImageCropped(croppedUri)
            } else {
                onError("Crop edilen resim URI'si bulunamadı")
            }
        } else {
            onImageCropped(null)
        }
    }
}

/**
 * Resim crop işlemini başlatır
 */
fun startImageCrop(
    context: Context,
    inputUri: Uri,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    val timeStamp = System.currentTimeMillis()
    val outputFileName = "cropped_${timeStamp}.jpg"
    
    // External files dizininde dosya oluştur
    val outputDir = File(context.getExternalFilesDir(null), "cropped_images")
    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }
    
    val croppedFile = File(outputDir, outputFileName)
    val outputUri = getFileProviderUri(context, croppedFile)
    
    val intent = Intent(context, ImageCropActivity::class.java).apply {
        putExtra(ImageCropActivity.EXTRA_INPUT_URI, inputUri)
        putExtra(ImageCropActivity.EXTRA_OUTPUT_URI, outputUri)
    }
    
    launcher.launch(intent)
}
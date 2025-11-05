package com.flort.evlilik.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Kamera ve galeri izinleri için gerekli permission'lar
 */
object PermissionUtils {
    
    val CAMERA_PERMISSION = Manifest.permission.CAMERA
    val READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    val READ_MEDIA_IMAGES_PERMISSION = Manifest.permission.READ_MEDIA_IMAGES
    
    /**
     * Android 13+ için gerekli izinler
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                CAMERA_PERMISSION,
                READ_MEDIA_IMAGES_PERMISSION
            )
        } else {
            arrayOf(
                CAMERA_PERMISSION,
                READ_EXTERNAL_STORAGE_PERMISSION
            )
        }
    }
    
    /**
     * İzinlerin verilip verilmediğini kontrol eder
     */
    fun hasRequiredPermissions(context: Context): Boolean {
        val permissions = getRequiredPermissions()
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Belirli bir iznin verilip verilmediğini kontrol eder
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Kamera izninin verilip verilmediğini kontrol eder
     */
    fun hasCameraPermission(context: Context): Boolean {
        return hasPermission(context, CAMERA_PERMISSION)
    }
    
    /**
     * Galeri izninin verilip verilmediğini kontrol eder
     */
    fun hasGalleryPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(context, READ_MEDIA_IMAGES_PERMISSION)
        } else {
            hasPermission(context, READ_EXTERNAL_STORAGE_PERMISSION)
        }
    }
}

/**
 * Compose'da permission launcher kullanımı
 */
@Composable
fun rememberPermissionLauncher(
    onPermissionResult: (Boolean) -> Unit
): androidx.activity.result.ActivityResultLauncher<Array<String>> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        onPermissionResult(allGranted)
    }
}

/**
 * Tek bir permission için launcher
 */
@Composable
fun rememberSinglePermissionLauncher(
    onPermissionResult: (Boolean) -> Unit
): androidx.activity.result.ActivityResultLauncher<String> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }
}

/**
 * Permission state'i için composable
 */
@Composable
fun rememberPermissionState(): PermissionState {
    return remember { PermissionState() }
}

/**
 * Permission durumunu yöneten state class
 */
class PermissionState {
    private val _hasCameraPermission = MutableStateFlow(false)
    val hasCameraPermission: StateFlow<Boolean> = _hasCameraPermission.asStateFlow()
    
    private val _hasGalleryPermission = MutableStateFlow(false)
    val hasGalleryPermission: StateFlow<Boolean> = _hasGalleryPermission.asStateFlow()
    
    private val _allPermissionsGranted = MutableStateFlow(false)
    val allPermissionsGranted: StateFlow<Boolean> = _allPermissionsGranted.asStateFlow()
    
    fun updatePermissions(context: Context) {
        _hasCameraPermission.value = PermissionUtils.hasCameraPermission(context)
        _hasGalleryPermission.value = PermissionUtils.hasGalleryPermission(context)
        _allPermissionsGranted.value = PermissionUtils.hasRequiredPermissions(context)
    }
    
    fun setCameraPermission(granted: Boolean) {
        _hasCameraPermission.value = granted
        updateAllPermissions()
    }
    
    fun setGalleryPermission(granted: Boolean) {
        _hasGalleryPermission.value = granted
        updateAllPermissions()
    }
    
    private fun updateAllPermissions() {
        _allPermissionsGranted.value = _hasCameraPermission.value && _hasGalleryPermission.value
    }
}

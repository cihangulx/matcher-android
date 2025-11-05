package com.flort.evlilik.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUploadUtils {
    
    /**
     * Uri'yi File'a dönüştürür
     */
    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val fileName = getFileName(context, uri)
            val file = File(context.cacheDir, fileName)
            
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Uri'den dosya adını alır
     */
    private fun getFileName(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex) ?: "temp_file_${System.currentTimeMillis()}"
        } ?: "temp_file_${System.currentTimeMillis()}"
    }


    /**
     * Dosya boyutunu kontrol eder (MB cinsinden)
     */
    fun isFileSizeValid(file: File, maxSizeMB: Int = 10): Boolean {
        val fileSizeMB = file.length() / (1024 * 1024)
        return fileSizeMB <= maxSizeMB
    }
    
    /**
     * Desteklenen dosya türlerini kontrol eder
     */
    fun isSupportedImageType(file: File): Boolean {
        val supportedTypes = listOf("jpg", "jpeg", "png", "gif", "webp")
        val extension = file.extension.lowercase()
        return supportedTypes.contains(extension)
    }
}

package com.matcher.matcher.modules.crop

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.yalantis.ucrop.UCrop

class ImageCropActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_INPUT_URI = "input_uri"
        const val EXTRA_OUTPUT_URI = "output_uri"
        const val RESULT_CROPPED_URI = "cropped_uri"
    }
    
    private var inputUri: Uri? = null
    private var outputUri: Uri? = null
    
    private val cropLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val croppedUri = UCrop.getOutput(result.data!!)
            if (croppedUri != null) {
                android.util.Log.d("ImageCropActivity", "Crop successful: $croppedUri")
                val resultIntent = Intent().apply {
                    putExtra(RESULT_CROPPED_URI, croppedUri.toString())
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                android.util.Log.e("ImageCropActivity", "Cropped URI is null")
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            android.util.Log.e("ImageCropActivity", "Crop failed", cropError)
            setResult(Activity.RESULT_CANCELED)
            finish()
        } else {
            android.util.Log.d("ImageCropActivity", "Crop cancelled")
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        inputUri = intent.getParcelableExtra(EXTRA_INPUT_URI)
        outputUri = intent.getParcelableExtra(EXTRA_OUTPUT_URI)
        
        if (inputUri == null || outputUri == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        
        startCrop()
    }
    
    private fun startCrop() {
        try {
            android.util.Log.d("ImageCropActivity", "Starting crop with input: $inputUri, output: $outputUri")
            
            // URI'leri kontrol et
            if (inputUri == null || outputUri == null) {
                android.util.Log.e("ImageCropActivity", "Input or output URI is null")
                setResult(Activity.RESULT_CANCELED)
                finish()
                return
            }
            
            val options = UCrop.Options().apply {
                setToolbarTitle("Resmi Kırp")
                setToolbarColor(resources.getColor(android.R.color.black, null))
                //setStatusBarColor(resources.getColor(android.R.color.black, null))
                setToolbarWidgetColor(resources.getColor(android.R.color.white, null))
                setActiveControlsWidgetColor(resources.getColor(android.R.color.white, null))
                setCompressionQuality(90)
                setFreeStyleCropEnabled(false) // Serbest crop'u kapat
                setHideBottomControls(false)
                setShowCropFrame(true)
                setShowCropGrid(true)
                setCropGridColor(resources.getColor(android.R.color.white, null))
                setCropFrameColor(resources.getColor(android.R.color.white, null))
                setMaxBitmapSize(2048)
            }
            
            val cropIntent = UCrop.of(inputUri!!, outputUri!!)
                .withAspectRatio(1f, 1f) // Kare crop (1:1)
                .withMaxResultSize(800, 800) // Maksimum boyut
                .withOptions(options)
                .getIntent(this)
            
            android.util.Log.d("ImageCropActivity", "Launching Yalantis uCrop")
            cropLauncher.launch(cropIntent)
        } catch (e: Exception) {
            android.util.Log.e("ImageCropActivity", "Error starting crop", e)
            e.printStackTrace()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}

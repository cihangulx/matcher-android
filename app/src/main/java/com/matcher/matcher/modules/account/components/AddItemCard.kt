package com.matcher.matcher.modules.account.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.matcher.matcher.R
import com.matcher.matcher.components.ImagePickerBottomSheet

@Composable
fun AddItemCard(
    isUploading: Boolean,
    onImageSelected: (android.net.Uri?) -> Unit
) {
    var showImagePicker by remember { mutableStateOf(false) }
    
    Surface(
        onClick = { 
            if (!isUploading) {
                showImagePicker = true
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colorResource(id = R.color.primaryColor).copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = colorResource(id = R.color.primaryColor)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.add_image_plus),
                    contentDescription = "Resim Ekle",
                    modifier = Modifier.size(32.dp),
                    tint = colorResource(id = R.color.primaryColor).copy(alpha = 0.5f)
                )
            }
        }
    }
    
    if (!isUploading) {
        ImagePickerBottomSheet(
            isVisible = showImagePicker,
            onDismiss = { showImagePicker = false },
            onImageSelected = { uri ->
                if (uri != null) {
                    onImageSelected(uri)
                }
                showImagePicker = false
            }
        )
    }
}

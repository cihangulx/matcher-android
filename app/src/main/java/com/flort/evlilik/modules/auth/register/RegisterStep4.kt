package com.flort.evlilik.modules.auth.register

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.flort.evlilik.R
import com.flort.evlilik.components.ImagePickerBottomSheet

@Composable
fun RegisterStep4(
    selectedIndex: Int,
    onSlotClick: (Int) -> Unit,
    selectedImages: List<Uri?>,
    onImageSelected: (Int, Uri?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Fotoğraflarını Yükle",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Fotoğraflarını daha sonra da yükleyebilirsin.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(3) { index ->
                PhotoSlot(
                    index = index,
                    isSelected = index == selectedIndex,
                    imageUri = selectedImages.getOrNull(index),
                    onSlotClick = { onSlotClick(index) },
                    onImageSelected = { uri -> onImageSelected(index, uri) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, colorResource(id = R.color.whiteButtonStrokeColor))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.register_info_icon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "Fotoğraf eklenen profiller %45 daha fazla etkileşim alır.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun PhotoSlot(
    index: Int,
    isSelected: Boolean,
    imageUri: Uri?,
    onSlotClick: () -> Unit,
    onImageSelected: (Uri?) -> Unit
) {
    var showImagePicker by remember { mutableStateOf(false) }
    
    val borderColor = if (isSelected) {
        colorResource(id = R.color.primaryColor)
    } else {
        colorResource(id = R.color.primaryColor).copy(alpha = 0.3f)
    }
    
    Surface(
        onClick = {
            onSlotClick()
            if (isSelected) {
                showImagePicker = true
            }
        },
        modifier = Modifier
            .size(100.dp),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp, 
            color = borderColor
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Seçilen fotoğraf",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(
                        id = if (isSelected) R.drawable.add_image_plus_selected else R.drawable.add_image_plus
                    ),
                    contentDescription = "Fotoğraf ekle",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
    
    ImagePickerBottomSheet(
        isVisible = showImagePicker,
        onDismiss = { showImagePicker = false },
        onImageSelected = { uri ->
            onImageSelected(uri)
            showImagePicker = false
        }
    )
}

package com.flort.evlilik.modules.account.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.flort.evlilik.R
import com.flort.evlilik.models.auth.GalleryItem

@Composable
fun GalleryItemCard(
    item: GalleryItem,
    isMarkedForDeletion: Boolean,
    isSelectedAsMain: Boolean,
    onDeleteToggle: (String) -> Unit,
    onSetAsMain: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AsyncImage(
                    model = item.url,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (isMarkedForDeletion) {
                                Modifier.background(Color(0x80FF0000))
                            } else {
                                Modifier
                            }
                        ),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_placeholder),
                    error = painterResource(id = R.drawable.ic_error)
                )
                
                if (isMarkedForDeletion) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Silinecek",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            if (isMarkedForDeletion) Color(0xFFFF3B30) else Color(0x80000000),
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { 
                            onDeleteToggle(item.url)
                        }
                        .padding(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.delete_icon),
                        contentDescription = if (isMarkedForDeletion) "Silmeyi İptal Et" else "Sil",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
            
            val buttonColor = when {
                isMarkedForDeletion -> Color(0xFF3A3A3C) // Pasif gri
                isSelectedAsMain -> Color(0xFF8E8E93) // Seçili - gri
                else -> colorResource(id = R.color.primaryDarkColor) // Seçilebilir - mor
            }
            
            val buttonText = when {
                isMarkedForDeletion -> "Silinecek"
                isSelectedAsMain -> "Profil Resmi"
                else -> "Profil Resmi Yap"
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(buttonColor)
                    .clickable(enabled = !isMarkedForDeletion && !isSelectedAsMain) { 
                        onSetAsMain(item.url)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

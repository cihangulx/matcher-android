package com.matcher.matcher.modules.main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.matcher.matcher.R
import com.matcher.matcher.models.profile.Profile

@Composable
fun LikesGridItemView(
    profile: Profile,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    isBlurred: Boolean = false,
    onClick: (Profile) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f) // dikdörtgen oran
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(profile) }
    ) {
        // Arkaplan resim (tüm itemı kaplar)
        val blurAmount = if (isBlurred) 20.dp else 0.dp
        
        // API'den gelen resim
        if (!profile.gallery.isNullOrEmpty() && profile.gallery.first().url != null) {
            SubcomposeAsyncImage(
                model = profile.gallery.first().url,
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .then(if (blurAmount > 0.dp) Modifier.blur(blurAmount) else Modifier),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.matchParentSize().background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Gray
                        )
                    }
                },
                error = {
                    Box(modifier = Modifier.matchParentSize().background(Color.LightGray))
                }
            )
        } else {
            // Default placeholder
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .then(if (blurAmount > 0.dp) Modifier.blur(blurAmount) else Modifier),
                contentScale = ContentScale.Crop
            )
        }
        
        // Blur overlay (premium olmayan kullanıcılar için)
        if (isBlurred) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Gray.copy(alpha = 0.5f))
            )
        }

        // Alt kısım: İsim, yaş ve ikon (sadece premium için)
        if (!isBlurred) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Sol: İsim ve yaş
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // İsim
                        Text(
                            text = profile.name ?: "Kullanıcı",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                        
                        // Yaş
                        profile.age?.let { age ->
                            Text(
                                text = "$age yaşında",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Sağ: Beğeni ikonu
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            painter = painterResource(
                                id = if (selected) R.drawable.likes_selected else R.drawable.ic_eye
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .size(16.dp),
                            tint = if (selected) Color.Unspecified else Color.White
                        )
                    }
                }
            }
        }
    }
}



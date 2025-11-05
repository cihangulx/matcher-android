package com.flort.evlilik.modules.main.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.flort.evlilik.R
import com.flort.evlilik.models.profile.Profile

@Composable
fun HomeProfileItemView(
    profile: Profile,
    onLikeClick: (Profile) -> Unit = {},
    onMessageClick: (Profile) -> Unit = {},
    onProfileClick: (Profile) -> Unit = {},
    // Dots indicator parametreleri
    dotsBackgroundColor: Color = Color.Black.copy(alpha = 0.3f),
    selectedDotColor: Color = Color.White,
    unselectedDotColor: Color = Color.White.copy(alpha = 0.5f),
    dotSize: Int = 6,
    selectedDotSize: Int = 8
) {
    var isLiked by remember(profile.isLiked) { mutableStateOf(profile.isLiked ?: false) }
    var likeCount by remember(profile.like) { mutableStateOf(profile.like ?: 0) }
    val pagerState = rememberPagerState(pageCount = { profile.gallery.size })
    
    // Profile değiştiğinde state'leri güncelle
    LaunchedEffect(profile.like, profile.isLiked) {
        likeCount = profile.like ?: 0
        isLiked = profile.isLiked ?: false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick(profile) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Üst kısım: İsim ve profil fotoğrafı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profil fotoğrafı (daire içinde)
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        profile.getPp() != null -> {
                            // URL'den resim yükle
                            SubcomposeAsyncImage(
                                model = profile.getPp(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    ShimmerPlaceholder(
                                        modifier = Modifier.fillMaxSize()
                                    )
                                },
                                error = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_logo),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.White
                                    )
                                }
                            )
                        }
                        else -> {
                            // Default placeholder
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // İsim ve yaş
                Column {
                    Text(
                        text = "${profile.name ?: "İsimsiz"}, ${profile.age ?: "?"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    if (!profile.statusDesc.isNullOrEmpty()) {
                        Text(
                            text = profile.statusDesc!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Galeri bölümü (1:1 oran, kenar boşluğu yok)
            if (profile.gallery.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        val galleryItem = profile.gallery[page]
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f) // 1:1 oran
                        ) {
                            when {
                                galleryItem.url != null -> {
                                    // URL'den resim yükle
                                    SubcomposeAsyncImage(
                                        model = galleryItem.url,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        loading = {
                                            ShimmerPlaceholder(
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        },
                                        error = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.LightGray),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_logo),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(48.dp),
                                                    tint = Color.Gray
                                                )
                                            }
                                        }
                                    )
                                }
                                !galleryItem.url.isNullOrEmpty() -> {
                                    // URL'den resim yükle
                                    AsyncImage(
                                        model = galleryItem.url,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                else -> {
                                    // Varsayılan resim
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.LightGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_logo),
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Custom Dots Indicator - resmin ortasında alt
                    if (profile.gallery.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        ) {
                            // Kapsül şeklinde arkaplan
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = dotsBackgroundColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(profile.gallery.size) { index ->
                                        val isSelected = index == pagerState.currentPage
                                        val dotSizeDp = if (isSelected) selectedDotSize.dp else dotSize.dp
                                        
                                        Box(
                                            modifier = Modifier
                                                .size(dotSizeDp)
                                                .background(
                                                    color = if (isSelected) selectedDotColor else unselectedDotColor,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Galeri boşsa varsayılan görünüm
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                }
            }

            // Alt kısım: Beğeni butonu ve mesaj gönder butonu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sol taraf: Beğeni butonu ve sayısı
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            // Local state'i hemen güncelle
                            if (isLiked) {
                                // Unlike
                                isLiked = false
                                likeCount = maxOf(likeCount - 1, 0)
                            } else {
                                // Like
                                isLiked = true
                                likeCount = likeCount + 1
                            }
                            onLikeClick(profile)
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isLiked) R.drawable.likes_selected else R.drawable.likes
                            ),
                            contentDescription = if (isLiked) "Beğenildi" else "Beğen",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = likeCount.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isLiked) colorResource(id = R.color.primaryColor) else Color.Gray,
                        fontWeight = if (isLiked) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                // Sağ taraf: Mesaj gönder butonu
                Surface(
                    onClick = { onMessageClick(profile) },
                    modifier = Modifier
                        .height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colorResource(id = R.color.primaryColor)
                ) {
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.home_send_message),
                            contentDescription = "Mesaj Gönder",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        Text(
                            text = "Mesaj Gönder",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
        
        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.LightGray.copy(alpha = 0.3f))
        )
    }
}

/**
 * Shimmer placeholder component for loading states
 */
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200)
        ),
        label = "shimmer_alpha"
    )
    
    Box(
        modifier = modifier
            .background(
                Color.White.copy(alpha = alpha),
                CircleShape
            )
    )
}
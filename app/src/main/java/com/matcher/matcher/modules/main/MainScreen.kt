package com.matcher.matcher.modules.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import com.matcher.matcher.R
import com.matcher.matcher.modules.main.tabs.HomeScreen
import com.matcher.matcher.modules.main.tabs.MessagesScreen
import com.matcher.matcher.modules.main.tabs.LikesScreen
import com.matcher.matcher.modules.main.tabs.AccountScreen
import com.matcher.matcher.modules.main.components.FilterPanel
import com.matcher.matcher.models.filter.ProfileFilter
import com.matcher.matcher.models.user.User
import android.content.Intent
import androidx.compose.ui.platform.LocalContext

private enum class HomeTab(
    val title: String,
    val iconRes: Int,
    val iconSelectedRes: Int
) {
    Discover("Keşfet", R.drawable.menu_item_1, R.drawable.menu_item_1_selected),
    Chats("Sohbetler", R.drawable.menu_item_2, R.drawable.menu_item_2_selected),
    Likes("Beğeniler", R.drawable.menu_item_3, R.drawable.menu_item_3_selected),
    Profile("Profil", R.drawable.menu_item_4, R.drawable.menu_item_4_selected)
}

@Composable
fun MainScreen() {
    val selectedTab = remember { mutableStateOf(HomeTab.Discover) }
    val isFilterVisible = remember { mutableStateOf(false) }
    val currentFilter = remember { mutableStateOf(ProfileFilter.DEFAULT) }
    val isSearchVisible = remember { mutableStateOf(false) }
    val searchQuery = remember { mutableStateOf("") }
    val isLikesSearchVisible = remember { mutableStateOf(false) }
    val likesSearchQuery = remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            val (title, leftIcon, rightIcon) = when (selectedTab.value) {
                HomeTab.Discover -> Triple(HomeTab.Discover.title, R.drawable.wallet, R.drawable.filter)
                HomeTab.Chats -> Triple(HomeTab.Chats.title, R.drawable.search, null)
                HomeTab.Likes -> Triple(HomeTab.Likes.title, R.drawable.search, null)
                HomeTab.Profile -> Triple(HomeTab.Profile.title, null, null)
            }
            TopToolbar(
                title = title,
                leftIconRes = leftIcon,
                rightIconRes = rightIcon,
                onLeftClick = {
                    if (selectedTab.value == HomeTab.Discover) {
                        context.startActivity(Intent(context, com.matcher.matcher.modules.wallet.WalletActivity::class.java))
                    } else if (selectedTab.value == HomeTab.Chats) {
                        isSearchVisible.value = !isSearchVisible.value
                        if (!isSearchVisible.value) {
                            searchQuery.value = ""
                        }
                    } else if (selectedTab.value == HomeTab.Likes) {
                        val currentUser = User.current
                        if (currentUser?.isPremium() == true) {
                            isLikesSearchVisible.value = !isLikesSearchVisible.value
                            if (!isLikesSearchVisible.value) {
                                likesSearchQuery.value = ""
                            }
                        } else {
                            android.widget.Toast.makeText(context, "Arama özelliği VIP üyeler için", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onRightClick = {
                    if (selectedTab.value == HomeTab.Discover) {
                        isFilterVisible.value = !isFilterVisible.value
                    }
                }
            )
        },
        bottomBar = {
            TabBar(selected = selectedTab.value, onSelect = { selectedTab.value = it })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                FilterPanel(
                    visible = isFilterVisible.value,
                    currentFilter = currentFilter.value,
                    onApply = { filter ->
                        currentFilter.value = filter
                        isFilterVisible.value = false
                    },
                    onCancel = { isFilterVisible.value = false }
                )

                if (isSearchVisible.value && selectedTab.value == HomeTab.Chats) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery.value,
                            onValueChange = { searchQuery.value = it },
                            placeholder = { 
                                Text(
                                    "Konuşma ara...",
                                    color = Color.Gray
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Ara",
                                    tint = Color.Gray
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.value.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery.value = "" }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Temizle",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedIndicatorColor = colorResource(id = R.color.primaryColor),
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                }
                            )
                        )
                    }
                }

                if (isLikesSearchVisible.value && selectedTab.value == HomeTab.Likes && User.current?.isPremium() == true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = likesSearchQuery.value,
                            onValueChange = { likesSearchQuery.value = it },
                            placeholder = { 
                                Text(
                                    "Beğenilerde ara...",
                                    color = Color.Gray
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Ara",
                                    tint = Color.Gray
                                )
                            },
                            trailingIcon = {
                                if (likesSearchQuery.value.isNotEmpty()) {
                                    IconButton(
                                        onClick = { likesSearchQuery.value = "" }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Temizle",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedIndicatorColor = colorResource(id = R.color.primaryColor),
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                }
                            )
                        )
                    }
                }

                when (selectedTab.value) {
                    HomeTab.Discover -> HomeScreen(
                        filter = currentFilter.value
                    )
                    HomeTab.Chats -> MessagesScreen(
                        searchQuery = if (isSearchVisible.value) searchQuery.value else ""
                    )
                    HomeTab.Likes -> LikesScreen(
                        searchQuery = if (isLikesSearchVisible.value && User.current?.isPremium() == true) likesSearchQuery.value else ""
                    )
                    HomeTab.Profile -> AccountScreen()
                }
            }

        }
    }
}

@Composable
private fun TabBar(selected: HomeTab, onSelect: (HomeTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .height(64.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeTab.values().forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(
                        animateColorAsState(
                            targetValue = if (isSelected) colorResource(id = R.color.primaryColor) else Color.Transparent,
                            label = "tabBg"
                        ).value
                    )
                    .clickable { onSelect(tab) },
                contentAlignment = Alignment.Center
            ) {
                val iconOffsetY by animateDpAsState(
                    targetValue = if (isSelected) (-2).dp else 0.dp,
                    label = "iconOffset"
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = if (isSelected) tab.iconSelectedRes else tab.iconRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .offset(y = iconOffsetY)
                    )
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopToolbar(
    title: String,
    leftIconRes: Int?,
    rightIconRes: Int?,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    val shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
    Column(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.statusBarsPadding())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .shadow(elevation = 8.dp, shape = shape, clip = false)
                .clip(shape)
                .background(Color.White)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
        if (leftIconRes != null) {
            Image(
                painter = painterResource(id = leftIconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onLeftClick() }
            )
        } else {
            Box(modifier = Modifier.size(24.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        if (rightIconRes != null) {
            Image(
                painter = painterResource(id = rightIconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onRightClick() }
            )
        } else {
            Box(modifier = Modifier.size(24.dp))
        }
        }
    }
}

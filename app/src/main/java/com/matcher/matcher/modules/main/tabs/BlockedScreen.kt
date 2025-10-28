package com.matcher.matcher.modules.main.tabs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.matcher.matcher.models.profile.Profile
import com.matcher.matcher.modules.main.components.BlockedGridItemView

@Composable
fun BlockedScreen() {
    val initialProfiles = remember { listOf<Profile>() }
    val blockedProfiles = remember { mutableStateListOf<Profile>().apply { addAll(initialProfiles) } }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        items(blockedProfiles) { profile ->
            // Sahte tarih: index'e göre örnek
            val date = "12.06.2024"
            BlockedGridItemView(
                profile = profile,
                date = date,
                onUnblock = { toUnblock -> blockedProfiles.remove(toUnblock) }
            )
        }
    }
}


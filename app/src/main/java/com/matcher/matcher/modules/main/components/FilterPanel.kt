package com.matcher.matcher.modules.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.res.colorResource
import com.matcher.matcher.R
import com.matcher.matcher.models.filter.ProfileFilter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults

@Composable
fun FilterPanel(
    visible: Boolean,
    currentFilter: ProfileFilter = ProfileFilter.DEFAULT,
    onApply: (ProfileFilter) -> Unit,
    onCancel: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .padding(16.dp)
        ) {
            // Filtreler başlığı
            Text(text = "Filtreler", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Yaş aralığı
            var ageRange by remember(currentFilter) { 
                mutableStateOf(
                    (currentFilter.minAge?.toFloat() ?: ProfileFilter.MIN_AGE.toFloat())..
                    (currentFilter.maxAge?.toFloat() ?: ProfileFilter.MAX_AGE.toFloat())
                )
            }
            
            // Cinsiyet seçimi
            var selectedGender by remember(currentFilter) { 
                mutableStateOf(currentFilter.gender)
            }
            
            // Yaş aralığı gösterimi - 50 ise "50+" yaz
            val ageRangeText = if (ageRange.endInclusive.toInt() >= ProfileFilter.MAX_AGE) {
                "${ageRange.start.toInt()} - ${ProfileFilter.MAX_AGE}+"
            } else {
                "${ageRange.start.toInt()} - ${ageRange.endInclusive.toInt()}"
            }
            
            Text(
                text = "Yaş aralığı: $ageRangeText", 
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            RangeSlider(
                value = ageRange,
                onValueChange = { range ->
                    val start = range.start.coerceIn(
                        ProfileFilter.MIN_AGE.toFloat(), 
                        ProfileFilter.MAX_AGE.toFloat()
                    )
                    val end = range.endInclusive.coerceIn(
                        ProfileFilter.MIN_AGE.toFloat(), 
                        ProfileFilter.MAX_AGE.toFloat()
                    )
                    if (end >= start) {
                        ageRange = start..end
                    }
                },
                valueRange = ProfileFilter.MIN_AGE.toFloat()..ProfileFilter.MAX_AGE.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = colorResource(id = R.color.primaryColor),
                    activeTrackColor = colorResource(id = R.color.primaryColor),
                    inactiveTrackColor = Color(0xFFE5E7EB)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Cinsiyet seçimi
            Text(
                text = "Cinsiyet", 
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Hepsi seçeneği
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = selectedGender == null,
                            onClick = { selectedGender = null }
                        )
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    RadioButton(
                        selected = selectedGender == null,
                        onClick = { selectedGender = null },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colorResource(id = R.color.primaryColor)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hepsi",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // Erkek seçeneği
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = selectedGender == 1,
                            onClick = { selectedGender = 1 }
                        )
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    RadioButton(
                        selected = selectedGender == 1,
                        onClick = { selectedGender = 1 },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colorResource(id = R.color.primaryColor)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Erkek",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // Kadın seçeneği
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = selectedGender == 2,
                            onClick = { selectedGender = 2 }
                        )
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    RadioButton(
                        selected = selectedGender == 2,
                        onClick = { selectedGender = 2 },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colorResource(id = R.color.primaryColor)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Kadın",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filtreyi sıfırla butonu
                OutlinedButton(
                    onClick = { 
                        ageRange = ProfileFilter.MIN_AGE.toFloat()..ProfileFilter.MAX_AGE.toFloat()
                        selectedGender = null
                        onApply(ProfileFilter.DEFAULT)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    )
                ) {
                    Text(text = "Sıfırla")
                }
                
                Row {
                    OutlinedButton(onClick = onCancel) {
                        Text(text = "İptal")
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(
                        onClick = { 
                            val filter = ProfileFilter(
                                minAge = ageRange.start.toInt(),
                                // 50 seçildiyse null yap (50+ anlamına gelir)
                                maxAge = if (ageRange.endInclusive.toInt() >= ProfileFilter.MAX_AGE) {
                                    null
                                } else {
                                    ageRange.endInclusive.toInt()
                                },
                                gender = selectedGender
                            )
                            onApply(filter)
                        }, 
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.primaryColor)
                        )
                    ) {
                        Text(text = "Uygula", color = Color.White)
                    }
                }
            }
        }
    }
}



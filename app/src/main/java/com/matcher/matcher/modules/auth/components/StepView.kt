package com.matcher.matcher.modules.auth.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.matcher.matcher.R

@Composable
fun StepView(currentStep: Int, totalSteps: Int) {
    val activeColor = colorResource(id = R.color.primaryDarkColor)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            val isActive = index < currentStep
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp),
                color = if (isActive) activeColor else activeColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = null
            ) {}
        }
    }
}



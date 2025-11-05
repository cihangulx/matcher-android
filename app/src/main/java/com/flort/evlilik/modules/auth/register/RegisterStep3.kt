package com.flort.evlilik.modules.auth.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flort.evlilik.R
import com.flort.evlilik.modules.auth.components.Gender

@Composable
fun RegisterStep3(
    name: String,
    age: String,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    gender: Gender?,
    onGenderChange: (Gender) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Profilini tamamla",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "İsmini ve yaşını gir, seni daha iyi tanıyalım.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(text = "İsim") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.primaryColor),
                unfocusedBorderColor = colorResource(id = R.color.whiteButtonStrokeColor),
                cursorColor = colorResource(id = R.color.primaryColor),
                focusedLabelColor = colorResource(id = R.color.primaryColor),
                unfocusedLabelColor = Color.Black.copy(alpha = 0.6f),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = age,
            onValueChange = onAgeChange,
            label = { Text(text = "Yaş") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.primaryColor),
                unfocusedBorderColor = colorResource(id = R.color.whiteButtonStrokeColor),
                cursorColor = colorResource(id = R.color.primaryColor),
                focusedLabelColor = colorResource(id = R.color.primaryColor),
                unfocusedLabelColor = Color.Black.copy(alpha = 0.6f),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        val ageInt = age.toIntOrNull()
        val isUnderage = ageInt != null && ageInt < 18
        if (isUnderage) {
            Text(
                text = "18 yaşından küçükler kayıt olamaz.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD32F2F)
            )
        } else {
            Text(
                text = "En az 18 yaşında olmalısın.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Cinsiyet",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            GenderCard(
                text = "Erkek",
                selected = gender == Gender.MALE,
                onClick = { onGenderChange(Gender.MALE) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            GenderCard(
                text = "Kadın",
                selected = gender == Gender.FEMALE,
                onClick = { onGenderChange(Gender.FEMALE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GenderCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) colorResource(id = R.color.primaryColor) else colorResource(id = R.color.whiteButtonStrokeColor)
    val bgColor = if (selected) colorResource(id = R.color.primaryColor).copy(alpha = 0.08f) else Color.White
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
        }
    }
}



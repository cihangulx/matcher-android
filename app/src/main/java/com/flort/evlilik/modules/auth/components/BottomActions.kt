package com.flort.evlilik.modules.auth.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flort.evlilik.R
import com.flort.evlilik.modules.terms.TermsActivity
import com.flort.evlilik.network.Routes

@Composable
fun BottomActions(
    context: android.content.Context? = null,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    isLoading: Boolean = false,
    continueButtonText: String = "Devam Et"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBack,
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colorResource(id = R.color.whiteButtonStrokeColor)),
                modifier = Modifier.height(60.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(60.dp)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.left_arrow),
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
            ) {
                Button(
                    onClick = onContinue,
                    enabled = !isLoading,
                    modifier = Modifier.matchParentSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.primaryDarkColor),
                        contentColor = Color.White,
                        disabledContainerColor = colorResource(id = R.color.primaryDarkColor).copy(alpha = 0.6f),
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(text = continueButtonText)
                    }
                }
                Image(
                    painter = painterResource(id = R.drawable.right_arrow_white),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val linkColor = colorResource(id = R.color.primaryDarkColor)
        val terms = stringResource(id = R.string.terms_of_use)
        val privacy = stringResource(id = R.string.privacy_policy)
        val consent = stringResource(id = R.string.consent_text, terms, privacy)

        val annotatedText = buildAnnotatedString {
            val termsStart = consent.indexOf(terms)
            val privacyStart = consent.indexOf(privacy)
            append(consent)
            if (termsStart >= 0) {
                addStyle(SpanStyle(color = linkColor), termsStart, termsStart + terms.length)
                addStringAnnotation(tag = "TERMS", annotation = "terms", start = termsStart, end = termsStart + terms.length)
            }
            if (privacyStart >= 0) {
                addStyle(SpanStyle(color = linkColor), privacyStart, privacyStart + privacy.length)
                addStringAnnotation(tag = "PRIVACY", annotation = "privacy", start = privacyStart, end = privacyStart + privacy.length)
            }
        }

        ClickableText(
            text = annotatedText,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.Black,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth(),
            onClick = { offset ->
                annotatedText.getStringAnnotations(
                    tag = "TERMS",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let {
                    context?.let { ctx ->
                        TermsActivity.start(
                            context = ctx,
                            url = Routes.BASE_URL + Routes.TERMS,
                            title = "Kullanım Sözleşmesi"
                        )
                    }
                }
                
                annotatedText.getStringAnnotations(
                    tag = "PRIVACY",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let {
                    context?.let { ctx ->
                        TermsActivity.start(
                            context = ctx,
                            url = Routes.BASE_URL + Routes.PRIVACY,
                            title = "Gizlilik Politikası"
                        )
                    }
                }
            }
        )
    }
}

package com.digifello.tutordesk.ui.Screens.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink

private const val PRIVACY_URL = "https://tutordesk.digifello.dev/"

@Composable
fun PrivacyAgreementText(
    textColor: Color,
    linkColor: Color,
    modifier: Modifier = Modifier
) {
    val annotated = buildAnnotatedString {
        append("By continuing, you agree to our ")
        withLink(
            LinkAnnotation.Url(
                PRIVACY_URL,
                TextLinkStyles(
                    SpanStyle(
                        color = linkColor,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) { append("Privacy Policy") }
        append(".")
    }
    Text(
        text = annotated,
        style = MaterialTheme.typography.bodySmall,
        color = textColor,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}
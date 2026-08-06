package com.digifello.tutordesk.ui.Screens.more

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.digifello.tutordesk.ui.theme.CardWhite
import com.digifello.tutordesk.ui.theme.CardWhiteDark
import com.digifello.tutordesk.ui.theme.ChalkWhite
import com.digifello.tutordesk.ui.theme.ChalkWhiteDark
import com.digifello.tutordesk.ui.theme.Ink_Navy
import com.digifello.tutordesk.ui.theme.Ink_NavyDark
import com.digifello.tutordesk.ui.theme.SlateBlue
import com.digifello.tutordesk.ui.theme.SlateBlueDark

private data class PolicySection(val title: String, val body: String)

private val privacySections = listOf(
    PolicySection(
        "What we store",
        "TutorDesk stores the batches, students, and payment records you enter — names, phone numbers, class schedules, and fee amounts — along with your account email for sign-in."
    ),
    PolicySection(
        "How your data is used",
        "Your data is used only to run the app for you: showing your batches, students, and payment history. We do not sell your data or share it with third parties for advertising."
    ),
    PolicySection(
        "Where it's stored",
        "Data is stored securely with our cloud backend and is only accessible from your signed-in account."
    ),
    PolicySection(
        "Your students' data",
        "As a tutor, you're responsible for the student information you enter — names and phone numbers of your students. Please only add information you have the right to store, and remove student records if a student or parent asks you to."
    ),
    PolicySection(
        "Contact",
        "For questions about your data or this policy, reach out to digifello.official@gmail.com."
    )
)

private val termsSections = listOf(
    PolicySection(
        "Using TutorDesk",
        "TutorDesk is provided to help you manage your tutoring batches, students, and payments. You agree to use it only for lawful purposes and to keep your account credentials secure."
    ),
    PolicySection(
        "Accuracy of data",
        "You're responsible for the accuracy of the batch, student, and payment information you enter. TutorDesk helps you track this data but isn't liable for decisions made based on it."
    ),
    PolicySection(
        "Changes to the service",
        "Features may be added, changed, or removed over time as the app improves."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) ChalkWhiteDark else ChalkWhite
    val cardColor = if (isDark) CardWhiteDark else CardWhite
    val textColor = if (isDark) Ink_NavyDark else Ink_Navy
    val secondaryColor = if (isDark) SlateBlueDark else SlateBlue

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Terms & Privacy", color = textColor, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(privacySections) { section ->
                PolicyCard(section, cardColor, textColor, secondaryColor)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Terms of Use",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(termsSections) { section ->
                PolicyCard(section, cardColor, textColor, secondaryColor)
            }
        }
    }
}

@Composable
private fun PolicyCard(
    section: PolicySection,
    cardColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = section.body,
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryColor
            )
        }
    }
}
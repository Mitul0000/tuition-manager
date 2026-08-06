package com.digifello.tutordesk.ui.Screens.questionGenerator
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent
import com.digifello.tutordesk.data.model.GeneratedQuestions
import com.digifello.tutordesk.data.model.McqQuestion
import com.digifello.tutordesk.data.model.SelectedFile
import com.digifello.tutordesk.data.model.SubjectiveQuestion
import com.digifello.tutordesk.ui.theme.TutorDeskTheme
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionGeneratorScreen(
    viewModel: QuestionGeneratorViewModel = viewModel(),
    onBack: () -> Unit = {}
) {

    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState.stage,
            transitionSpec = {
                (slideInHorizontally(animationSpec = tween(300)) { it / 4 } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(animationSpec = tween(300)) { -it / 4 } + fadeOut(tween(200)))
            },
            label = "stage_transition",
            modifier = Modifier.fillMaxSize()
        ) { stage ->
            when (stage) {
                GenerationStage.API_KEY -> ApiKeyStageContent(uiState, viewModel)
                GenerationStage.UPLOAD -> UploadStageContent(uiState, viewModel)
                GenerationStage.PROCESSING -> ProcessingStageContent(uiState)
                GenerationStage.RESULT -> ResultStageContent(uiState, viewModel)
                GenerationStage.PAPER_SETUP -> PaperSetupStageContent(uiState, viewModel)
                GenerationStage.ERROR -> ErrorStageContent(uiState, viewModel)
            }
        }
    }
}


@Composable
private fun ChangeApiKeyRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Key,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Change API Key",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium
        )
    }
}

// STAGE: API KEY


@Composable
private fun ApiKeyStageContent(
    uiState: QuestionGeneratorUiState,
    viewModel: QuestionGeneratorViewModel
) {
    var showKey by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Enter your Gemini API key to generate questions.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "It's stored securely on this device only and never leaves it except to talk to Google's API.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = uiState.apiKeyInput,
            onValueChange = viewModel::onApiKeyInputChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Gemini API Key") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showKey = !showKey }) {
                    Icon(
                        imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showKey) "Hide key" else "Show key"
                    )
                }
            },
            isError = uiState.errorMessage != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = uiState.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = viewModel::saveApiKey,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Save & Continue", fontWeight = FontWeight.SemiBold)
        }

        if (uiState.isEditingApiKey) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = viewModel::cancelChangeApiKey) {
                Text("Cancel")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Don't have a key? Get one from Google AI Studio",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

// STAGE: UPLOAD

@Composable
private fun UploadStageContent(
    uiState: QuestionGeneratorUiState,
    viewModel: QuestionGeneratorViewModel
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> if (uris.isNotEmpty()) viewModel.onFilesSelected(uris) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (uiState.hasApiKey) {
            ChangeApiKeyRow(onClick = viewModel::startChangeApiKey)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { filePickerLauncher.launch(arrayOf("application/pdf", "image/*")) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to upload study material",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "PDFs and images supported",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (uiState.selectedFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.selectedFiles.forEach { file ->
                    SelectedFileRow(file = file, onRemove = { viewModel.removeFile(file) })
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Requirements",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CountField("MCQ", uiState.mcqCountText, viewModel::onMcqCountChange, Modifier.weight(1f))
                    CountField("Short", uiState.shortCountText, viewModel::onShortCountChange, Modifier.weight(1f))
                    CountField("Long", uiState.longCountText, viewModel::onLongCountChange, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.language,
                    onValueChange = viewModel::onLanguageChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Language") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.syllabus,
                    onValueChange = viewModel::onSyllabusChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                    label = { Text("Syllabus / topics (optional)") },
                    placeholder = { Text("e.g. Chapter 3–5, only algebra topics") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            ErrorBanner(message = uiState.errorMessage)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = viewModel::generateQuestions,
            enabled = uiState.canGenerate,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
        ) {
            Text("Generate Questions", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CountField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun SelectedFileRow(file: SelectedFile, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(file.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                Text(formatBytes(file.sizeBytes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove file", tint = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

private fun formatBytes(bytes: Long): String {
    val kb = bytes / 1024.0
    if (kb < 1024) return "${kb.roundToInt()} KB"
    return "%.1f MB".format(kb / 1024.0)
}

// STAGE: PROCESSING

@Composable
private fun ProcessingStageContent(uiState: QuestionGeneratorUiState) {
    val animatedProgress by animateFloatAsState(
        targetValue = uiState.progress,
        animationSpec = tween(durationMillis = 500),
        label = "progress_anim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "processing_pulse")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(2200, easing = LinearEasing)),
        label = "rotation"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier.size(96.dp),
                strokeWidth = 6.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp).rotate(rotation)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        AnimatedContent(
            targetState = uiState.currentStepIndex,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "step_label"
        ) {
            Text(
                text = uiState.currentStepLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.fillMaxWidth(0.7f).height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "This can take a minute for larger documents",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}


// STAGE: RESULT

private enum class ResultTab(val label: String) { MCQ("MCQ"), SHORT("Short"), LONG("Long") }

// Selection now lives directly on the Result screen: as soon as questions are generated,
// they're already checkbox-selectable. Only two actions follow: Regenerate or Create Paper.
@Composable
private fun ResultStageContent(
    uiState: QuestionGeneratorUiState,
    viewModel: QuestionGeneratorViewModel
) {
    val result = uiState.result
    var selectedTab by remember { mutableStateOf(ResultTab.MCQ) }

    if (result == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No results yet.", color = MaterialTheme.colorScheme.secondary)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Select the questions to include in your paper",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${uiState.selectedMcqIndices.size + uiState.selectedShortIndices.size + uiState.selectedLongIndices.size} selected · ${uiState.selectedTotalMarks} marks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            ResultTab.entries.forEach { tab ->
                Tab(selected = selectedTab == tab, onClick = { selectedTab = tab }, text = { Text(tab.label) })
            }
        }

        val (allSelected, type) = when (selectedTab) {
            ResultTab.MCQ -> (result.mcq.isNotEmpty() && uiState.selectedMcqIndices.size == result.mcq.size) to ResultQuestionType.MCQ
            ResultTab.SHORT -> (result.shortAnswer.isNotEmpty() && uiState.selectedShortIndices.size == result.shortAnswer.size) to ResultQuestionType.SHORT
            ResultTab.LONG -> (result.longAnswer.isNotEmpty() && uiState.selectedLongIndices.size == result.longAnswer.size) to ResultQuestionType.LONG
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                if (allSelected) viewModel.clearAllOfType(type) else viewModel.selectAllOfType(type)
            }) {
                Text(if (allSelected) "Clear all" else "Select all")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (selectedTab) {
                ResultTab.MCQ -> itemsIndexed(result.mcq) { index, q ->
                    SelectableMcqCard(q, index in uiState.selectedMcqIndices) { viewModel.toggleMcqSelected(index) }
                }
                ResultTab.SHORT -> itemsIndexed(result.shortAnswer) { index, q ->
                    SelectableSubjectiveCard(q, index in uiState.selectedShortIndices) { viewModel.toggleShortSelected(index) }
                }
                ResultTab.LONG -> itemsIndexed(result.longAnswer) { index, q ->
                    SelectableSubjectiveCard(q, index in uiState.selectedLongIndices) { viewModel.toggleLongSelected(index) }
                }
            }
        }

        if (uiState.errorMessage != null) {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ErrorBanner(message = uiState.errorMessage)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Button(
                onClick = viewModel::proceedToPaperSetup,
                enabled = uiState.hasAnySelection,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Create Question Paper", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = viewModel::regenerateQuestions,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Regenerate Questions")
            }
        }
    }
}

@Composable
private fun SelectableMcqCard(q: McqQuestion, selected: Boolean, onToggle: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().clickable { onToggle() }
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Checkbox(checked = selected, onCheckedChange = { onToggle() })
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(q.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                q.options.forEachIndexed { index, option ->
                    val letter = 'A' + index
                    Text(
                        text = "$letter. $option",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (option == q.answer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            MarksBadge(q.marks)
        }
    }
}

@Composable
private fun SelectableSubjectiveCard(q: SubjectiveQuestion, selected: Boolean, onToggle: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().clickable { onToggle() }
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Checkbox(checked = selected, onCheckedChange = { onToggle() })
            Spacer(modifier = Modifier.width(4.dp))
            Text(q.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            MarksBadge(q.marks)
        }
    }
}

// STAGE: PAPER SETUP

@Composable
private fun PaperSetupStageContent(
    uiState: QuestionGeneratorUiState,
    viewModel: QuestionGeneratorViewModel
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Paper details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${uiState.selectedMcqIndices.size + uiState.selectedShortIndices.size + uiState.selectedLongIndices.size} questions selected",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.paperTitle,
            onValueChange = viewModel::onPaperTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Paper title (for My Papers)") },
            placeholder = { Text("e.g. Algebra Unit Test") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.institutionName,
            onValueChange = viewModel::onInstitutionNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Institution name *") },
            singleLine = true,
            isError = uiState.errorMessage != null && uiState.institutionName.isBlank(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.className,
            onValueChange = viewModel::onClassNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Class / Section") },
            placeholder = { Text("e.g. Class 10 - B") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.timeAllowed,
                onValueChange = viewModel::onTimeAllowedChange,
                modifier = Modifier.weight(1f),
                label = { Text("Time allowed") },
                placeholder = { Text("e.g. 2 hrs") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = uiState.totalMarksText,
                onValueChange = viewModel::onTotalMarksChange,
                modifier = Modifier.weight(1f),
                label = { Text("Total marks") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Watermark", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(
                        text = "Overlay institution name faintly across the paper",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = uiState.watermarkEnabled,
                    onCheckedChange = viewModel::onWatermarkToggle,
                    modifier = Modifier.scale(0.85f)
                )
            }
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            ErrorBanner(message = uiState.errorMessage)
        }

        if (uiState.paperInfoMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Text(
                    text = uiState.paperInfoMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = viewModel::generatePdf,
            enabled = uiState.canGeneratePaper && !uiState.isGeneratingPdf,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (uiState.isGeneratingPdf) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Download Question Paper (PDF)", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = viewModel::generateAnswerSheet,
            enabled = uiState.canGeneratePaper && !uiState.isGeneratingAnswerSheet,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isGeneratingAnswerSheet) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Download Answer Key (PDF)")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = viewModel::savePaperToLibrary,
            enabled = uiState.canGeneratePaper && !uiState.isSavingPaper,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isSavingPaper) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Save to My Papers")
            }
        }

        if (uiState.lastDownloadedUri != null) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    val shareUri = uiState.lastDownloadedUri
                    if (shareUri != null) {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, shareUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(
                            Intent.createChooser(shareIntent, "Share ${uiState.lastDownloadedFileName ?: "PDF"}")
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share ${uiState.lastDownloadedFileName ?: "PDF"}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = viewModel::backToResultFromPaperSetup, modifier = Modifier.fillMaxWidth()) {
            Text("Back to results")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun McqCard(q: McqQuestion) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(q.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                MarksBadge(q.marks)
            }
            Spacer(modifier = Modifier.height(10.dp))
            q.options.forEachIndexed { index, option ->
                val letter = 'A' + index
                val isCorrect = option == q.answer
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$letter.",
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.width(24.dp)
                    )
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCorrect) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectiveCard(q: SubjectiveQuestion) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(q.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                MarksBadge(q.marks)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp)) {
                Text(if (expanded) "Hide answer" else "Show answer")
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Text(
                    text = q.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun MarksBadge(marks: Int) {
    Box(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$marks pt${if (marks == 1) "" else "s"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// STAGE: ERROR

@Composable
private fun ErrorStageContent(
    uiState: QuestionGeneratorUiState,
    viewModel: QuestionGeneratorViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = uiState.errorMessage ?: "Something went wrong.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = viewModel::dismissError,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Try Again", fontWeight = FontWeight.SemiBold)
        }
    }
}

// PREVIEWS — light & dark, so you can eyeball both themes in Android Studio

private val fakeFiles = listOf(
    SelectedFile(uri = Uri.EMPTY, name = "Chapter_3_Algebra.pdf", sizeBytes = 1_240_000, mimeType = "application/pdf"),
    SelectedFile(uri = Uri.EMPTY, name = "notes_scan.jpg", sizeBytes = 340_000, mimeType = "image/jpeg")
)

private val fakeUploadState = QuestionGeneratorUiState(
    stage = GenerationStage.UPLOAD,
    hasApiKey = true,
    selectedFiles = fakeFiles,
    mcqCountText = "10",
    shortCountText = "5",
    longCountText = "2",
    language = "English",
    syllabus = "Chapter 3–5, only algebra topics"
)

private val fakeResult = GeneratedQuestions(
    mcq = listOf(
        McqQuestion("What is the value of x in 2x + 4 = 10?", listOf("2", "3", "4", "5"), "3", 1),
        McqQuestion("Which of these is a quadratic equation?", listOf("x + 2 = 0", "x^2 + 2x + 1 = 0", "2x = 4", "x/2 = 1"), "x^2 + 2x + 1 = 0", 1)
    ),
    shortAnswer = listOf(
        SubjectiveQuestion("Explain the difference between a linear and quadratic equation.", "A linear equation has degree 1; a quadratic has degree 2 and graphs as a parabola.", 3)
    ),
    longAnswer = listOf(
        SubjectiveQuestion("Derive the quadratic formula from ax^2 + bx + c = 0.", "Completing the square gives x = (-b ± √(b²-4ac)) / 2a.", 5)
    )
)

private val fakeResultState = QuestionGeneratorUiState(stage = GenerationStage.RESULT, hasApiKey = true, result = fakeResult)
private val fakeProcessingState = QuestionGeneratorUiState(stage = GenerationStage.PROCESSING, currentStepLabel = "Drafting MCQs, short & long answers…", progress = 0.62f)

@Preview(showBackground = true, name = "Upload - Light")
@Composable
private fun UploadPreviewLight() {
    TutorDeskTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            UploadStageContent(uiState = fakeUploadState, viewModel = viewModel())
        }
    }
}

@Preview(showBackground = true, name = "Upload - Dark")
@Composable
private fun UploadPreviewDark() {
    TutorDeskTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            UploadStageContent(uiState = fakeUploadState, viewModel = viewModel())
        }
    }
}

@Preview(showBackground = true, name = "Processing - Light")
@Composable
private fun ProcessingPreviewLight() {
    TutorDeskTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ProcessingStageContent(uiState = fakeProcessingState)
        }
    }
}

@Preview(showBackground = true, name = "Result - Light")
@Composable
private fun ResultPreviewLight() {
    TutorDeskTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ResultStageContent(uiState = fakeResultState, viewModel = viewModel())
        }
    }
}

@Preview(showBackground = true, name = "Result - Dark")
@Composable
private fun ResultPreviewDark() {
    TutorDeskTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ResultStageContent(uiState = fakeResultState, viewModel = viewModel())
        }
    }
}
package com.digifello.tutordesk.ui.Screens.questionGenerator

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tutordesk.data.model.GenerateRequirements
import com.digifello.tutordesk.data.model.GeneratedQuestions
import com.digifello.tutordesk.data.model.SelectedFile
import com.digifello.tutordesk.data.repository.ApiKeyRepository
import com.digifello.tutordesk.data.repository.QuestionGeneratorRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.digifello.tutordesk.data.model.McqQuestion
import com.digifello.tutordesk.data.model.PaperMetadata
import com.digifello.tutordesk.data.model.SelectedQuestionsPayload
import com.digifello.tutordesk.data.model.SubjectiveQuestion

enum class GenerationStage {
    API_KEY,
    UPLOAD,
    PROCESSING,
    RESULT,
    PAPER_SETUP,
    ERROR
}

data class ProcessingStep(val label: String, val durationMs: Long)

enum class ResultQuestionType { MCQ, SHORT, LONG }

val QUESTION_GENERATION_STEPS = listOf(
    ProcessingStep("Uploading your documents…", 1200),
    ProcessingStep("Extracting text from files…", 3200),
    ProcessingStep("Understanding document structure…", 4800),
    ProcessingStep("Selecting the best generation strategy…", 4500),
    ProcessingStep("Scanning content for relevant topics…", 5400),
    ProcessingStep("Drafting MCQs, short & long answers…", 6200),
    ProcessingStep("Polishing your questions…", 2500)
)

data class QuestionGeneratorUiState(
    val stage: GenerationStage = GenerationStage.UPLOAD,

    //API key
    val hasApiKey: Boolean = false,
    val apiKeyInput: String = "",
    val isEditingApiKey: Boolean = false,

    //Upload section
    val selectedFiles: List<SelectedFile> = emptyList(),
    val mcqCountText: String = "5",
    val shortCountText: String = "3",
    val longCountText: String = "2",
    val language: String = "English",
    val syllabus: String = "",

    // Processing section
    val currentStepIndex: Int = 0,
    val currentStepLabel: String = "",
    val progress: Float = 0f,

// Result
    val result: GeneratedQuestions? = null,
    val sessionId: String? = null,

    //Selection (which generated questions go into the final paper)
    val selectedMcqIndices: Set<Int> = emptySet(),
    val selectedShortIndices: Set<Int> = emptySet(),
    val selectedLongIndices: Set<Int> = emptySet(),

    //Paper setup (metadata + watermark)
    val paperTitle: String = "",
    val institutionName: String = "",
    val className: String = "",
    val timeAllowed: String = "",
    val totalMarksText: String = "",
    val watermarkEnabled: Boolean = false,
// Paper generation status
    val isGeneratingPdf: Boolean = false,
    val isGeneratingAnswerSheet: Boolean = false,
    val isSavingPaper: Boolean = false,
    val lastDownloadedUri: Uri? = null,
    val lastDownloadedFileName: String? = null,
    val paperInfoMessage: String? = null,

    val errorMessage: String? = null
) {
    val canGenerate: Boolean
        get() = selectedFiles.isNotEmpty() &&
                (mcqCountText.toIntOrNull() ?: 0) +
                (shortCountText.toIntOrNull() ?: 0) +
                (longCountText.toIntOrNull() ?: 0) > 0

    val hasAnySelection: Boolean
        get() = selectedMcqIndices.isNotEmpty() || selectedShortIndices.isNotEmpty() || selectedLongIndices.isNotEmpty()

    val selectedTotalMarks: Int
        get() {
            val r = result ?: return 0
            val mcqMarks = selectedMcqIndices.sumOf { i -> r.mcq.getOrNull(i)?.marks ?: 0 }
            val shortMarks = selectedShortIndices.sumOf { i -> r.shortAnswer.getOrNull(i)?.marks ?: 0 }
            val longMarks = selectedLongIndices.sumOf { i -> r.longAnswer.getOrNull(i)?.marks ?: 0 }
            return mcqMarks + shortMarks + longMarks
        }

    val canGeneratePaper: Boolean
        get() = hasAnySelection && institutionName.isNotBlank()
}

class QuestionGeneratorViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: QuestionGeneratorRepository = QuestionGeneratorRepository(),
    private val apiKeyRepository: ApiKeyRepository = ApiKeyRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(QuestionGeneratorUiState())
    val uiState: StateFlow<QuestionGeneratorUiState> = _uiState.asStateFlow()

    private var progressJob: Job? = null

    init {
        val hasKey = apiKeyRepository.hasApiKey()
        _uiState.value = _uiState.value.copy(
            hasApiKey = hasKey,
            stage = if (hasKey) GenerationStage.UPLOAD else GenerationStage.API_KEY
        )
    }

    // API key actions

    fun onApiKeyInputChange(value: String) {
        _uiState.value = _uiState.value.copy(apiKeyInput = value, errorMessage = null)
    }

    fun saveApiKey() {
        val key = _uiState.value.apiKeyInput.trim()
        if (key.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your Gemini API key")
            return
        }
        apiKeyRepository.saveGeminiApiKey(key)
        _uiState.value = _uiState.value.copy(
            hasApiKey = true,
            isEditingApiKey = false,
            apiKeyInput = "",
            stage = GenerationStage.UPLOAD,
            errorMessage = null
        )
    }

    fun startChangeApiKey() {
        _uiState.value = _uiState.value.copy(
            isEditingApiKey = true,
            apiKeyInput = "",
            stage = GenerationStage.API_KEY,
            errorMessage = null
        )
    }

    fun cancelChangeApiKey() {
        _uiState.value = _uiState.value.copy(
            isEditingApiKey = false,
            apiKeyInput = "",
            stage = if (_uiState.value.hasApiKey) GenerationStage.UPLOAD else GenerationStage.API_KEY,
            errorMessage = null
        )
    }

    fun clearApiKey() {
        apiKeyRepository.clearGeminiApiKey()
        _uiState.value = _uiState.value.copy(
            hasApiKey = false,
            isEditingApiKey = false,
            apiKeyInput = "",
            stage = GenerationStage.API_KEY
        )
    }

    //Upload stage actions

    fun onFilesSelected(uris: List<Uri>) {
        val resolver = getApplication<Application>().contentResolver
        val newFiles = uris.mapNotNull { uri -> SelectedFile.fromUri(resolver, uri) }
        val merged = (_uiState.value.selectedFiles + newFiles).distinctBy { it.uri }
        _uiState.value = _uiState.value.copy(selectedFiles = merged, errorMessage = null)
    }

    fun removeFile(file: SelectedFile) {
        _uiState.value = _uiState.value.copy(selectedFiles = _uiState.value.selectedFiles - file)
    }

    fun onMcqCountChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(mcqCountText = value, errorMessage = null)
        }
    }

    fun onShortCountChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(shortCountText = value, errorMessage = null)
        }
    }

    fun onLongCountChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(longCountText = value, errorMessage = null)
        }
    }

    fun onLanguageChange(value: String) {
        _uiState.value = _uiState.value.copy(language = value)
    }

    fun onSyllabusChange(value: String) {
        _uiState.value = _uiState.value.copy(syllabus = value)
    }

    //Generate / Regenerate ---

    fun generateQuestions() {
        val state = _uiState.value
        val apiKey = apiKeyRepository.getGeminiApiKey()

        if (apiKey.isNullOrBlank()) {
            _uiState.value = state.copy(stage = GenerationStage.API_KEY, errorMessage = "Add your Gemini API key to continue")
            return
        }
        if (state.selectedFiles.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Please upload at least one file")
            return
        }
        if (!state.canGenerate) {
            _uiState.value = state.copy(errorMessage = "Add at least one question count (MCQ / Short / Long)")
            return
        }

        _uiState.value = state.copy(
            stage = GenerationStage.PROCESSING,
            currentStepIndex = 0,
            progress = 0f,
            errorMessage = null
        )
        startFakeProgress()

        viewModelScope.launch {
            try {
                val requirements = buildRequirements(state)
                val resolver = getApplication<Application>().contentResolver

                val response = repository.generateQuestions(
                    contentResolver = resolver,
                    files = state.selectedFiles,
                    requirements = requirements,
                    apiKey = apiKey
                )

                progressJob?.cancel()
                _uiState.value = _uiState.value.copy(
                    stage = GenerationStage.RESULT,
                    progress = 1f,
                    result = response.questions,
                    sessionId = response.sessionId,
                    selectedMcqIndices = emptySet(),
                    selectedShortIndices = emptySet(),
                    selectedLongIndices = emptySet()
                )
            } catch (e: Exception) {
                progressJob?.cancel()
                _uiState.value = _uiState.value.copy(
                    stage = GenerationStage.ERROR,
                    errorMessage = e.message ?: "Something went wrong while generating questions"
                )
            }
        }
    }

    fun regenerateQuestions() {
        val state = _uiState.value
        val apiKey = apiKeyRepository.getGeminiApiKey()

        if (apiKey.isNullOrBlank()) {
            _uiState.value = state.copy(stage = GenerationStage.API_KEY, errorMessage = "Add your Gemini API key to continue")
            return
        }

        val sessionId = state.sessionId
        if (sessionId == null) {
            generateQuestions()
            return
        }

        _uiState.value = state.copy(
            stage = GenerationStage.PROCESSING,
            currentStepIndex = 0,
            progress = 0f,
            errorMessage = null
        )
        startFakeProgress()

        viewModelScope.launch {
            try {
                val requirements = buildRequirements(state)
                val response = repository.regenerateQuestions(sessionId, requirements, apiKey)

                progressJob?.cancel()
                _uiState.value = _uiState.value.copy(
                    stage = GenerationStage.RESULT,
                    progress = 1f,
                    result = response.questions,
                    sessionId = response.sessionId,
                    selectedMcqIndices = emptySet(),
                    selectedShortIndices = emptySet(),
                    selectedLongIndices = emptySet()
                )
            } catch (e: Exception) {
                progressJob?.cancel()
                _uiState.value = _uiState.value.copy(
                    stage = GenerationStage.ERROR,
                    errorMessage = e.message ?: "Something went wrong while regenerating questions"
                )
            }
        }
    }
    //Selection stage actions



    fun toggleMcqSelected(index: Int) {
        val current = _uiState.value.selectedMcqIndices
        val updated = if (index in current) current - index else current + index
        _uiState.value = _uiState.value.copy(selectedMcqIndices = updated)
    }

    fun toggleShortSelected(index: Int) {
        val current = _uiState.value.selectedShortIndices
        val updated = if (index in current) current - index else current + index
        _uiState.value = _uiState.value.copy(selectedShortIndices = updated)
    }

    fun toggleLongSelected(index: Int) {
        val current = _uiState.value.selectedLongIndices
        val updated = if (index in current) current - index else current + index
        _uiState.value = _uiState.value.copy(selectedLongIndices = updated)
    }

    fun selectAllOfType(type: ResultQuestionType) {
        val result = _uiState.value.result ?: return
        _uiState.value = when (type) {
            ResultQuestionType.MCQ -> _uiState.value.copy(selectedMcqIndices = result.mcq.indices.toSet())
            ResultQuestionType.SHORT -> _uiState.value.copy(selectedShortIndices = result.shortAnswer.indices.toSet())
            ResultQuestionType.LONG -> _uiState.value.copy(selectedLongIndices = result.longAnswer.indices.toSet())
        }
    }

    fun clearAllOfType(type: ResultQuestionType) {
        _uiState.value = when (type) {
            ResultQuestionType.MCQ -> _uiState.value.copy(selectedMcqIndices = emptySet())
            ResultQuestionType.SHORT -> _uiState.value.copy(selectedShortIndices = emptySet())
            ResultQuestionType.LONG -> _uiState.value.copy(selectedLongIndices = emptySet())
        }
    }

    fun proceedToPaperSetup() {
        if (!_uiState.value.hasAnySelection) {
            _uiState.value = _uiState.value.copy(errorMessage = "Select at least one question")
            return
        }
        val suggestedMarks = _uiState.value.selectedTotalMarks
        _uiState.value = _uiState.value.copy(
            stage = GenerationStage.PAPER_SETUP,
            totalMarksText = if (_uiState.value.totalMarksText.isBlank()) suggestedMarks.toString() else _uiState.value.totalMarksText,
            errorMessage = null
        )
    }

    fun backToResultFromPaperSetup() {
        _uiState.value = _uiState.value.copy(stage = GenerationStage.RESULT, errorMessage = null, paperInfoMessage = null)
    }

    //Paper setup stage actions

    fun onPaperTitleChange(value: String) { _uiState.value = _uiState.value.copy(paperTitle = value) }
    fun onInstitutionNameChange(value: String) { _uiState.value = _uiState.value.copy(institutionName = value, errorMessage = null) }
    fun onClassNameChange(value: String) { _uiState.value = _uiState.value.copy(className = value) }
    fun onTimeAllowedChange(value: String) { _uiState.value = _uiState.value.copy(timeAllowed = value) }
    fun onTotalMarksChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(totalMarksText = value)
        }
    }
    fun onWatermarkToggle(enabled: Boolean) { _uiState.value = _uiState.value.copy(watermarkEnabled = enabled) }

    private fun buildSelectedQuestionsPayload(): SelectedQuestionsPayload {
        val state = _uiState.value
        val result = state.result ?: return SelectedQuestionsPayload()
        return SelectedQuestionsPayload(
            mcq = state.selectedMcqIndices.sorted().mapNotNull { result.mcq.getOrNull(it) },
            shortAnswer = state.selectedShortIndices.sorted().mapNotNull { result.shortAnswer.getOrNull(it) },
            longAnswer = state.selectedLongIndices.sorted().mapNotNull { result.longAnswer.getOrNull(it) },
            fillInTheBlank = emptyList()
        )
    }

    private fun buildMetadata(): PaperMetadata {
        val state = _uiState.value
        return PaperMetadata(
            institutionName = state.institutionName.trim(),
            className = state.className.trim(),
            timeAllowed = state.timeAllowed.trim(),
            totalMarks = state.totalMarksText.toIntOrNull() ?: state.selectedTotalMarks,
            watermark = state.watermarkEnabled
        )
    }

    fun generatePdf() {
        val state = _uiState.value
        if (!state.canGeneratePaper) {
            _uiState.value = state.copy(errorMessage = "Add an institution name and select at least one question")
            return
        }
        val fileName = "question-paper.pdf"
        _uiState.value = state.copy(isGeneratingPdf = true, errorMessage = null, paperInfoMessage = null)
        viewModelScope.launch {
            try {
                val uri = repository.generatePdf(
                    context = getApplication(),
                    selectedQuestions = buildSelectedQuestionsPayload(),
                    metadata = buildMetadata(),
                    fileName = fileName
                )
                _uiState.value = _uiState.value.copy(
                    isGeneratingPdf = false,
                    lastDownloadedUri = uri,
                    lastDownloadedFileName = fileName,
                    paperInfoMessage = "Question paper saved to Downloads"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingPdf = false,
                    errorMessage = e.message ?: "Failed to generate the question paper"
                )
            }
        }
    }

    fun generateAnswerSheet() {
        val state = _uiState.value
        if (!state.canGeneratePaper) {
            _uiState.value = state.copy(errorMessage = "Add an institution name and select at least one question")
            return
        }
        val fileName = "answer-key.pdf"
        _uiState.value = state.copy(isGeneratingAnswerSheet = true, errorMessage = null, paperInfoMessage = null)
        viewModelScope.launch {
            try {
                val uri = repository.generateAnswerSheet(
                    context = getApplication(),
                    selectedQuestions = buildSelectedQuestionsPayload(),
                    metadata = buildMetadata(),
                    fileName = fileName
                )
                _uiState.value = _uiState.value.copy(
                    isGeneratingAnswerSheet = false,
                    lastDownloadedUri = uri,
                    lastDownloadedFileName = fileName,
                    paperInfoMessage = "Answer key saved to Downloads"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingAnswerSheet = false,
                    errorMessage = e.message ?: "Failed to generate the answer sheet"
                )
            }
        }
    }

    fun savePaperToLibrary() {
        val state = _uiState.value
        if (!state.canGeneratePaper) {
            _uiState.value = state.copy(errorMessage = "Add an institution name and select at least one question")
            return
        }
        val title = state.paperTitle.ifBlank { state.institutionName.ifBlank { "Untitled Paper" } }
        _uiState.value = state.copy(isSavingPaper = true, errorMessage = null, paperInfoMessage = null)
        viewModelScope.launch {
            try {
                repository.savePaper(
                    title = title,
                    metadata = buildMetadata(),
                    selectedQuestions = buildSelectedQuestionsPayload()
                )
                _uiState.value = _uiState.value.copy(
                    isSavingPaper = false,
                    paperInfoMessage = "Paper saved to My Papers"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSavingPaper = false,
                    errorMessage = e.message ?: "Failed to save the paper"
                )
            }
        }
    }

    fun dismissPaperInfoMessage() {
        _uiState.value = _uiState.value.copy(paperInfoMessage = null)
    }
    fun startOver() {
        val sessionId = _uiState.value.sessionId
        if (sessionId != null) {
            viewModelScope.launch { runCatching { repository.deleteSession(sessionId) } }
        }
        progressJob?.cancel()
        _uiState.value = QuestionGeneratorUiState(
            hasApiKey = apiKeyRepository.hasApiKey(),
            stage = if (apiKeyRepository.hasApiKey()) GenerationStage.UPLOAD else GenerationStage.API_KEY
        )
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(
            stage = if (apiKeyRepository.hasApiKey()) GenerationStage.UPLOAD else GenerationStage.API_KEY,
            errorMessage = null
        )
    }

    private fun buildRequirements(state: QuestionGeneratorUiState) = GenerateRequirements(
        mcqCount = state.mcqCountText.toIntOrNull() ?: 0,
        shortCount = state.shortCountText.toIntOrNull() ?: 0,
        longCount = state.longCountText.toIntOrNull() ?: 0,
        language = state.language,
        syllabus = state.syllabus.ifBlank { null }
    )

    //Fake progress engine

    private fun startFakeProgress() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            val totalDuration = QUESTION_GENERATION_STEPS.sumOf { it.durationMs }
            var elapsed = 0L

            for ((index, step) in QUESTION_GENERATION_STEPS.withIndex()) {
                _uiState.value = _uiState.value.copy(currentStepIndex = index, currentStepLabel = step.label)

                val stepEnd = elapsed + step.durationMs
                val tickMs = 60L
                var t = elapsed

                while (t < stepEnd) {
                    delay(tickMs)
                    t += tickMs
                    _uiState.value = _uiState.value.copy(
                        progress = (t.toFloat() / totalDuration.toFloat()).coerceIn(0f, 0.97f)
                    )
                }
                elapsed = stepEnd
            }

            _uiState.value = _uiState.value.copy(
                currentStepLabel = "Almost there, finalizing your questions…",
                progress = 0.97f
            )
        }
    }

    override fun onCleared() {
        progressJob?.cancel()
        super.onCleared()
    }
}
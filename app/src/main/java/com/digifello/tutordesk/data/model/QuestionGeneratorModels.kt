package com.digifello.tutordesk.data.model

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

// Request models

data class GenerateRequirements(
    val mcqCount: Int,
    val shortCount: Int,
    val longCount: Int,
    val language: String,
    val syllabus: String? = null
)


data class SelectedFile(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String?
) {
    companion object {
        fun fromUri(resolver: ContentResolver, uri: Uri): SelectedFile? {
            var name = "file"
            var size = 0L

            resolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                    if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                }
            }

            return SelectedFile(uri = uri, name = name, sizeBytes = size, mimeType = resolver.getType(uri))
        }
    }
}

//Response models

data class GenerateQuestionResponse(
    val questions: GeneratedQuestions?,
    val sessionId: String?
)

data class GeneratedQuestions(
    val mcq: List<McqQuestion> = emptyList(),
    val shortAnswer: List<SubjectiveQuestion> = emptyList(),
    val longAnswer: List<SubjectiveQuestion> = emptyList(),
    val coverageNotes: List<CoverageNote> = emptyList()
)

data class McqQuestion(
    val question: String = "",
    val options: List<String> = emptyList(),
    val answer: String = "",
    val marks: Int = 0
)

data class SubjectiveQuestion(
    val question: String = "",
    val answer: String = "",
    val marks: Int = 0
)

data class CoverageNote(
    val topic: String = "",
    val status: String = ""
)

data class DeleteSessionResponse(val message: String? = null)

data class ApiErrorResponse(val error: String? = null, val message: String? = null)

//Paper generation (selection, metadata, PDF, save)

data class PaperMetadata(
    val institutionName: String = "",
    val className: String = "",
    val timeAllowed: String = "",
    val totalMarks: Int = 0,
    val watermark: Boolean = false
)

data class SelectedQuestionsPayload(
    val mcq: List<McqQuestion> = emptyList(),
    val shortAnswer: List<SubjectiveQuestion> = emptyList(),
    val longAnswer: List<SubjectiveQuestion> = emptyList(),
    val fillInTheBlank: List<SubjectiveQuestion> = emptyList()
)

data class GeneratePaperRequest(
    val selectedQuestions: SelectedQuestionsPayload,
    val metadata: PaperMetadata
)

data class SavePaperRequest(
    val title: String,
    val metadata: PaperMetadata,
    val selectedQuestions: SelectedQuestionsPayload
)

data class SavedPaper(
    val _id: String? = null,
    val uid: String? = null,
    val title: String? = null,
    val metadata: PaperMetadata? = null,
    val selectedQuestions: SelectedQuestionsPayload? = null,
    val createdAt: String? = null
)

data class SavePaperResponse(val paper: SavedPaper? = null)

data class MyPapersResponse(val papers: List<SavedPaper> = emptyList())
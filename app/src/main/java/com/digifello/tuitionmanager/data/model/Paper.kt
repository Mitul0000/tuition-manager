package com.digifello.tuitionmanager.data.model

data class GeneratedQuestion(
    val id: String = "",
    val text: String = "",
    val type: String = "short",       // "mcq" | "short" | "long" | "fill_blank" | "true_false"
    val marks: Int = 0,
    val options: List<String>? = null,
    val answer: String? = null,
    val topic: String = "",
    val difficulty: String = "medium"
)

data class PaperSection(
    val title: String = "",
    val instructions: String? = null,
    val marksPerQuestion: Int = 0,
    val questions: List<GeneratedQuestion> = emptyList()
)

data class Paper(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val className: String = "",
    val totalMarks: Int = 0,
    val durationMinutes: Int = 0,
    val createdAtMillis: Long = 0L,
    val pdfUrl: String? = null,
    val sections: List<PaperSection> = emptyList()
)

data class GenerationRequirements(
    val subject: String,
    val className: String,
    val topics: List<String>,
    val difficultyMix: Map<String, Int>,
    val questionTypes: List<String>,
    val totalMarks: Int,
    val extraInstructions: String? = null
)

data class PaperSetupRequest(
    val title: String,
    val durationMinutes: Int,
    val sections: List<PaperSection>
)
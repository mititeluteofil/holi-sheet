package com.holisheet.app.ocr

enum class SuggestionSource { TEXT_OCR, OBJECT_DETECTION }

enum class SuggestionRole { NONE, NAME, LABEL }

data class RecognitionSuggestion(
    val id: Int,
    val originalText: String,
    val source: SuggestionSource,
    val confidence: Float? = null
)

data class EditableSuggestion(
    val suggestion: RecognitionSuggestion,
    val editedText: String = suggestion.originalText,
    val role: SuggestionRole = SuggestionRole.NONE,
    val isEditing: Boolean = false
)

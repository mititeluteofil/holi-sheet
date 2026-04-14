package com.holisheet.app.ui.screens.scan

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holisheet.app.ocr.EditableSuggestion
import com.holisheet.app.ocr.RecognitionProcessor
import com.holisheet.app.ocr.SuggestionRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class ScanState {
    object Camera : ScanState()
    object Processing : ScanState()
    data class Results(val photoPath: String) : ScanState()
    data class Error(val message: String) : ScanState()
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val recognitionProcessor: RecognitionProcessor
) : ViewModel() {

    private val _state = MutableStateFlow<ScanState>(ScanState.Camera)
    val state = _state.asStateFlow()

    private val _suggestions = MutableStateFlow<List<EditableSuggestion>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    fun processImage(imagePath: String) = viewModelScope.launch {
        _state.value = ScanState.Processing
        val rawSuggestions = recognitionProcessor.process(imagePath)
        _suggestions.value = rawSuggestions.mapIndexed { i, s ->
            EditableSuggestion(suggestion = s)
        }
        _state.value = ScanState.Results(imagePath)
    }

    fun editSuggestion(index: Int, newText: String) {
        _suggestions.update { list ->
            list.mapIndexed { i, s -> if (i == index) s.copy(editedText = newText) else s }
        }
    }

    fun toggleEditing(index: Int) {
        _suggestions.update { list ->
            list.mapIndexed { i, s -> if (i == index) s.copy(isEditing = !s.isEditing) else s }
        }
    }

    fun setRole(index: Int, role: SuggestionRole) {
        _suggestions.update { list ->
            list.mapIndexed { i, s ->
                when {
                    i == index -> s.copy(role = role)
                    // Only one suggestion can be the item name
                    role == SuggestionRole.NAME && s.role == SuggestionRole.NAME -> s.copy(role = SuggestionRole.NONE)
                    else -> s
                }
            }
        }
    }

    fun retake() {
        _state.value = ScanState.Camera
        _suggestions.value = emptyList()
    }

    fun createPhotoFile(context: Context): File {
        val dir = File(context.filesDir, "photos").also { it.mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(dir, "IMG_$timestamp.jpg")
    }
}

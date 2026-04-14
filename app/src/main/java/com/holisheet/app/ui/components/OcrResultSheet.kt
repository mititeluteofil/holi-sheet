package com.holisheet.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.holisheet.app.ocr.EditableSuggestion
import com.holisheet.app.ocr.SuggestionRole
import com.holisheet.app.ocr.SuggestionSource

/**
 * Bottom sheet showing OCR text and object-detection suggestions after a photo is captured.
 *
 * Each suggestion:
 *  - Shows original recognized text
 *  - Has a pencil button that opens an inline TextField for editing
 *    (e.g. "Red Hot Wheels Car" → "Red Hot Wheels Car '66 Chevy")
 *  - Can be assigned as NAME (one at a time) or LABEL (multiple)
 *
 * Once happy, user taps "Continue" to proceed to Create Item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrResultSheet(
    suggestions: List<EditableSuggestion>,
    onSuggestionEdit: (index: Int, newText: String) -> Unit,
    onToggleEditing: (index: Int) -> Unit,
    onSetRole: (index: Int, role: SuggestionRole) -> Unit,
    onRetake: () -> Unit,
    onContinue: (name: String, labels: List<String>) -> Unit
) {
    val chosenName = suggestions.firstOrNull { it.role == SuggestionRole.NAME }?.editedText ?: ""
    val chosenLabels = suggestions.filter { it.role == SuggestionRole.LABEL }.map { it.editedText }

    val textSuggestions = suggestions.withIndex().filter { (_, s) -> s.suggestion.source == SuggestionSource.TEXT_OCR }
    val objectSuggestions = suggestions.withIndex().filter { (_, s) -> s.suggestion.source == SuggestionSource.OBJECT_DETECTION }

    Column(modifier = Modifier.padding(16.dp)) {
        // Handle
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .padding(bottom = 4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxSize()
            ) {}
        }

        Spacer(Modifier.height(8.dp))
        Text("Recognition Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Tap ✏️ to edit a suggestion. Set one as the item name and any others as labels.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        if (suggestions.isEmpty()) {
            Text(
                "Nothing recognized. Try retaking with better lighting.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (textSuggestions.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TextFields, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Text("Text found", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    itemsIndexed(textSuggestions) { _, (globalIndex, editable) ->
                        SuggestionRow(
                            editable = editable,
                            onEdit = { newText -> onSuggestionEdit(globalIndex, newText) },
                            onToggleEditing = { onToggleEditing(globalIndex) },
                            onSetRole = { role -> onSetRole(globalIndex, role) }
                        )
                    }
                }

                if (objectSuggestions.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Label, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(4.dp))
                            Text("Objects detected", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    itemsIndexed(objectSuggestions) { _, (globalIndex, editable) ->
                        SuggestionRow(
                            editable = editable,
                            onEdit = { newText -> onSuggestionEdit(globalIndex, newText) },
                            onToggleEditing = { onToggleEditing(globalIndex) },
                            onSetRole = { role -> onSetRole(globalIndex, role) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Summary of what will be used
        if (chosenName.isNotBlank() || chosenLabels.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(10.dp)) {
                    if (chosenName.isNotBlank()) {
                        Text("Name: $chosenName", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                    if (chosenLabels.isNotEmpty()) {
                        Text("Labels: ${chosenLabels.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onRetake, modifier = Modifier.weight(1f)) {
                Text("Retake")
            }
            Button(
                onClick = { onContinue(chosenName, chosenLabels) },
                modifier = Modifier.weight(1f),
                enabled = chosenName.isNotBlank()
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    editable: EditableSuggestion,
    onEdit: (String) -> Unit,
    onToggleEditing: () -> Unit,
    onSetRole: (SuggestionRole) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(editable.isEditing) {
        if (editable.isEditing) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    val roleColor = when (editable.role) {
        SuggestionRole.NAME  -> MaterialTheme.colorScheme.primaryContainer
        SuggestionRole.LABEL -> MaterialTheme.colorScheme.secondaryContainer
        SuggestionRole.NONE  -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when (editable.role) {
        SuggestionRole.NAME  -> MaterialTheme.colorScheme.primary
        SuggestionRole.LABEL -> MaterialTheme.colorScheme.secondary
        SuggestionRole.NONE  -> Color.Transparent
    }

    Surface(
        color = roleColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (editable.isEditing) {
                    // Inline editable TextField
                    var localText by remember(editable.editedText) { mutableStateOf(editable.editedText) }
                    OutlinedTextField(
                        value = localText,
                        onValueChange = { localText = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        trailingIcon = {
                            IconButton(onClick = { onEdit(localText); onToggleEditing() }) {
                                Icon(Icons.Default.Check, "Confirm edit", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                } else {
                    Text(
                        text = editable.editedText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    // Show confidence for object detection results
                    editable.suggestion.confidence?.let { conf ->
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${(conf * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onToggleEditing, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit suggestion", modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (!editable.isEditing) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = editable.role == SuggestionRole.NAME,
                        onClick = {
                            onSetRole(if (editable.role == SuggestionRole.NAME) SuggestionRole.NONE else SuggestionRole.NAME)
                        },
                        label = { Text("Item name", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = editable.role == SuggestionRole.LABEL,
                        onClick = {
                            onSetRole(if (editable.role == SuggestionRole.LABEL) SuggestionRole.NONE else SuggestionRole.LABEL)
                        },
                        label = { Text("Label", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}

package com.holisheet.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.holisheet.app.data.model.Label

@Composable
fun LabelChip(
    label: Label,
    onRemove: (() -> Unit)? = null
) {
    val color = runCatching { Color(android.graphics.Color.parseColor(label.colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    if (onRemove != null) {
        InputChip(
            selected = true,
            onClick = {},
            label = { Text(label.name) },
            trailingIcon = {
                IconButton(onClick = onRemove, modifier = Modifier.size(18.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove ${label.name}", modifier = Modifier.size(14.dp))
                }
            },
            colors = InputChipDefaults.inputChipColors(
                selectedContainerColor = color.copy(alpha = 0.15f),
                selectedLabelColor = color
            )
        )
    } else {
        AssistChip(
            onClick = {},
            label = { Text(label.name) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = color.copy(alpha = 0.15f),
                labelColor = color
            )
        )
    }
}

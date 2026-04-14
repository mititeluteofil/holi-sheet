package com.holisheet.app.ui.screens.item

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.holisheet.app.ui.components.LabelChip
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(itemId) { viewModel.setItemId(itemId) }

    val itemWithLabels by viewModel.item.collectAsState()
    val item = itemWithLabels?.item
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOcrText by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Remove \"${item?.name}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteItem(onBack) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.name ?: "") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "More") }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = { Icon(Icons.Default.Edit, null) },
                                onClick = { showMenu = false; item?.id?.let { onEdit(it) } }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; showDeleteDialog = true }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { item?.id?.let { onEdit(it) } }) {
                Icon(Icons.Default.Edit, "Edit")
            }
        }
    ) { padding ->
        if (item == null) {
            Box(Modifier.padding(padding).fillMaxSize()) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Full-width photo
            if (item.photoPath.isNotBlank() && File(item.photoPath).exists()) {
                AsyncImage(
                    model = item.photoPath,
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Name + quantity
                Row {
                    Text(item.name, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    if (item.quantity > 1) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text("×${item.quantity}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }

                // Labels
                if (itemWithLabels!!.labels.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(itemWithLabels!!.labels, key = { it.id }) { label ->
                            LabelChip(label = label)
                        }
                    }
                }

                // Description
                if (item.description.isNotBlank()) {
                    Text(item.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Notes
                if (item.notes.isNotBlank()) {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Notes", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(item.notes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // OCR text (collapsible)
                if (item.ocrText.isNotBlank()) {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row {
                                Text("Recognized text", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                                TextButton(onClick = { showOcrText = !showOcrText }, contentPadding = PaddingValues(0.dp)) {
                                    Text(if (showOcrText) "Hide" else "Show", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            if (showOcrText) {
                                Spacer(Modifier.height(4.dp))
                                Text(item.ocrText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Dates
                Divider()
                val fmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                Text(
                    "Added ${fmt.format(Date(item.createdAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.updatedAt != item.createdAt) {
                    Text(
                        "Updated ${fmt.format(Date(item.updatedAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

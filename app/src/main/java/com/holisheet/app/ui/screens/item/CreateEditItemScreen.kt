package com.holisheet.app.ui.screens.item

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.holisheet.app.ui.components.LabelChip
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditItemScreen(
    itemId: Long?,
    inventoryId: Long?,
    prefillName: String,
    prefillOcrText: String,
    prefillPhotoPath: String,
    prefillLabels: String,
    onBack: () -> Unit,
    viewModel: CreateEditItemViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isEditing = itemId != null

    LaunchedEffect(Unit) {
        if (isEditing) {
            viewModel.initEdit(itemId!!)
        } else {
            viewModel.initCreate(inventoryId!!, prefillName, prefillOcrText, prefillPhotoPath, prefillLabels)
        }
    }

    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val ocrText by viewModel.ocrText.collectAsState()
    val photoPath by viewModel.photoPath.collectAsState()
    val attachedLabels by viewModel.attachedLabels.collectAsState()
    val allLabels by viewModel.allLabels.collectAsState()
    val newLabelText by viewModel.newLabelText.collectAsState()
    val showLabelPicker by viewModel.showLabelPicker.collectAsState()
    var showOcrText by remember { mutableStateOf(false) }

    // Camera for retaking photo
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) pendingPhotoFile?.absolutePath?.let { viewModel.onPhotoPathChange(it) }
    }
    fun launchCamera() {
        val dir = File(context.filesDir, "photos").also { it.mkdirs() }
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "IMG_$ts.jpg")
        pendingPhotoFile = file
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraLauncher.launch(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Item" else "New Item") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo section
            if (photoPath.isNotBlank() && File(photoPath).exists()) {
                Box {
                    AsyncImage(
                        model = photoPath,
                        contentDescription = "Item photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { launchCamera() },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.CameraAlt, "Retake photo", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { launchCamera() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Take Photo")
                }
            }

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Item Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Quantity stepper
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Quantity", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.onQuantityChange(quantity - 1) }) {
                    Icon(Icons.Default.Remove, "Decrease quantity")
                }
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = { viewModel.onQuantityChange(quantity + 1) }) {
                    Icon(Icons.Default.Add, "Increase quantity")
                }
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // Labels section
            Column {
                Text("Labels", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))

                if (attachedLabels.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(attachedLabels, key = { it.id }) { label ->
                            LabelChip(label = label, onRemove = { viewModel.removeLabel(label) })
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                // Add label chip
                if (showLabelPicker) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Existing labels to pick from
                        val available = allLabels.filter { l -> attachedLabels.none { it.id == l.id } }
                        if (available.isNotEmpty()) {
                            Text("Existing labels:", style = MaterialTheme.typography.bodySmall)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(available, key = { it.id }) { label ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { viewModel.addExistingLabel(label) },
                                        label = { Text(label.name) }
                                    )
                                }
                            }
                        }
                        // Create new label
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newLabelText,
                                onValueChange = viewModel::onNewLabelTextChange,
                                label = { Text("New label name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.createAndAddLabel() },
                                enabled = newLabelText.isNotBlank()
                            ) { Text("Add") }
                        }
                        TextButton(onClick = viewModel::toggleLabelPicker) { Text("Done") }
                    }
                } else {
                    AssistChip(
                        onClick = viewModel::toggleLabelPicker,
                        label = { Text("+ Add label") },
                        leadingIcon = { Icon(Icons.Default.Label, null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 6
            )

            // OCR text (collapsible, read-only)
            if (ocrText.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Recognized text",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { showOcrText = !showOcrText }) {
                                Text(if (showOcrText) "Hide" else "Show")
                            }
                        }
                        if (showOcrText) {
                            Text(
                                text = ocrText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.save(onBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(if (isEditing) "Save Changes" else "Add to Inventory")
            }
        }
    }
}

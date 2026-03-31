package com.holisheet.app.ui.screens.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.holisheet.app.ui.components.OcrResultSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    inventoryId: Long,
    onBack: () -> Unit,
    onContinue: (name: String, ocrText: String, photoPath: String, labels: String) -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    var cameraGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraGranted = granted
    }
    LaunchedEffect(Unit) { if (!cameraGranted) permissionLauncher.launch(Manifest.permission.CAMERA) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Item") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                !cameraGranted -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Camera permission required to scan items")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Grant Permission")
                        }
                    }
                }
                state == ScanState.Camera -> {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        onCapture = { imageCapture ->
                            val file = viewModel.createPhotoFile(context)
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                            imageCapture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        viewModel.processImage(file.absolutePath)
                                    }
                                    override fun onError(exc: ImageCaptureException) {
                                        // swallow — user can retry
                                    }
                                }
                            )
                        }
                    )
                }
                state == ScanState.Processing -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Recognizing…")
                        }
                    }
                }
                state is ScanState.Results -> {
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.retake() },
                        sheetState = sheetState
                    ) {
                        OcrResultSheet(
                            suggestions = suggestions,
                            onSuggestionEdit = { index, text -> viewModel.editSuggestion(index, text) },
                            onToggleEditing = { index -> viewModel.toggleEditing(index) },
                            onSetRole = { index, role -> viewModel.setRole(index, role) },
                            onRetake = { viewModel.retake() },
                            onContinue = { name, labels ->
                                val photoPath = (state as? ScanState.Results)?.photoPath ?: ""
                                val rawOcr = suggestions.joinToString("\n") { it.suggestion.originalText }
                                onContinue(name, rawOcr, photoPath, labels.joinToString(","))
                            }
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    onCapture: (ImageCapture) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Box(modifier) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_STILL)
                        .build()
                    runCatching {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture!!
                        )
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        FloatingActionButton(
            onClick = { imageCapture?.let { onCapture(it) } },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Icon(Icons.Default.Camera, "Capture")
        }
    }
}

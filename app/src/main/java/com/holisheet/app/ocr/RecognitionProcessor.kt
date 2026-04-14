package com.holisheet.app.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecognitionProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Runs Text Recognition (reads printed text) and Image Labeling (identifies objects)
     * in parallel on the given image file. Both engines are bundled/offline — no internet needed.
     *
     * Text OCR: great for packaged goods ("Yogurt", "Hot Wheels")
     * Image Labeling: great for unpackaged objects ("red toy car", "clothing")
     */
    suspend fun process(imagePath: String): List<RecognitionSuggestion> = coroutineScope {
        val imageFile = File(imagePath)
        if (!imageFile.exists()) return@coroutineScope emptyList()

        val image = InputImage.fromFilePath(context, Uri.fromFile(imageFile))

        val textDeferred = async {
            runCatching {
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(image).await()
                    .textBlocks
                    .map { it.text.trim() }
                    .filter { it.isNotBlank() && it.length > 1 }
                    .map { text ->
                        RecognitionSuggestion(
                            id = text.hashCode(),
                            originalText = text,
                            source = SuggestionSource.TEXT_OCR
                        )
                    }
            }.getOrDefault(emptyList())
        }

        val labelDeferred = async {
            runCatching {
                ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                    .process(image).await()
                    .filter { it.confidence >= 0.70f }
                    .map { label ->
                        RecognitionSuggestion(
                            id = label.text.hashCode(),
                            originalText = label.text,
                            source = SuggestionSource.OBJECT_DETECTION,
                            confidence = label.confidence
                        )
                    }
            }.getOrDefault(emptyList())
        }

        val combined = textDeferred.await() + labelDeferred.await()
        // Deduplicate by lowercased text, preferring TEXT_OCR entries
        combined
            .groupBy { it.originalText.lowercase().trim() }
            .values
            .map { group -> group.firstOrNull { it.source == SuggestionSource.TEXT_OCR } ?: group.first() }
    }
}

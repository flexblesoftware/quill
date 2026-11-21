package com.shahid.quill.app.domain.images

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.shahid.quill.app.data.local.dao.GeneratedImageDao
import com.shahid.quill.app.data.local.entity.GeneratedImageEntity
import com.shahid.quill.app.domain.linux.PRootRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.UUID

sealed class ImageGenerationState {
    data class Generating(val step: String) : ImageGenerationState()
    data class Success(val imageEntity: GeneratedImageEntity) : ImageGenerationState()
    data class Error(val reason: String) : ImageGenerationState()
}

class ImageManager(
    private val context: Context,
    private val imageDao: GeneratedImageDao
) {
    private val prootRunner = PRootRunner(context)
    val imagesDir: File = File(context.filesDir, "generated_images").apply { mkdirs() }

    fun generateImage(
        prompt: String,
        aspectRatio: String = "1:1",
        model: String? = null
    ): Flow<ImageGenerationState> = flow {
        emit(ImageGenerationState.Generating("Submitting image generation request to Antigravity..."))

        val imageId = UUID.randomUUID().toString()
        val outputFile = File(imagesDir, "quill_$imageId.png")
        val fullPrompt = "Generate an image with aspect ratio $aspectRatio: $prompt. Save output to ${outputFile.absolutePath}"

        var generatedPath: String? = null
        var lastError: String? = null

        prootRunner.streamAgyPrompt(fullPrompt, model).collect { event ->
            when (event.type) {
                "step_update" -> {
                    emit(ImageGenerationState.Generating(event.content ?: event.stage ?: "Processing image generation..."))
                }
                "tool_call" -> {
                    emit(ImageGenerationState.Generating("Invoking image tool: ${event.tool ?: "generate_image"}..."))
                }
                "result" -> {
                    emit(ImageGenerationState.Generating("Finalizing generated asset..."))
                }
                "error" -> {
                    lastError = event.error ?: event.content
                }
                else -> {
                    if (event.content != null && (event.content.contains(".png") || event.content.contains(".webp") || event.content.contains(".jpg"))) {
                        val pathRegex = Regex("(/[^\\s]+\\.(png|jpg|jpeg|webp))")
                        val match = pathRegex.find(event.content)
                        if (match != null) {
                            generatedPath = match.value
                        }
                    }
                }
            }
        }

        val finalFile = when {
            outputFile.exists() -> outputFile
            generatedPath != null && File(generatedPath!!).exists() -> File(generatedPath!!)
            else -> findLatestBrainImage()
        }

        if (finalFile != null && finalFile.exists() && finalFile.length() > 0) {
            val savedLocalFile = if (finalFile.absolutePath != outputFile.absolutePath) {
                finalFile.copyTo(outputFile, overwrite = true)
            } else {
                finalFile
            }

            val entity = GeneratedImageEntity(
                id = imageId,
                prompt = prompt,
                aspectRatio = aspectRatio,
                localFilePath = savedLocalFile.absolutePath,
                model = model,
                status = "completed"
            )
            imageDao.insert(entity)
            emit(ImageGenerationState.Success(entity))
        } else {
            val failureReason = lastError ?: "Image generation output was not produced by Antigravity."
            emit(ImageGenerationState.Error(failureReason))
        }
    }

    private fun findLatestBrainImage(): File? {
        val brainDir = File("/data/data/com.termux/files/home/.gemini/antigravity-cli/brain")
        if (!brainDir.exists()) return null
        return brainDir.walkTopDown()
            .filter { it.isFile && (it.extension == "png" || it.extension == "jpg" || it.extension == "webp") }
            .maxByOrNull { it.lastModified() }
    }

    suspend fun saveToGallery(imageEntity: GeneratedImageEntity): Result<Uri> = withContext(Dispatchers.IO) {
        val sourceFile = File(imageEntity.localFilePath)
        if (!sourceFile.exists()) {
            return@withContext Result.failure(Exception("Source image file not found on disk."))
        }

        val fileName = "Quill_${System.currentTimeMillis()}_${imageEntity.id.take(8)}.png"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Quill")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry."))

                resolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(sourceFile).use { input ->
                        input.copyTo(out)
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                imageDao.update(imageEntity.copy(galleryUri = uri.toString()))
                Result.success(uri)
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val quillFolder = File(picturesDir, "Quill").apply { mkdirs() }
                val targetFile = File(quillFolder, fileName)

                sourceFile.copyTo(targetFile, overwrite = true)
                val uri = Uri.fromFile(targetFile)
                imageDao.update(imageEntity.copy(galleryUri = uri.toString()))
                Result.success(uri)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

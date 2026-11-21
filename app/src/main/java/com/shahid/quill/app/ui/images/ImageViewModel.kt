package com.shahid.quill.app.ui.images

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shahid.quill.app.QuillApplication
import com.shahid.quill.app.data.local.entity.GeneratedImageEntity
import com.shahid.quill.app.domain.images.ImageGenerationState
import com.shahid.quill.app.domain.images.ImageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ImageViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as QuillApplication
    private val imageManager = ImageManager(app, app.database.generatedImageDao())

    val images: StateFlow<List<GeneratedImageEntity>> = app.database.generatedImageDao()
        .getAllImages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prompt = MutableStateFlow("")
    val selectedAspectRatio = MutableStateFlow("1:1")

    private val _generationState = MutableStateFlow<ImageGenerationState?>(null)
    val generationState: StateFlow<ImageGenerationState?> = _generationState.asStateFlow()

    private val _selectedImageForModal = MutableStateFlow<GeneratedImageEntity?>(null)
    val selectedImageForModal: StateFlow<GeneratedImageEntity?> = _selectedImageForModal.asStateFlow()

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    fun selectAspectRatio(ratio: String) {
        selectedAspectRatio.value = ratio
    }

    fun openImageModal(entity: GeneratedImageEntity) {
        _selectedImageForModal.value = entity
    }

    fun closeImageModal() {
        _selectedImageForModal.value = null
    }

    fun clearSaveMessage() {
        _saveMessage.value = null
    }

    fun generate() {
        val currentPrompt = prompt.value.trim()
        if (currentPrompt.isBlank()) return

        viewModelScope.launch {
            imageManager.generateImage(
                prompt = currentPrompt,
                aspectRatio = selectedAspectRatio.value
            ).collect { state ->
                _generationState.value = state
                if (state is ImageGenerationState.Success) {
                    prompt.value = ""
                }
            }
        }
    }

    fun saveToGallery(entity: GeneratedImageEntity) {
        viewModelScope.launch {
            val res = imageManager.saveToGallery(entity)
            if (res.isSuccess) {
                _saveMessage.value = "Saved to Gallery (Pictures/Quill)!"
            } else {
                _saveMessage.value = "Failed to save: ${res.exceptionOrNull()?.message}"
            }
        }
    }
}

package com.shahid.quill.app.ui.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shahid.quill.app.QuillApplication
import com.shahid.quill.app.data.model.AgyModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ModelsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as QuillApplication
    private val prootRunner = app.prootRunner
    private val preferences = app.preferences

    private val _models = MutableStateFlow<List<AgyModel>>(emptyList())
    val models: StateFlow<List<AgyModel>> = _models.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val activeModel = preferences.activeModel

    init {
        loadModels()
    }

    fun loadModels() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = prootRunner.fetchModels()
            if (result.exitCode == 0 && result.stdout.isNotBlank()) {
                val parsedModels = parseModelsOutput(result.stdout)
                _models.value = parsedModels
            } else {
                _errorMessage.value = "Failed to fetch models from agy: ${result.stderr.ifEmpty { result.stdout.ifEmpty { "Command returned empty" } }}"
            }
            _isLoading.value = false
        }
    }

    fun setActiveModel(modelId: String) {
        viewModelScope.launch {
            preferences.setActiveModel(modelId)
        }
    }

    private fun parseModelsOutput(output: String): List<AgyModel> {
        val list = mutableListOf<AgyModel>()
        val lines = output.lines()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("#") || trimmed.startsWith("NAME")) continue

            val parts = trimmed.split(Regex("\\s{2,}|\t"))
            if (parts.isNotEmpty()) {
                val id = parts[0].trim()
                val name = if (parts.size > 1) parts[1].trim() else id
                val capability = if (parts.size > 2 && parts[2].isNotBlank()) parts[2].trim() else "Not provided by agy"
                list.add(
                    AgyModel(
                        id = id,
                        name = name,
                        capability = capability
                    )
                )
            }
        }

        if (list.isEmpty()) {
            list.add(AgyModel(id = "default", name = "Default Model", capability = "Not provided by agy", isDefault = true))
        }

        return list
    }
}

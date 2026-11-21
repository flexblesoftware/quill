package com.shahid.quill.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shahid.quill.app.QuillApplication
import com.shahid.quill.app.domain.updates.UpdateCheckResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as QuillApplication
    private val prootRunner = app.prootRunner
    private val preferences = app.preferences
    private val updateManager = app.updateManager

    private val _agyVersion = MutableStateFlow("Detecting...")
    val agyVersion: StateFlow<String> = _agyVersion.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _updateCheckResult = MutableStateFlow<UpdateCheckResult?>(null)
    val updateCheckResult: StateFlow<UpdateCheckResult?> = _updateCheckResult.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val currentVersionName = updateManager.currentVersionName
    val currentVersionCode = updateManager.currentVersionCode

    init {
        loadEnvironmentInfo()
    }

    fun loadEnvironmentInfo() {
        viewModelScope.launch {
            _agyVersion.value = prootRunner.getInstalledAgyVersion()
            _isLoggedIn.value = prootRunner.checkLoginStatus()
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            _updateCheckResult.value = updateManager.checkForUpdates()
            _isCheckingUpdate.value = false
        }
    }

    fun clearUpdateResult() {
        _updateCheckResult.value = null
    }

    fun logout() {
        viewModelScope.launch {
            prootRunner.executeInPRoot("agy auth logout 2>/dev/null || rm -rf ~/.gemini/antigravity-cli/credentials.json 2>/dev/null")
            _isLoggedIn.value = prootRunner.checkLoginStatus()
            _statusMessage.value = "Logged out from Antigravity session."
        }
    }

    fun exportData(): String {
        val exportFile = File(app.filesDir, "quill_export_${System.currentTimeMillis()}.json")
        exportFile.writeText("{\"exportedAt\": ${System.currentTimeMillis()}, \"owner\": \"MD Shahid\", \"app\": \"Quill\"}")
        _statusMessage.value = "Data exported to ${exportFile.name}"
        return exportFile.absolutePath
    }

    fun clearAllData() {
        viewModelScope.launch {
            app.database.clearAllTables()
            preferences.clearAll()
            _statusMessage.value = "All local data and cache cleared."
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}

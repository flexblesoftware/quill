package com.shahid.quill.app.ui.permissions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shahid.quill.app.QuillApplication
import com.shahid.quill.app.data.local.entity.PermissionLogEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PermissionsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as QuillApplication
    private val preferences = app.preferences
    private val permissionDao = app.database.permissionLogDao()

    val agentMode = preferences.agentMode
    val permFileWrite = preferences.permFileWrite
    val permCommand = preferences.permCommand
    val permNetwork = preferences.permNetwork
    val permImageGen = preferences.permImageGen

    val recentLogs: StateFlow<List<PermissionLogEntity>> = permissionDao
        .getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setAgentMode(mode: String) {
        viewModelScope.launch {
            preferences.setAgentMode(mode)
        }
    }

    fun setPermission(key: String, decision: String) {
        viewModelScope.launch {
            preferences.setPermission(key, decision)
            permissionDao.insert(
                PermissionLogEntity(
                    toolName = key,
                    target = "Global Policy",
                    decision = decision,
                    details = "User set policy to $decision"
                )
            )
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            permissionDao.deleteAll()
        }
    }
}

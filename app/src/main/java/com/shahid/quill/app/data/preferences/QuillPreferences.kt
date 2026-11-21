package com.shahid.quill.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quill_settings")

class QuillPreferences(private val context: Context) {

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
        val KEY_ACTIVE_MODEL = stringPreferencesKey("active_model")
        val KEY_AGENT_MODE = stringPreferencesKey("agent_mode")
        val KEY_DEFAULT_ASPECT_RATIO = stringPreferencesKey("default_aspect_ratio")
        val KEY_PERM_FILE_WRITE = stringPreferencesKey("perm_file_write")
        val KEY_PERM_COMMAND = stringPreferencesKey("perm_command")
        val KEY_PERM_NETWORK = stringPreferencesKey("perm_network")
        val KEY_PERM_IMAGE_GEN = stringPreferencesKey("perm_image_gen")
        val KEY_UPDATE_REPO = stringPreferencesKey("update_repo")
        val KEY_LAST_UPDATE_CHECK = longPreferencesKey("last_update_check")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val isSetupCompleted: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_SETUP_COMPLETED] ?: false
    }

    val activeModel: Flow<String> = context.dataStore.data.map {
        it[KEY_ACTIVE_MODEL] ?: "default"
    }

    val agentMode: Flow<String> = context.dataStore.data.map {
        it[KEY_AGENT_MODE] ?: "auto"
    }

    val defaultAspectRatio: Flow<String> = context.dataStore.data.map {
        it[KEY_DEFAULT_ASPECT_RATIO] ?: "1:1"
    }

    val permFileWrite: Flow<String> = context.dataStore.data.map {
        it[KEY_PERM_FILE_WRITE] ?: "ALLOW"
    }

    val permCommand: Flow<String> = context.dataStore.data.map {
        it[KEY_PERM_COMMAND] ?: "ALLOW"
    }

    val permNetwork: Flow<String> = context.dataStore.data.map {
        it[KEY_PERM_NETWORK] ?: "ALLOW"
    }

    val permImageGen: Flow<String> = context.dataStore.data.map {
        it[KEY_PERM_IMAGE_GEN] ?: "ALLOW"
    }

    val updateRepo: Flow<String> = context.dataStore.data.map {
        it[KEY_UPDATE_REPO] ?: "flexblesoftware/quill"
    }

    val lastUpdateCheck: Flow<Long> = context.dataStore.data.map {
        it[KEY_LAST_UPDATE_CHECK] ?: 0L
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setSetupCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_SETUP_COMPLETED] = completed }
    }

    suspend fun setActiveModel(model: String) {
        context.dataStore.edit { it[KEY_ACTIVE_MODEL] = model }
    }

    suspend fun setAgentMode(mode: String) {
        context.dataStore.edit { it[KEY_AGENT_MODE] = mode }
    }

    suspend fun setDefaultAspectRatio(ratio: String) {
        context.dataStore.edit { it[KEY_DEFAULT_ASPECT_RATIO] = ratio }
    }

    suspend fun setPermission(permissionKey: String, decision: String) {
        context.dataStore.edit {
            when (permissionKey) {
                "file_write" -> it[KEY_PERM_FILE_WRITE] = decision
                "command" -> it[KEY_PERM_COMMAND] = decision
                "network" -> it[KEY_PERM_NETWORK] = decision
                "image_gen" -> it[KEY_PERM_IMAGE_GEN] = decision
            }
        }
    }

    suspend fun setUpdateRepo(repo: String) {
        context.dataStore.edit { it[KEY_UPDATE_REPO] = repo }
    }

    suspend fun setLastUpdateCheck(time: Long) {
        context.dataStore.edit { it[KEY_LAST_UPDATE_CHECK] = time }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}

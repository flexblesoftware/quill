package com.shahid.quill.app.domain.code

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ProjectItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long
)

class CodeProjectManager(private val context: Context) {

    val projectsBaseDir: File = File(context.filesDir, "projects").apply { mkdirs() }

    suspend fun listProjects(): List<File> = withContext(Dispatchers.IO) {
        projectsBaseDir.listFiles { f -> f.isDirectory }?.toList()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    suspend fun createProject(projectName: String): File = withContext(Dispatchers.IO) {
        val sanitized = projectName.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val dir = File(projectsBaseDir, sanitized)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    suspend fun listFiles(directory: File): List<ProjectItem> = withContext(Dispatchers.IO) {
        if (!directory.exists() || !directory.isDirectory) return@withContext emptyList()
        directory.listFiles()?.map {
            ProjectItem(
                name = it.name,
                path = it.absolutePath,
                isDirectory = it.isDirectory,
                sizeBytes = if (it.isFile) it.length() else 0L,
                lastModified = it.lastModified()
            )
        }?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
    }

    suspend fun readFile(file: File): String = withContext(Dispatchers.IO) {
        if (file.exists() && file.isFile) {
            file.readText()
        } else {
            ""
        }
    }

    suspend fun saveFile(file: File, content: String) = withContext(Dispatchers.IO) {
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    suspend fun deleteItem(file: File): Boolean = withContext(Dispatchers.IO) {
        file.deleteRecursively()
    }
}

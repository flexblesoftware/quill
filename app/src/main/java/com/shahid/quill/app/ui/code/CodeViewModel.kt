package com.shahid.quill.app.ui.code

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shahid.quill.app.QuillApplication
import com.shahid.quill.app.domain.code.CodeProjectManager
import com.shahid.quill.app.domain.code.ProjectItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class CodeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as QuillApplication
    private val projectManager = CodeProjectManager(app)
    private val prootRunner = app.prootRunner

    private val _projects = MutableStateFlow<List<File>>(emptyList())
    val projects: StateFlow<List<File>> = _projects.asStateFlow()

    private val _selectedProject = MutableStateFlow<File?>(null)
    val selectedProject: StateFlow<File?> = _selectedProject.asStateFlow()

    private val _currentDirectory = MutableStateFlow<File?>(null)
    val currentDirectory: StateFlow<File?> = _currentDirectory.asStateFlow()

    private val _directoryItems = MutableStateFlow<List<ProjectItem>>(emptyList())
    val directoryItems: StateFlow<List<ProjectItem>> = _directoryItems.asStateFlow()

    private val _selectedFile = MutableStateFlow<File?>(null)
    val selectedFile: StateFlow<File?> = _selectedFile.asStateFlow()

    private val _fileContent = MutableStateFlow("")
    val fileContent: StateFlow<String> = _fileContent.asStateFlow()

    val prompt = MutableStateFlow("")
    val newProjectName = MutableStateFlow("")

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationLog = MutableStateFlow("")
    val generationLog: StateFlow<String> = _generationLog.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _projects.value = projectManager.listProjects()
        }
    }

    fun selectProject(dir: File) {
        _selectedProject.value = dir
        _currentDirectory.value = dir
        _selectedFile.value = null
        loadDirectory(dir)
    }

    fun closeProject() {
        _selectedProject.value = null
        _currentDirectory.value = null
        _selectedFile.value = null
    }

    fun loadDirectory(dir: File) {
        viewModelScope.launch {
            _currentDirectory.value = dir
            _directoryItems.value = projectManager.listFiles(dir)
        }
    }

    fun navigateUp() {
        val cur = _currentDirectory.value ?: return
        val proj = _selectedProject.value ?: return
        if (cur.absolutePath != proj.absolutePath && cur.parentFile != null) {
            loadDirectory(cur.parentFile!!)
        }
    }

    fun selectFile(item: ProjectItem) {
        val file = File(item.path)
        if (item.isDirectory) {
            loadDirectory(file)
        } else {
            viewModelScope.launch {
                _selectedFile.value = file
                _fileContent.value = projectManager.readFile(file)
            }
        }
    }

    fun closeFile() {
        _selectedFile.value = null
        _fileContent.value = ""
    }

    fun saveCurrentFile(content: String) {
        val file = _selectedFile.value ?: return
        viewModelScope.launch {
            projectManager.saveFile(file, content)
            _fileContent.value = content
        }
    }

    fun createProjectFromPrompt() {
        val projName = newProjectName.value.trim()
        val userPrompt = prompt.value.trim()
        if (projName.isBlank() || userPrompt.isBlank() || _isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            _generationLog.value = "Creating project folder $projName..."
            val projectDir = projectManager.createProject(projName)

            val fullPrompt = "Create a project in ${projectDir.absolutePath}: $userPrompt. Generate all required files."

            prootRunner.streamAgyPrompt(fullPrompt).collect { event ->
                _generationLog.value += "\n${event.type}: ${event.content ?: event.stage ?: event.tool ?: ""}"
            }

            _isGenerating.value = false
            loadProjects()
            selectProject(projectDir)
            prompt.value = ""
            newProjectName.value = ""
        }
    }
}

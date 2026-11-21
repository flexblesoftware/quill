package com.shahid.quill.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shahid.quill.app.QuillApplication
import com.shahid.quill.app.data.local.entity.ConversationEntity
import com.shahid.quill.app.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as QuillApplication
    private val conversationDao = app.database.conversationDao()
    private val messageDao = app.database.messageDao()
    private val prootRunner = app.prootRunner

    val conversations: StateFlow<List<ConversationEntity>> = conversationDao
        .getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    val messages: StateFlow<List<MessageEntity>> = _currentConversationId
        .filterNotNull()
        .flatMapLatest { id -> messageDao.getMessagesForConversation(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inputText = MutableStateFlow("")

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _currentStage = MutableStateFlow<String?>(null)
    val currentStage: StateFlow<String?> = _currentStage.asStateFlow()

    fun selectConversation(id: String) {
        _currentConversationId.value = id
    }

    fun startNewConversation() {
        val newId = UUID.randomUUID().toString()
        viewModelScope.launch {
            val entity = ConversationEntity(
                id = newId,
                title = "New Conversation"
            )
            conversationDao.insert(entity)
            _currentConversationId.value = newId
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            conversationDao.deleteById(id)
            if (_currentConversationId.value == id) {
                _currentConversationId.value = null
            }
        }
    }

    fun sendMessage() {
        val text = inputText.value.trim()
        if (text.isBlank() || _isStreaming.value) return

        viewModelScope.launch {
            var convId = _currentConversationId.value
            if (convId == null) {
                convId = UUID.randomUUID().toString()
                val title = if (text.length > 30) text.take(30) + "..." else text
                conversationDao.insert(ConversationEntity(id = convId, title = title))
                _currentConversationId.value = convId
            }

            val userMsg = MessageEntity(
                conversationId = convId,
                role = "user",
                content = text
            )
            messageDao.insert(userMsg)
            inputText.value = ""

            val assistantMsgId = UUID.randomUUID().toString()
            val assistantMsg = MessageEntity(
                id = assistantMsgId,
                conversationId = convId,
                role = "assistant",
                content = "",
                status = "streaming"
            )
            messageDao.insert(assistantMsg)

            _isStreaming.value = true
            val responseBuilder = StringBuilder()
            val eventsLog = StringBuilder()

            prootRunner.streamAgyPrompt(text).collect { event ->
                eventsLog.appendLine("${event.type}: ${event.content ?: event.stage ?: event.tool ?: ""}")
                _currentStage.value = event.stage ?: event.tool ?: "Thinking..."

                when (event.type) {
                    "step_update" -> {
                        if (!event.content.isNullOrBlank()) {
                            responseBuilder.append(event.content).append(" ")
                            messageDao.update(
                                assistantMsg.copy(
                                    content = responseBuilder.toString(),
                                    stage = event.stage,
                                    rawEvents = eventsLog.toString()
                                )
                            )
                        }
                    }
                    "result", "raw" -> {
                        val chunk = event.content ?: event.rawText ?: ""
                        if (chunk.isNotBlank() && !responseBuilder.contains(chunk)) {
                            responseBuilder.append(chunk).append("\n")
                            messageDao.update(
                                assistantMsg.copy(
                                    content = responseBuilder.toString().trim(),
                                    stage = "Completed",
                                    rawEvents = eventsLog.toString()
                                )
                            )
                        }
                    }
                    "error" -> {
                        val err = event.error ?: "Error during generation"
                        responseBuilder.append("\n[Error: $err]")
                        messageDao.update(
                            assistantMsg.copy(
                                content = responseBuilder.toString(),
                                status = "error",
                                rawEvents = eventsLog.toString()
                            )
                        )
                    }
                }
            }

            _isStreaming.value = false
            _currentStage.value = null

            val finalContent = if (responseBuilder.isBlank()) "Agent completed response." else responseBuilder.toString().trim()
            messageDao.update(
                assistantMsg.copy(
                    content = finalContent,
                    status = "completed",
                    rawEvents = eventsLog.toString()
                )
            )

            conversationDao.getConversationById(convId)?.let {
                conversationDao.update(it.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }
}

package com.shahid.quill.app.domain.terminal

import android.content.Context
import com.shahid.quill.app.domain.linux.PRootRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class TerminalSession(
    private val context: Context,
    private val scope: CoroutineScope,
    private val initialCommand: String? = null
) {
    private var process: Process? = null
    private var outputStream: OutputStream? = null

    private val _buffer = MutableStateFlow("")
    val buffer: StateFlow<String> = _buffer.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun start() {
        if (_isRunning.value) return

        val prootRunner = PRootRunner(context)
        val termuxBash = File("/data/data/com.termux/files/usr/bin/bash")
        val pb: ProcessBuilder

        if (termuxBash.exists()) {
            pb = ProcessBuilder("/data/data/com.termux/files/usr/bin/bash", "-l")
            pb.directory(File("/data/data/com.termux/files/home"))
            pb.environment()["TERM"] = "xterm-256color"
            pb.environment()["PATH"] = "/data/data/com.termux/files/home/.local/bin:/data/data/com.termux/files/usr/bin:" + (System.getenv("PATH") ?: "")
        } else if (prootRunner.isEnvironmentReady()) {
            pb = ProcessBuilder(
                prootRunner.prootBinary.absolutePath,
                "-0",
                "-r", prootRunner.rootfsDir.absolutePath,
                "-b", "/dev",
                "-b", "/proc",
                "-b", "/sys",
                "-w", "/root",
                "/bin/bash", "-l"
            )
            pb.environment()["TERM"] = "xterm-256color"
        } else {
            pb = ProcessBuilder("sh")
        }

        pb.redirectErrorStream(true)

        try {
            val proc = pb.start()
            process = proc
            outputStream = proc.outputStream
            _isRunning.value = true

            scope.launch(Dispatchers.IO) {
                val inputStream: InputStream = proc.inputStream
                val bufferBytes = ByteArray(4096)
                var read: Int

                while (inputStream.read(bufferBytes).also { read = it } != -1) {
                    val text = String(bufferBytes, 0, read)
                    _buffer.value = (_buffer.value + text).takeLast(50000)
                }

                _isRunning.value = false
            }

            if (!initialCommand.isNullOrBlank()) {
                write(initialCommand + "\n")
            }
        } catch (e: Exception) {
            _buffer.value += "\nFailed to start terminal: ${e.message}\n"
            _isRunning.value = false
        }
    }

    fun write(input: String) {
        scope.launch(Dispatchers.IO) {
            try {
                outputStream?.write(input.toByteArray())
                outputStream?.flush()
            } catch (e: Exception) {
                _buffer.value += "\n[Error writing to terminal: ${e.message}]\n"
            }
        }
    }

    fun sendCtrl(char: Char) {
        val ctrlCode = (char.uppercaseChar().code - 64).toByte()
        scope.launch(Dispatchers.IO) {
            try {
                outputStream?.write(byteArrayOf(ctrlCode))
                outputStream?.flush()
            } catch (e: Exception) {
            }
        }
    }

    fun sendEsc() = write("\u001B")
    fun sendTab() = write("\t")
    fun sendArrowUp() = write("\u001B[A")
    fun sendArrowDown() = write("\u001B[B")
    fun sendArrowRight() = write("\u001B[C")
    fun sendArrowLeft() = write("\u001B[D")

    fun clear() {
        _buffer.value = ""
    }

    fun destroy() {
        try {
            process?.destroy()
        } catch (e: Exception) {
        }
        _isRunning.value = false
    }
}

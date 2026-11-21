package com.shahid.quill.app

import android.app.Application
import com.shahid.quill.app.data.local.QuillDatabase
import com.shahid.quill.app.data.preferences.QuillPreferences
import com.shahid.quill.app.domain.linux.LinuxEnvironmentManager
import com.shahid.quill.app.domain.linux.PRootRunner
import com.shahid.quill.app.domain.updates.UpdateManager

class QuillApplication : Application() {

    lateinit var database: QuillDatabase
        private set

    lateinit var preferences: QuillPreferences
        private set

    lateinit var prootRunner: PRootRunner
        private set

    lateinit var environmentManager: LinuxEnvironmentManager
        private set

    lateinit var updateManager: UpdateManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = QuillDatabase.getInstance(this)
        preferences = QuillPreferences(this)
        prootRunner = PRootRunner(this)
        environmentManager = LinuxEnvironmentManager(this)
        updateManager = UpdateManager(this)
    }
}

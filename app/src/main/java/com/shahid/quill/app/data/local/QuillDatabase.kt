package com.shahid.quill.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shahid.quill.app.data.local.dao.ConversationDao
import com.shahid.quill.app.data.local.dao.GeneratedImageDao
import com.shahid.quill.app.data.local.dao.MessageDao
import com.shahid.quill.app.data.local.dao.PermissionLogDao
import com.shahid.quill.app.data.local.dao.UsageCacheDao
import com.shahid.quill.app.data.local.entity.ConversationEntity
import com.shahid.quill.app.data.local.entity.GeneratedImageEntity
import com.shahid.quill.app.data.local.entity.MessageEntity
import com.shahid.quill.app.data.local.entity.PermissionLogEntity
import com.shahid.quill.app.data.local.entity.UsageCacheEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        GeneratedImageEntity::class,
        PermissionLogEntity::class,
        UsageCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class QuillDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun generatedImageDao(): GeneratedImageDao
    abstract fun permissionLogDao(): PermissionLogDao
    abstract fun usageCacheDao(): UsageCacheDao

    companion object {
        @Volatile
        private var INSTANCE: QuillDatabase? = null

        fun getInstance(context: Context): QuillDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuillDatabase::class.java,
                    "quill_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

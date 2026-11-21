package com.shahid.quill.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "permission_logs")
data class PermissionLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val toolName: String,
    val target: String,
    val decision: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String? = null
)

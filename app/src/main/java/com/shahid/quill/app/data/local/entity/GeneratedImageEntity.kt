package com.shahid.quill.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "generated_images")
data class GeneratedImageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val prompt: String,
    val aspectRatio: String = "1:1",
    val localFilePath: String,
    val galleryUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "completed",
    val model: String? = null,
    val errorReason: String? = null
)

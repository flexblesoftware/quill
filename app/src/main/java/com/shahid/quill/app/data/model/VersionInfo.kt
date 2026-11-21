package com.shahid.quill.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VersionInfo(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val minRequiredVersionCode: Int,
    val apkUrl: String,
    val sha256: String,
    val changelog: List<String> = emptyList(),
    val updatedAt: String
)

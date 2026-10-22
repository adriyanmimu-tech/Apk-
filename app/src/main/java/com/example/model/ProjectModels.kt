package com.example.model

import android.net.Uri

data class ValidationResult(
    val isValid: Boolean = false,
    val zipFileName: String = "",
    val zipFileSize: Long = 0L,
    val zipFileSizeFormatted: String = "",
    val totalEntriesCount: Int = 0,
    val uncompressedSizeBytes: Long = 0L,
    val hasSettingsGradle: Boolean = false,
    val hasGradlew: Boolean = false,
    val hasGradleWrapperProperties: Boolean = false,
    val hasAppModule: Boolean = false,
    val hasManifest: Boolean = false,
    val isApkDirectlyUploaded: Boolean = false,
    val isZipBombDetected: Boolean = false,
    val isPathTraversalDetected: Boolean = false,
    val detectedAppName: String? = null,
    val detectedPackageName: String? = null,
    val detectedMinSdk: String? = null,
    val detectedTargetSdk: String? = null,
    val detectedCompileSdk: String? = null,
    val detectedGradleVersion: String? = null,
    val detectedLanguages: List<String> = emptyList(),
    val detectedLibraries: List<String> = emptyList(),
    val potentialMissingSecrets: List<String> = emptyList(),
    val bengaliSummary: String = "",
    val detailedBengaliIssues: List<String> = emptyList(),
    val missingFilesList: List<String> = emptyList(),
    val foundKeyFiles: List<String> = emptyList()
)

enum class BuildStepState {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

data class BuildStep(
    val titleBn: String,
    val descriptionBn: String,
    val state: BuildStepState = BuildStepState.PENDING
)

enum class LogLevel {
    INFO,
    WARN,
    ERROR,
    SUCCESS
}

data class BuildLogEntry(
    val timestamp: String,
    val message: String,
    val level: LogLevel = LogLevel.INFO
)

enum class BuildState {
    IDLE,
    VALIDATING,
    VALIDATED,
    UPLOADING,
    COMPILING,
    SUCCESS,
    FAILED
}

data class GeneratedApkInfo(
    val fileName: String,
    val fileSizeFormatted: String,
    val fileSizeBytes: Long,
    val localUri: Uri?,
    val packageName: String,
    val buildTimeFormatted: String,
    val sha256Checksum: String
)

data class BuildWorkerConfig(
    val workerUrl: String = "https://build-worker.local:8080",
    val useLocalTestWorker: Boolean = true,
    val isConnected: Boolean = true,
    val lastPingMs: Long? = 42L,
    val statusMessageBn: String = "টেস্ট ওয়ার্কার সক্রিয় (সিমুলেটেড JDK 17 + Android SDK 34)"
)

enum class SampleProjectType {
    VALID_COMPOSE_PROJECT,
    INCOMPLETE_SOURCE_ONLY,
    INVALID_APK_FILE
}

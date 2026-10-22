package com.example.validator

import android.content.Context
import android.net.Uri
import com.example.model.ValidationResult
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ZipProjectValidator {

    private const val MAX_ENTRIES = 15000
    private const val MAX_UNCOMPRESSED_BYTES = 350L * 1024L * 1024L // 350 MB limit
    private const val MAX_PARSE_BUFFER_BYTES = 64 * 1024 // 64 KB for inspecting gradle/manifest files

    fun validateZipStream(
        context: Context,
        uri: Uri?,
        fileNameOverride: String? = null,
        fileSizeOverride: Long? = null
    ): ValidationResult {
        if (uri == null) {
            return ValidationResult(
                isValid = false,
                bengaliSummary = "কোনো ফাইল নির্বাচন করা হয়নি। দয়া করে একটি অ্যান্ড্রয়েড স্টুডিও প্রজেক্টের ZIP ফাইল বেছে নিন।"
            )
        }

        val resolvedFileName = fileNameOverride ?: getFileName(context, uri) ?: "project.zip"
        val resolvedFileSize = fileSizeOverride ?: getFileSize(context, uri)

        // Rule: Direct APK upload rejection
        if (resolvedFileName.lowercase(Locale.ROOT).endsWith(".apk")) {
            return ValidationResult(
                isValid = false,
                zipFileName = resolvedFileName,
                zipFileSize = resolvedFileSize,
                zipFileSizeFormatted = formatBytes(resolvedFileSize),
                isApkDirectlyUploaded = true,
                bengaliSummary = "সরাসরি APK ফাইল আপলোড করা যাবে না। এটি কোনো APK কনভার্টার নয়।",
                detailedBengaliIssues = listOf(
                    "আপনি একটি তৈরি করা .apk ফাইল আপলোড করেছেন।",
                    "আমাদের টুলটি সোর্স কোড থেকে নতুন করে APK বিল্ড করে।",
                    "অনুগ্রহ করে মূল Android Studio প্রজেক্টের সম্পূর্ণ ZIP ফাইল আপলোড করুন।"
                )
            )
        }

        var entryCount = 0
        var totalUncompressedSize = 0L
        var isPathTraversal = false
        var isZipBomb = false
        var containsApkInside = false

        var hasSettingsGradle = false
        var hasGradlew = false
        var hasGradleWrapperProperties = false
        var hasAppModule = false
        var hasManifest = false
        var hasSourceFiles = false

        val foundKeyFiles = mutableListOf<String>()
        val missingFiles = mutableListOf<String>()

        var detectedAppName: String? = null
        var detectedPackageName: String? = null
        var detectedMinSdk: String? = null
        var detectedTargetSdk: String? = null
        var detectedCompileSdk: String? = null
        var detectedGradleVersion: String? = null

        val detectedLanguages = mutableSetOf<String>()
        val detectedLibraries = mutableSetOf<String>()
        val potentialMissingSecrets = mutableSetOf<String>()

        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return ValidationResult(
                    isValid = false,
                    zipFileName = resolvedFileName,
                    bengaliSummary = "ফাইলটি পড়তে ব্যর্থ হয়েছে। অনুগ্রহ করে আবার চেষ্টা করুন।"
                )

            ZipInputStream(inputStream).use { zis ->
                var entry: ZipEntry? = zis.nextEntry

                while (entry != null) {
                    entryCount++
                    val name = entry.name

                    // Security check: Path traversal (Zip Slip)
                    if (name.contains("..") || name.startsWith("/") || name.startsWith("\\")) {
                        isPathTraversal = true
                    }

                    // Security check: Size limits
                    val size = entry.size
                    if (size > 0) {
                        totalUncompressedSize += size
                    }

                    if (entryCount > MAX_ENTRIES || totalUncompressedSize > MAX_UNCOMPRESSED_BYTES) {
                        isZipBomb = true
                        break
                    }

                    val normalizedName = name.replace('\\', '/').trimStart('/')
                    val lower = normalizedName.lowercase(Locale.ROOT)

                    if (lower.endsWith(".apk")) {
                        containsApkInside = true
                    }

                    // Detect language
                    if (lower.endsWith(".kt")) {
                        detectedLanguages.add("Kotlin")
                        hasSourceFiles = true
                    }
                    if (lower.endsWith(".java")) {
                        detectedLanguages.add("Java")
                        hasSourceFiles = true
                    }

                    // Check for key files (supporting root level or one folder nesting)
                    val baseName = normalizedName.substringAfterLast('/')
                    val lowerBaseName = baseName.lowercase(Locale.ROOT)

                    if (lowerBaseName == "settings.gradle" || lowerBaseName == "settings.gradle.kts") {
                        hasSettingsGradle = true
                        foundKeyFiles.add(normalizedName)
                        val content = readPartialContent(zis)
                        parseSettingsGradle(content)?.let { detectedAppName = it }
                    }

                    if (lowerBaseName == "gradlew" || lowerBaseName == "gradlew.bat") {
                        hasGradlew = true
                        foundKeyFiles.add(normalizedName)
                    }

                    if (lower.contains("gradle-wrapper.properties")) {
                        hasGradleWrapperProperties = true
                        foundKeyFiles.add(normalizedName)
                        val content = readPartialContent(zis)
                        parseGradleWrapperVersion(content)?.let { detectedGradleVersion = it }
                    }

                    if (lowerBaseName == "build.gradle" || lowerBaseName == "build.gradle.kts") {
                        if (lower.contains("/app/") || lower.startsWith("app/")) {
                            hasAppModule = true
                            foundKeyFiles.add(normalizedName)
                        }
                        val content = readPartialContent(zis)
                        parseAppBuildGradle(
                            content,
                            onPackageFound = { if (detectedPackageName == null) detectedPackageName = it },
                            onMinSdkFound = { detectedMinSdk = it },
                            onTargetSdkFound = { detectedTargetSdk = it },
                            onCompileSdkFound = { detectedCompileSdk = it },
                            onLibraryFound = { detectedLibraries.add(it) },
                            onSecretPotential = { potentialMissingSecrets.add(it) }
                        )
                    }

                    if (lowerBaseName == "androidmanifest.xml") {
                        hasManifest = true
                        foundKeyFiles.add(normalizedName)
                        val content = readPartialContent(zis)
                        parseManifest(content, onPackageFound = {
                            if (detectedPackageName == null) detectedPackageName = it
                        })
                    }

                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            return ValidationResult(
                isValid = false,
                zipFileName = resolvedFileName,
                zipFileSize = resolvedFileSize,
                zipFileSizeFormatted = formatBytes(resolvedFileSize),
                bengaliSummary = "ZIP ফাইল আনপ্যাক করতে ত্রুটি: ${e.localizedMessage ?: "অজ্ঞাত ত্রুটি"}। ফাইলটি নষ্ট বা ক্ষতিগ্রস্থ হতে পারে।"
            )
        }

        // Evaluate security violations
        if (isPathTraversal) {
            return ValidationResult(
                isValid = false,
                zipFileName = resolvedFileName,
                zipFileSize = resolvedFileSize,
                zipFileSizeFormatted = formatBytes(resolvedFileSize),
                isPathTraversalDetected = true,
                bengaliSummary = "নিরাপত্তা ঝুঁকি! ZIP ফাইলে Path Traversal (Zip Slip) প্যাটার্ন সনাক্ত হয়েছে। বিল্ড বাতিল করা হয়েছে।",
                detailedBengaliIssues = listOf(
                    "ফাইল আর্काइভে '..' বা সরাসরি রুট নির্দেশকারী বিপজ্জনক পাথ রয়েছে।",
                    "নিরাপত্তার স্বার্থে এই ধরনের আনট্রাস্টেড ফাইল প্রসেস করা সম্ভব নয়।"
                )
            )
        }

        if (isZipBomb) {
            return ValidationResult(
                isValid = false,
                zipFileName = resolvedFileName,
                zipFileSize = resolvedFileSize,
                zipFileSizeFormatted = formatBytes(resolvedFileSize),
                isZipBombDetected = true,
                bengaliSummary = "নিরাপত্তা সতর্কতা! ZIP ফাইলটির সাইজ বা ফাইল সংখ্যা অস্বাভাবিকভাবে বেশি (Zip Bomb সন্দেহযুক্ত)।",
                detailedBengaliIssues = listOf(
                    "সর্বোচ্চ আনকম্প্রেসড মেমরি সীমা (৩৫০ মেগাবাইট) বা ফাইল সংখ্যা (১৫,০০০) অতিক্রম করেছে।"
                )
            )
        }

        // Evaluate Missing Files
        if (!hasSettingsGradle) missingFiles.add("settings.gradle বা settings.gradle.kts (রুট বিল্ড ফাইল)")
        if (!hasGradlew) missingFiles.add("gradlew (Gradle Wrapper স্ক্রিপ্ট)")
        if (!hasGradleWrapperProperties) missingFiles.add("gradle/wrapper/gradle-wrapper.properties")
        if (!hasAppModule) missingFiles.add("app/build.gradle বা app/build.gradle.kts")
        if (!hasManifest) missingFiles.add("AndroidManifest.xml")

        val issues = mutableListOf<String>()

        if (missingFiles.isNotEmpty()) {
            if (hasSourceFiles && (!hasSettingsGradle || !hasAppModule || !hasGradlew)) {
                issues.add("প্রজেক্টটি অসম্পূর্ণ! শুধুমাত্র সোর্স কোড (.kt / .java) ফাইল দিলে APK তৈরি হবে না।")
                issues.add("Android Studio প্রজেক্টের সম্পূর্ণ রুট ডিরেক্টরি ZIP করে দিতে হবে (যাতে Gradle ও কনফিগারেশন থাকে)।")
            }
            issues.add("অনুপস্থিত প্রয়োজনীয় ফাইলসমূহ: " + missingFiles.joinToString(", "))
        }

        val isValid = missingFiles.isEmpty() && !isPathTraversal && !isZipBomb

        val summary = if (isValid) {
            "প্রজেক্ট সফলভাবে ভ্যালিডেট হয়েছে! প্রয়োজনীয় সব ফাইল (settings.gradle, gradlew, app মডিউল) বিদ্যমান। এখন আসল APK বিল্ড করা যাবে।"
        } else {
            "প্রজেক্ট ভ্যালিডেশনে ত্রুটি! সম্পূর্ণ অ্যান্ড্রয়েড স্টুডিও স্ট্রাকচার পাওয়া যায়নি।"
        }

        return ValidationResult(
            isValid = isValid,
            zipFileName = resolvedFileName,
            zipFileSize = resolvedFileSize,
            zipFileSizeFormatted = formatBytes(resolvedFileSize),
            totalEntriesCount = entryCount,
            uncompressedSizeBytes = totalUncompressedSize,
            hasSettingsGradle = hasSettingsGradle,
            hasGradlew = hasGradlew,
            hasGradleWrapperProperties = hasGradleWrapperProperties,
            hasAppModule = hasAppModule,
            hasManifest = hasManifest,
            isApkDirectlyUploaded = containsApkInside && entryCount <= 2,
            isZipBombDetected = isZipBomb,
            isPathTraversalDetected = isPathTraversal,
            detectedAppName = detectedAppName ?: "My Application",
            detectedPackageName = detectedPackageName ?: "com.example.app",
            detectedMinSdk = detectedMinSdk ?: "24",
            detectedTargetSdk = detectedTargetSdk ?: "34",
            detectedCompileSdk = detectedCompileSdk ?: "34",
            detectedGradleVersion = detectedGradleVersion ?: "8.x",
            detectedLanguages = detectedLanguages.toList().ifEmpty { listOf("Kotlin") },
            detectedLibraries = detectedLibraries.toList(),
            potentialMissingSecrets = potentialMissingSecrets.toList(),
            bengaliSummary = summary,
            detailedBengaliIssues = issues,
            missingFilesList = missingFiles,
            foundKeyFiles = foundKeyFiles
        )
    }

    private fun readPartialContent(zis: ZipInputStream): String {
        val buffer = ByteArray(MAX_PARSE_BUFFER_BYTES)
        val bytesRead = zis.read(buffer, 0, buffer.size)
        return if (bytesRead > 0) String(buffer, 0, bytesRead, Charsets.UTF_8) else ""
    }

    private fun parseSettingsGradle(content: String): String? {
        val rootNameRegex = Regex("""rootProject\.name\s*=\s*["']([^"']+)["']""")
        return rootNameRegex.find(content)?.groupValues?.getOrNull(1)
    }

    private fun parseGradleWrapperVersion(content: String): String? {
        val gradleRegex = Regex("""gradle-([0-9.]+)-""")
        return gradleRegex.find(content)?.groupValues?.getOrNull(1)
    }

    private fun parseAppBuildGradle(
        content: String,
        onPackageFound: (String) -> Unit,
        onMinSdkFound: (String) -> Unit,
        onTargetSdkFound: (String) -> Unit,
        onCompileSdkFound: (String) -> Unit,
        onLibraryFound: (String) -> Unit,
        onSecretPotential: (String) -> Unit
    ) {
        // Application ID
        val appIdRegex = Regex("""(?:applicationId|namespace)\s*=?\s*["']([^"']+)["']""")
        appIdRegex.find(content)?.groupValues?.getOrNull(1)?.let(onPackageFound)

        // SDK versions
        val minSdkRegex = Regex("""minSdk(?:Version)?\s*=?\s*([0-9]+)""")
        minSdkRegex.find(content)?.groupValues?.getOrNull(1)?.let(onMinSdkFound)

        val targetSdkRegex = Regex("""targetSdk(?:Version)?\s*=?\s*([0-9]+)""")
        targetSdkRegex.find(content)?.groupValues?.getOrNull(1)?.let(onTargetSdkFound)

        val compileSdkRegex = Regex("""compileSdk(?:Version)?\s*=?\s*([0-9]+)""")
        compileSdkRegex.find(content)?.groupValues?.getOrNull(1)?.let(onCompileSdkFound)

        // Libraries
        if (content.contains("compose", ignoreCase = true)) onLibraryFound("Jetpack Compose")
        if (content.contains("room", ignoreCase = true)) onLibraryFound("Room Database")
        if (content.contains("retrofit", ignoreCase = true)) onLibraryFound("Retrofit Networking")
        if (content.contains("coroutines", ignoreCase = true)) onLibraryFound("Kotlin Coroutines")
        if (content.contains("firebase", ignoreCase = true)) onLibraryFound("Firebase")

        // Potential secrets check
        if (content.contains("google-services", ignoreCase = true)) {
            onSecretPotential("google-services.json (ফায়ারবেস কনফিগ ফাইল)")
        }
        if (content.contains(".env", ignoreCase = true) || content.contains("secrets.properties", ignoreCase = true)) {
            onSecretPotential(".env বা সিক্রেট কনফিগারেশন")
        }
        if (content.contains("signingConfigs", ignoreCase = true) && content.contains("keyPassword", ignoreCase = true)) {
            onSecretPotential("রিলিজ সাইনিং কীস্টোর (ডিবাগ বিল্ডে স্বয়ংক্রিয় ডিবাগ কী ব্যবহৃত হবে)")
        }
    }

    private fun parseManifest(content: String, onPackageFound: (String) -> Unit) {
        val packageRegex = Regex("""package\s*=\s*["']([^"']+)["']""")
        packageRegex.find(content)?.groupValues?.getOrNull(1)?.let(onPackageFound)
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val colIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (colIndex != -1) {
                        name = it.getString(colIndex)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path?.substringAfterLast('/')
        }
        return name
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        var size: Long = 0L
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val colIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (colIndex != -1) {
                        size = it.getLong(colIndex)
                    }
                }
            }
        }
        return size
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        var digitGroups = 0
        var dBytes = bytes.toDouble()
        while (dBytes >= 1024 && digitGroups < units.size - 1) {
            dBytes /= 1024
            digitGroups++
        }
        return String.format(Locale.US, "%.1f %s", dBytes, units[digitGroups])
    }
}

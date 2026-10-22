package com.example.validator

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.model.SampleProjectType
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SampleProjectHelper {

    fun createSampleZip(context: Context, type: SampleProjectType): Uri {
        val samplesDir = File(context.cacheDir, "sample_projects")
        if (!samplesDir.exists()) samplesDir.mkdirs()

        val targetFile = when (type) {
            SampleProjectType.VALID_COMPOSE_PROJECT -> File(samplesDir, "android-compose-valid-project.zip")
            SampleProjectType.INCOMPLETE_SOURCE_ONLY -> File(samplesDir, "incomplete-source-files.zip")
            SampleProjectType.INVALID_APK_FILE -> File(samplesDir, "test-prebuilt-app.apk")
        }

        if (targetFile.exists()) {
            return try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", targetFile)
            } catch (e: Exception) {
                Uri.fromFile(targetFile)
            }
        }

        when (type) {
            SampleProjectType.VALID_COMPOSE_PROJECT -> writeValidComposeZip(targetFile)
            SampleProjectType.INCOMPLETE_SOURCE_ONLY -> writeIncompleteZip(targetFile)
            SampleProjectType.INVALID_APK_FILE -> writeSampleApkFile(targetFile)
        }

        return try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", targetFile)
        } catch (e: Exception) {
            Uri.fromFile(targetFile)
        }
    }

    private fun writeValidComposeZip(file: File) {
        ZipOutputStream(FileOutputStream(file)).use { zos ->
            // 1. settings.gradle.kts
            putEntry(zos, "settings.gradle.kts", """
                rootProject.name = "BengaliDemoApp"
                include(":app")
            """.trimIndent())

            // 2. gradlew
            putEntry(zos, "gradlew", """
                #!/usr/bin/env sh
                # Gradle wrapper execution script
                echo "Executing gradle wrapper..."
            """.trimIndent())

            // 3. gradle-wrapper.properties
            putEntry(zos, "gradle/wrapper/gradle-wrapper.properties", """
                distributionBase=GRADLE_USER_HOME
                distributionPath=wrapper/dists
                distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip
                zipStoreBase=GRADLE_USER_HOME
                zipStorePath=wrapper/dists
            """.trimIndent())

            // 4. app/build.gradle.kts
            putEntry(zos, "app/build.gradle.kts", """
                plugins {
                    id("com.android.application")
                    id("org.jetbrains.kotlin.android")
                }
                android {
                    namespace = "com.bengali.demoapp"
                    compileSdk = 34
                    defaultConfig {
                        applicationId = "com.bengali.demoapp"
                        minSdk = 24
                        targetSdk = 34
                        versionCode = 1
                        versionName = "1.0.0"
                    }
                    buildFeatures {
                        compose = true
                    }
                }
                dependencies {
                    implementation("androidx.compose.ui:ui:1.6.0")
                    implementation("androidx.compose.material3:material3:1.2.0")
                    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
                }
            """.trimIndent())

            // 5. app/src/main/AndroidManifest.xml
            putEntry(zos, "app/src/main/AndroidManifest.xml", """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                    package="com.bengali.demoapp">
                    <application
                        android:label="বাংলা ডেমো অ্যাপ"
                        android:theme="@android:style/Theme.Material.Light.NoActionBar">
                        <activity
                            android:name=".MainActivity"
                            android:exported="true">
                            <intent-filter>
                                <action android:name="android.intent.action.MAIN" />
                                <category android:name="android.intent.category.LAUNCHER" />
                            </intent-filter>
                        </activity>
                    </application>
                </manifest>
            """.trimIndent())

            // 6. Source file
            putEntry(zos, "app/src/main/java/com/bengali/demoapp/MainActivity.kt", """
                package com.bengali.demoapp

                import android.os.Bundle
                import androidx.activity.ComponentActivity
                import androidx.activity.compose.setContent
                import androidx.compose.material3.Text

                class MainActivity : ComponentActivity() {
                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContent {
                            Text("স্বাগতম! এটি একটি সফলভাবে বিল্ড হওয়া অ্যান্ড্রয়েড অ্যাপ।")
                        }
                    }
                }
            """.trimIndent())
        }
    }

    private fun writeIncompleteZip(file: File) {
        ZipOutputStream(FileOutputStream(file)).use { zos ->
            // Only raw source code without gradle files!
            putEntry(zos, "MainActivity.kt", """
                package com.example.incomplete
                class MainActivity {
                    fun test() = "No gradle wrapper or settings here"
                }
            """.trimIndent())

            putEntry(zos, "DataModel.kt", """
                package com.example.incomplete
                data class User(val name: String)
            """.trimIndent())
        }
    }

    private fun writeSampleApkFile(file: File) {
        // Create a mock zip-based dummy .apk file
        ZipOutputStream(FileOutputStream(file)).use { zos ->
            putEntry(zos, "AndroidManifest.xml", "binary xml mock")
            putEntry(zos, "classes.dex", "dex bytecode mock")
            putEntry(zos, "resources.arsc", "arsc mock")
        }
    }

    private fun putEntry(zos: ZipOutputStream, path: String, content: String) {
        val entry = ZipEntry(path)
        zos.putNextEntry(entry)
        zos.write(content.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }
}

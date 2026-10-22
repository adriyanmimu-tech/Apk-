package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ApkInstallerHelper {

    fun canInstallUnknownApps(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openUnknownAppsSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun launchApkInstall(context: Context, apkUri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ইনস্টলার খুলতে ত্রুটি: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareApk(context: Context, apkUri: Uri, appName: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(Intent.EXTRA_SUBJECT, "$appName - Debug APK")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "APK ফাইল পাঠান / শেয়ার করুন").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "শেয়ার করতে ত্রুটি: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveApkToDownloads(context: Context, sourceUri: Uri, fileName: String): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.android.package-archive")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val targetUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return null

                resolver.openOutputStream(targetUri)?.use { output ->
                    inputStream.copyTo(output)
                }
                Toast.makeText(context, "APK ডাউনলোড ফোল্ডারে সংরক্ষিত হয়েছে!", Toast.LENGTH_LONG).show()
                targetUri
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                FileOutputStream(targetFile).use { output ->
                    inputStream.copyTo(output)
                }
                Toast.makeText(context, "APK ডাউনলোড ফোল্ডারে সংরক্ষিত হয়েছে!", Toast.LENGTH_LONG).show()
                Uri.fromFile(targetFile)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "ডাউনলোড সংরক্ষণ ব্যর্থ: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }
}

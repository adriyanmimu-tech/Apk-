package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BuildState
import com.example.model.SampleProjectType
import com.example.ui.MainViewModel
import com.example.ui.components.TerminalLogViewer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.TechBlue
import com.example.ui.theme.TechCyan
import com.example.util.ApkInstallerHelper

@Composable
fun BuilderScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val selectedUri by viewModel.selectedZipUri.collectAsState()
    val validation by viewModel.validationResult.collectAsState()
    val isValidating by viewModel.isValidating.collectAsState()
    val buildState by viewModel.buildState.collectAsState()
    val buildProgress by viewModel.buildProgress.collectAsState()
    val statusText by viewModel.currentBuildStatusText.collectAsState()
    val logs by viewModel.buildLogs.collectAsState()
    val apkInfo by viewModel.generatedApk.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val workerConfig by viewModel.workerConfig.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.selectProjectZip(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upload & Picker Card
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            border = BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(listOf(TechCyan, TechBlue))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            filePickerLauncher.launch(
                                arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream", "*/*")
                            )
                        }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(TechCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload ZIP",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "সম্পূর্ণ Android Studio প্রজেক্ট ZIP আপলোড করুন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "settings.gradle(.kts), gradlew ও app/build.gradle সম্বলিত আসল প্রজেক্ট ফোল্ডার নির্বাচন করুন",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                filePickerLauncher.launch(
                                    arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream", "*/*")
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("select_zip_button")
                        ) {
                            Icon(imageVector = Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "ফাইল ব্রাউজ করুন")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sample project test buttons
                Text(
                    text = "দ্রুত যাচাইয়ের জন্য নমুনা প্রজেক্ট ব্যবহার করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { viewModel.loadSampleProject(SampleProjectType.VALID_COMPOSE_PROJECT) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sample_valid_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "সঠিক প্রজেক্ট", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.loadSampleProject(SampleProjectType.INCOMPLETE_SOURCE_ONLY) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sample_incomplete_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "অসম্পূর্ণ প্রজেক্ট", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.loadSampleProject(SampleProjectType.INVALID_APK_FILE) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sample_apk_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "APK ফাইল", fontSize = 12.sp)
                    }
                }
            }
        }

        // Validation Results Card
        if (isValidating) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "প্রজেক্টের ভেতরের ফাইলসমূহ ও Gradle কাঠামো বিশ্লেষণ করা হচ্ছে...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (validation != null) {
            val v = validation!!
            if (v.isValid) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldSuccess.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Valid",
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "প্রজেক্ট ভ্যালিডেশন সফল!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = v.bengaliSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "অ্যাপ: ${v.detectedAppName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(text = "প্যাকেজ: ${v.detectedPackageName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "টার্গেট: SDK ${v.detectedTargetSdk}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(text = "আর্काइভ: ${v.zipFileSizeFormatted}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = RoseError.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, RoseError.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Invalid",
                                tint = RoseError,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ভ্যালিডেশন ব্যর্থ হয়েছে",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RoseError
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = v.bengaliSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (v.detailedBengaliIssues.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            v.detailedBengaliIssues.forEach { issue ->
                                Text(
                                    text = "• $issue",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Build Trigger Card
        val canBuild = validation?.isValid == true && buildState != BuildState.COMPILING
        ElevatedCard(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "বিল্ড ইঞ্জিন ও প্রগ্রেস",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (workerConfig.useLocalTestWorker) "টেস্ট ওয়ার্কার সক্রিয়" else "সার্ভার ওয়ার্কার: ${workerConfig.workerUrl}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (buildState == BuildState.COMPILING) {
                        OutlinedButton(
                            onClick = { viewModel.cancelBuild() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("বাতিল")
                        }
                    }
                }

                if (buildState == BuildState.COMPILING) {
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { buildProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(buildProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.startBuild() },
                    enabled = canBuild,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechBlue,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_build_button")
                ) {
                    if (buildState == BuildState.COMPILING) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("APK বিল্ড হচ্ছে...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("APK বিল্ড শুরু করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Success Download & Install Card
        AnimatedVisibility(visible = apkInfo != null) {
            apkInfo?.let { apk ->
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.5.dp, EmeraldSuccess),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Android,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "ডিবাগ APK প্রস্তুত!",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                                Text(
                                    text = "${apk.fileName} • ${apk.fileSizeFormatted}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(12.dp)
                        ) {
                            Text(text = "প্যাকেজ আইডি: ${apk.packageName}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(text = "বিল্ড সময়: ${apk.buildTimeFormatted}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "SHA256: ${apk.sha256Checksum.take(16)}...",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Download & Direct Install
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    apk.localUri?.let { uri ->
                                        ApkInstallerHelper.saveApkToDownloads(context, uri, apk.fileName)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("download_apk_btn")
                            ) {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ডাউনলোড")
                            }

                            Button(
                                onClick = {
                                    apk.localUri?.let { uri ->
                                        if (ApkInstallerHelper.canInstallUnknownApps(context)) {
                                            ApkInstallerHelper.launchApkInstall(context, uri)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "অজানা অ্যাপ ইনস্টল করার অনুমতি প্রয়োজন। সেটিংস খোলা হচ্ছে...",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            ApkInstallerHelper.openUnknownAppsSettings(context)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("install_apk_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Android, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ইনস্টল করুন")
                            }

                            FilledTonalButton(
                                onClick = {
                                    apk.localUri?.let { uri ->
                                        ApkInstallerHelper.shareApk(context, uri, apk.fileName)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("share_apk_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "💡 টিপস: অ্যান্ড্রয়েড সিস্টেমে প্রথমবার ইনস্টল করার সময় 'Install unknown apps' অনুমতি দিতে হবে। Google Play Protect ওয়ার্নিং দেখালে 'More details > Install anyway' নির্বাচন করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Terminal Logs Viewer
        TerminalLogViewer(logs = logs)
    }
}

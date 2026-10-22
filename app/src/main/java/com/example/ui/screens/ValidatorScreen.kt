package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.TechCyan

@Composable
fun ValidatorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val validation by viewModel.validationResult.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overview Card
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(TechCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "প্রজেক্ট স্ট্রাকচার ভ্যালিডেটর",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "আসল Android Studio প্রজেক্টের প্রয়োজনীয় ফাইল নিরীক্ষণ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (validation == null) {
                    Text(
                        text = "কোনো প্রজেক্ট নির্বাচিত নেই। প্রথমে বিল্ডার ট্যাব থেকে একটি প্রজেক্ট আপলোড করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val v = validation!!
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "ফাইল নাম", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = v.zipFileName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "আর্काइভ সাইজ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = v.zipFileSizeFormatted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "মোট ফাইল সংখ্যা", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${v.totalEntriesCount} টি ফাইল", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "ভ্যালিডেশন স্ট্যাটাস", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (v.isValid) "পাস (Pass)" else "ব্যর্থ (Fail)",
                                fontWeight = FontWeight.Bold,
                                color = if (v.isValid) EmeraldSuccess else RoseError,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        validation?.let { v ->
            // Core Android Studio Files Checklist
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "প্রয়োজনীয় মূল ফাইলসমূহ:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FileCheckRow(
                        title = "settings.gradle বা settings.gradle.kts",
                        description = "রুট প্রোজেক্টের নাম ও মডিউল সংযোগ নির্ধারণ করে",
                        isFound = v.hasSettingsGradle
                    )

                    FileCheckRow(
                        title = "gradlew (Gradle Wrapper স্ক্রিপ্ট)",
                        description = "সার্ভারে ./gradlew assembleDebug এক্সিকিউট করতে প্রয়োজন",
                        isFound = v.hasGradlew
                    )

                    FileCheckRow(
                        title = "gradle/wrapper/gradle-wrapper.properties",
                        description = "সঠিক Gradle সংস্করণ (যেমন Gradle 8.x) ডাউনলোড নিশ্চিত করে",
                        isFound = v.hasGradleWrapperProperties
                    )

                    FileCheckRow(
                        title = "app/build.gradle বা app/build.gradle.kts",
                        description = "মডিউল লেভেল প্লাগইন, SDK ভার্সন ও ডিপেনডেন্সি ধারণ করে",
                        isFound = v.hasAppModule
                    )

                    FileCheckRow(
                        title = "AndroidManifest.xml",
                        description = "অ্যাপের প্যাকেজ আইডি, এক্টিভিটি ও পারমিশন ঘোষণা করে",
                        isFound = v.hasManifest
                    )
                }
            }

            // Security & Untrusted Code Guard Card
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "নিরাপত্তা ও প্রাইভেসি বিশ্লেষণ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    SecurityCheckRow(
                        title = "Path Traversal (Zip Slip) ফিল্টার",
                        status = if (!v.isPathTraversalDetected) "সুরক্ষিত (কোনো বিপজ্জনক পাথ নেই)" else "হুমকি সনাক্ত (বাতিল)",
                        isSafe = !v.isPathTraversalDetected
                    )

                    SecurityCheckRow(
                        title = "Zip Bomb ও মেমরি সুরক্ষা",
                        status = if (!v.isZipBombDetected) "সুরক্ষিত (মেমরি ও ফাইল সীমা স্বাভাবিক)" else "সীমা অতিক্রম করেছে",
                        isSafe = !v.isZipBombDetected
                    )

                    SecurityCheckRow(
                        title = "সরাসরি APK আপলোড সুরক্ষা",
                        status = if (!v.isApkDirectlyUploaded) "সুরক্ষিত (সোর্স প্রজেক্ট)" else "ত্রুটি: সোর্স নয়, শুধু APK",
                        isSafe = !v.isApkDirectlyUploaded
                    )

                    if (v.potentialMissingSecrets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberWarning.copy(alpha = 0.12f))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "কনফিগারেশন সতর্কতা:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AmberWarning)
                                }
                                v.potentialMissingSecrets.forEach { sec ->
                                    Text(text = "• $sec বিল্ড স্ক্রিপ্টে উল্লেখিত আছে", fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Detected Specifications
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "সনাক্তকৃত প্রজেক্ট স্পেসিফিকেশন:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailItem("অ্যাপ্লিকেশন নাম:", v.detectedAppName ?: "অজানা")
                    DetailItem("প্যাকেজ আইডি / Namespace:", v.detectedPackageName ?: "অজানা")
                    DetailItem("Compile SDK:", "API ${v.detectedCompileSdk ?: "34"}")
                    DetailItem("Target SDK:", "API ${v.detectedTargetSdk ?: "34"}")
                    DetailItem("Min SDK:", "API ${v.detectedMinSdk ?: "24"}")
                    DetailItem("Gradle Wrapper ভার্সন:", v.detectedGradleVersion ?: "8.x")

                    if (v.detectedLanguages.isNotEmpty() || v.detectedLibraries.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "সনাক্তকৃত প্রযুক্তি ও ফ্রেমওয়ার্ক:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (v.detectedLanguages + v.detectedLibraries).take(5).forEach { tag ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(text = tag, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileCheckRow(title: String, description: String, isFound: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (isFound) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isFound) EmeraldSuccess else RoseError,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isFound) MaterialTheme.colorScheme.onSurface else RoseError
            )
            Text(
                text = description,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SecurityCheckRow(title: String, status: String, isSafe: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
        Text(
            text = status,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSafe) EmeraldSuccess else RoseError
        )
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

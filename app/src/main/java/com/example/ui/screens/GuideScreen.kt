package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.DesktopMac
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TechBlue
import com.example.ui.theme.TechCyan
import com.example.util.ApkInstallerHelper

@Composable
fun GuideScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(TechCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ইনস্টলেশন ও ব্যবহার নির্দেশিকা",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "অ্যান্ড্রয়েড ডিভাইসে সফলভাবে APK ইনস্টল করার পদ্ধতি",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Android Installation Steps Card
        ElevatedCard(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অ্যান্ড্রয়েড ডিভাইসে ইনস্টল করার ৪টি ধাপ:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                GuideStepRow(
                    stepNumber = "১",
                    title = "APK ফাইল সংরক্ষণ বা ডাউনলোড করুন",
                    desc = "বিল্ড সম্পন্ন হওয়ার পর 'APK ডাউনলোড করুন' বা 'ইনস্টল করুন' বাটনে চাপ দিন।"
                )

                GuideStepRow(
                    stepNumber = "২",
                    title = "অজানা অ্যাপের অনুমতি (Unknown Sources) দিন",
                    desc = "অ্যান্ড্রয়েডের স্বাভাবিক নিরাপত্তা নীতি অনুযায়ী অপরিচিত সোর্স থেকে অ্যাপ ইনস্টল করার অনুমতি চাইলে 'Allow' বা অনুমতি চালু করুন।"
                )

                GuideStepRow(
                    stepNumber = "৩",
                    title = "অ্যান্ড্রয়েড ইনস্টলার স্ক্রিন পর্যালোচনা করুন",
                    desc = "অ্যাপটির নাম ও অনুমতিসমূহ দেখে নিয়ে 'Install' বাটনে ট্যাপ করুন।"
                )

                GuideStepRow(
                    stepNumber = "৪",
                    title = "Google Play Protect প্রম্পট আসলে",
                    desc = "ডিবাগ বিল্ডের কারণে 'Unrecognized app' বার্তা আসতে পারে। 'More details > Install anyway' নির্বাচন করুন।"
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { ApkInstallerHelper.openUnknownAppsSettings(context) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                    modifier = Modifier.fillMaxWidth().testTag("open_permission_settings_btn")
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("অনুমতি সেটিংস পরীক্ষা / খুলুন")
                }
            }
        }

        // Desktop to Android transfer Card
        ElevatedCard(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DesktopMac, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "কম্পিউটার বা ডেস্কটপ ব্যবহারকারীদের জন্য:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "আপনি যদি কম্পিউটার/ডেস্কটপে ফাইলটি ডাউনলোড করেন, তবে মনে রাখবেন .apk ফাইলটি রান বা ইনস্টল করতে একটি আসল Android ফোন বা ট্যাবলেটে স্থানান্তর করতে হবে।",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "• USB ক্যাবল বা Bluetooth দিয়ে ফোনে কপি করুন", fontSize = 12.sp)
                Text(text = "• অথবা Google Drive / Telegram-এ আপলোড করে ফোন থেকে ডাউনলোড করুন", fontSize = 12.sp)
                Text(text = "• ডেভেলপাররা সরাসরি কমান্ড চালাতে পারেন: adb install app-debug.apk", fontSize = 12.sp)
            }
        }

        // Privacy & Security Guarantee Card
        ElevatedCard(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "গোপনীয়তা ও নিরাপত্তা নীতি:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "• প্রতিটি প্রজেক্ট পৃথক অস্থায়ী আইসোলেটেড ওয়ার্কারে বিল্ড হয়।",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• Zip Slip ও Zip Bomb প্রতিরোধক অ্যালগরিদম সক্রিয় থাকে।",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• বিল্ড সম্পন্ন হওয়ার পর অস্থায়ী ফাইলসমূহ স্বয়ংক্রিয়ভাবে মুছে ফেলা হয়।",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• আমরা কখনোই আপনার পাসওয়ার্ড বা সাইনিং প্রাইভেট কী চাই না।",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GuideStepRow(stepNumber: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stepNumber, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(text = desc, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
        }
    }
}

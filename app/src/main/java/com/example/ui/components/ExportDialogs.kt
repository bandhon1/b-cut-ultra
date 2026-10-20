package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.VideoClip
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDark2
import com.example.ui.theme.UltraCyan
import com.example.ui.theme.UltraGreen
import com.example.ui.theme.UltraPurple
import com.example.util.TimeFormatter
import com.example.util.VideoExporter

@Composable
fun ExportProgressDialog(
    isExporting: Boolean,
    progress: Int,
    statusMessage: String,
    isBengali: Boolean
) {
    if (!isExporting) return

    Dialog(onDismissRequest = { /* Prevent dismiss while rendering */ }) {
        Surface(
            color = CardDark,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, UltraCyan),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("export_progress_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.size(80.dp),
                        color = UltraCyan,
                        trackColor = Color(0xFF26263A),
                        strokeWidth = 6.dp
                    )
                    Text(
                        text = "$progress%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (isBengali) "⏳ ভিডিও রেন্ডার হচ্ছে..." else "⏳ Rendering Video...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = statusMessage,
                    fontSize = 12.sp,
                    color = Color(0xFFA0A0BA),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ExportCompleteDialog(
    showDialog: Boolean,
    result: VideoExporter.ExportResult?,
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onPlayExported: (VideoClip) -> Unit
) {
    if (!showDialog || result == null) return
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = CardDark,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, UltraGreen),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("export_complete_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0x2210B981), CircleShape)
                        .border(1.5.dp, UltraGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = UltraGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isBengali) "ভিডিও সফলভাবে এক্সপোর্ট হয়েছে!" else "Video Exported Successfully!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isBengali) "Movies/B-CUT ফোল্ডার ও গ্যালারিতে সংরক্ষিত হয়েছে" else "Saved to Movies/B-CUT and Device Gallery",
                    fontSize = 11.sp,
                    color = UltraCyan,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = CardDark2,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "সাইজ: ${result.fileSizeFormatted} • দৈর্ঘ্য: ${TimeFormatter.formatDuration(result.durationMs)}",
                            fontSize = 11.sp,
                            color = Color(0xFFD0D0E0)
                        )
                        Text(
                            text = result.outputFile.name,
                            fontSize = 10.sp,
                            color = Color(0xFF88889A),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                            onPlayExported(
                                VideoClip(
                                    id = result.outputFile.name,
                                    title = result.outputFile.nameWithoutExtension,
                                    uri = result.contentUri ?: android.net.Uri.fromFile(result.outputFile),
                                    durationMs = result.durationMs
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UltraPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isBengali) "প্লে করো" else "Play", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val shareUri = result.contentUri ?: android.net.Uri.fromFile(result.outputFile)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "video/mp4"
                                    putExtra(Intent.EXTRA_STREAM, shareUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                            } catch (ignored: Exception) {}
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isBengali) "শেয়ার" else "Share", fontSize = 12.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333348))
                ) {
                    Text(text = if (isBengali) "ঠিক আছে (Done)" else "Done", fontSize = 12.sp, color = Color(0xFFA0A0BA))
                }
            }
        }
    }
}

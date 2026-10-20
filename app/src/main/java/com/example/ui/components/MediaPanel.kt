package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VideoClip
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark2
import com.example.ui.theme.UltraCyan
import com.example.ui.theme.UltraPurple
import com.example.ui.theme.UltraPurpleLight
import com.example.util.TimeFormatter

@Composable
fun MediaPanel(
    clips: List<VideoClip>,
    currentClip: VideoClip?,
    isBengali: Boolean,
    isLoadingSamples: Boolean,
    onImportUris: (List<Uri>) -> Unit,
    onSelectClip: (VideoClip) -> Unit,
    onRemoveClip: (String) -> Unit,
    onReloadSamples: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Android Zero-Permission Photo/Video Picker
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportUris(uris)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = UltraPurpleLight,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBengali) "📁 ফাইল ম্যানেজার" else "📁 Media Manager",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (isLoadingSamples) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = UltraCyan,
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(
                    onClick = onReloadSamples,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload Demo Clips",
                        tint = Color(0xFFA0A0BA),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Big Pick / Drop Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDark2, RoundedCornerShape(10.dp))
                .border(
                    width = 1.5.dp,
                    color = Color(0xFF3A3A52),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable {
                    mediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                }
                .padding(16.dp)
                .testTag("import_media_zone"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = UltraPurpleLight,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isBengali) "+ ভিডিও ফাইল সিলেক্ট করুন" else "+ Import Video From Device",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UltraPurpleLight
                )
                Text(
                    text = if (isBengali) "গ্যালারি থেকে MP4, MKV বা যেকোনো ভিডিও নির্বাচন করুন" else "Supports MP4, WebM, 4K & Shorts clips",
                    fontSize = 11.sp,
                    color = Color(0xFF7A7A90)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Clips List
        Text(
            text = if (isBengali) "টাইমলাইন ক্লিপসমূহ (${clips.size})" else "Project Clips (${clips.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFA0A0BA)
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(clips, key = { it.id }) { clip ->
                val isSelected = clip.id == currentClip?.id
                MediaClipItem(
                    clip = clip,
                    isSelected = isSelected,
                    onSelect = { onSelectClip(clip) },
                    onDelete = { onRemoveClip(clip.id) }
                )
            }
        }
    }
}

@Composable
fun MediaClipItem(
    clip: VideoClip,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onSelect,
        color = if (isSelected) Color(0xFF1E1A30) else CardDark2,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isSelected) UltraCyan else CardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("media_clip_${clip.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(UltraPurple, UltraCyan)),
                            shape = RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoFile,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = clip.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = "${TimeFormatter.formatDuration(clip.durationMs)} • ${clip.width}x${clip.height}",
                        fontSize = 10.sp,
                        color = Color(0xFF8E8EA8)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = UltraCyan,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 6.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Clip",
                        tint = Color(0xFF888899),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

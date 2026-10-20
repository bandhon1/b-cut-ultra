package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TextOverlay
import com.example.model.VideoClip
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDark2
import com.example.ui.theme.UltraCyan
import com.example.ui.theme.UltraPink
import com.example.ui.theme.UltraPurple
import com.example.ui.theme.UltraPurpleLight
import com.example.util.TimeFormatter

@Composable
fun TimelineView(
    clip: VideoClip?,
    textOverlay: TextOverlay?,
    currentPositionMs: Long,
    isPlaying: Boolean,
    isBengali: Boolean,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onStepTime: (Long) -> Unit,
    onSplitClip: () -> Unit,
    onTrimChange: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val durationMs = clip?.durationMs?.coerceAtLeast(1000L) ?: 6000L
    val trimStart = clip?.trimStartMs ?: 0L
    val trimEnd = clip?.trimEndMs ?: durationMs

    Surface(
        color = CardDark,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Top Transport & Controls Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause + Frame Stepping
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                brush = Brush.horizontalGradient(listOf(UltraPurple, UltraPurpleLight)),
                                shape = CircleShape
                            )
                            .testTag("timeline_play_pause_btn")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = { onStepTime(-1000L) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "-1s",
                            tint = Color(0xFFB0B0C8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onStepTime(1000L) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "+1s",
                            tint = Color(0xFFB0B0C8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Split Button (Scissors)
                    Surface(
                        onClick = onSplitClip,
                        color = CardDark2,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.testTag("timeline_split_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = "Split",
                                tint = UltraCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isBengali) "কাটুন" else "Split",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Timecode Monitor
                Surface(
                    color = Color(0xFF0F0F17),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232332))
                ) {
                    Text(
                        text = "${TimeFormatter.formatDuration(currentPositionMs, true)} / ${TimeFormatter.formatDuration(durationMs, true)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = UltraCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Multi-Track Timeline Canvas
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0D14), RoundedCornerShape(8.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                val trackWidthPx = constraints.maxWidth.toFloat()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Track V1 (Video Layer)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrackLabel(title = "V1", icon = Icons.Default.Videocam, color = UltraPurpleLight)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(CardDark2, RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF26263A), RoundedCornerShape(6.dp))
                                .pointerInput(durationMs) {
                                    detectTapGestures { offset ->
                                        val percent = (offset.x / size.width).coerceIn(0f, 1f)
                                        val targetMs = (percent * durationMs).toLong()
                                        onSeek(targetMs)
                                    }
                                }
                        ) {
                            if (clip != null) {
                                val startFrac = (trimStart.toFloat() / durationMs).coerceIn(0f, 1f)
                                val endFrac = (trimEnd.toFloat() / durationMs).coerceIn(0f, 1f)
                                val clipFracWidth = (endFrac - startFrac).coerceAtLeast(0.05f)

                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = clipFracWidth)
                                        .offset { IntOffset(x = (startFrac * trackWidthPx).toInt(), y = 0) }
                                        .background(
                                            brush = Brush.horizontalGradient(listOf(UltraPurple, UltraCyan)),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "🎬 ${clip.title}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Track TXT (Text Overlay Layer)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrackLabel(title = "TXT", icon = Icons.Default.TextFields, color = UltraPink)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(CardDark2, RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF26263A), RoundedCornerShape(6.dp))
                        ) {
                            if (textOverlay != null && textOverlay.text.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(0.9f)
                                        .background(
                                            brush = Brush.horizontalGradient(listOf(UltraPink, UltraPurple)),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "🔤 ${textOverlay.text}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Track AUD (Audio Waveform Layer)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrackLabel(title = "AUD", icon = Icons.AutoMirrored.Filled.VolumeUp, color = UltraCyan)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(CardDark2, RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF26263A), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(24) { i ->
                                    val waveHeight = (6 + (i * 7) % 12).dp
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(waveHeight)
                                            .background(UltraCyan.copy(alpha = 0.5f), RoundedCornerShape(1.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                // Global Playhead Red Needle Scrub Line
                val playheadFrac = (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)
                val playheadOffsetPx = 46.dp + (trackWidthPx - 46.dp.value * 2f).coerceAtLeast(0f).dp * playheadFrac

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .offset { IntOffset(x = (trackWidthPx * playheadFrac).toInt().coerceIn(40, trackWidthPx.toInt() - 10), y = 0) }
                        .width(3.dp)
                        .background(Color(0xFFFF3366))
                        .pointerInput(durationMs) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newX = (trackWidthPx * playheadFrac) + dragAmount.x
                                val newFrac = (newX / trackWidthPx).coerceIn(0f, 1f)
                                onSeek((newFrac * durationMs).toLong())
                            }
                        }
                )
            }

            // Trimming Handles (Range Slider for in & out points)
            if (clip != null && durationMs > 500L) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBengali) "ট্রিম শুরু: ${TimeFormatter.formatDuration(trimStart)}" else "Trim In: ${TimeFormatter.formatDuration(trimStart)}",
                        fontSize = 10.sp,
                        color = Color(0xFFA0A0BA)
                    )
                    Text(
                        text = if (isBengali) "ট্রিম শেষ: ${TimeFormatter.formatDuration(trimEnd)}" else "Trim Out: ${TimeFormatter.formatDuration(trimEnd)}",
                        fontSize = 10.sp,
                        color = Color(0xFFA0A0BA)
                    )
                }

                RangeSlider(
                    value = trimStart.toFloat()..trimEnd.toFloat(),
                    onValueChange = { range ->
                        onTrimChange(range.start.toLong(), range.endInclusive.toLong())
                    },
                    valueRange = 0f..durationMs.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = UltraCyan,
                        activeTrackColor = UltraPurple,
                        inactiveTrackColor = Color(0xFF26263A)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("timeline_trim_slider")
                )
            }
        }
    }
}

@Composable
private fun TrackLabel(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        color = Color(0xFF13131D),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .width(42.dp)
            .padding(end = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 3.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

package com.example.ui.components

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.AspectRatioMode
import com.example.model.FilterType
import com.example.model.TextOverlay
import com.example.model.TextPosition
import com.example.model.VideoClip
import com.example.ui.theme.BgDark
import com.example.ui.theme.CardDark2
import com.example.ui.theme.UltraCyan
import com.example.ui.theme.UltraPurple
import com.example.util.FilterMatrixHelper
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerView(
    clip: VideoClip?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    playbackSpeed: Float,
    currentFilter: FilterType,
    textOverlay: TextOverlay?,
    aspectRatio: AspectRatioMode,
    isMuted: Boolean,
    volume: Float,
    onTogglePlay: () -> Unit,
    onProgressUpdate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var surfaceTexture by remember { mutableStateOf<SurfaceTexture?>(null) }
    var surface by remember { mutableStateOf<Surface?>(null) }
    var isPrepared by remember { mutableStateOf(false) }

    // Maintain position polling loop
    LaunchedEffect(isPlaying, isPrepared, mediaPlayer) {
        while (isPlaying && isPrepared) {
            mediaPlayer?.let { player ->
                try {
                    val pos = player.currentPosition.toLong()
                    onProgressUpdate(pos)
                } catch (e: Exception) {
                    // player might be state-transitioning
                }
            }
            delay(100)
        }
    }

    // React to seek from outside
    LaunchedEffect(currentPositionMs) {
        mediaPlayer?.let { player ->
            try {
                val current = player.currentPosition.toLong()
                if (kotlin.math.abs(current - currentPositionMs) > 400L) {
                    player.seekTo(currentPositionMs.toInt())
                }
            } catch (ignored: Exception) {}
        }
    }

    // Playback Speed update
    LaunchedEffect(playbackSpeed, isPrepared) {
        mediaPlayer?.let { player ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isPrepared) {
                    val params = PlaybackParams().apply {
                        speed = playbackSpeed
                    }
                    player.playbackParams = params
                }
            } catch (e: Exception) {
                Log.w("VideoPlayerView", "Error applying speed $playbackSpeed: ${e.message}")
            }
        }
    }

    // Volume & Mute update
    LaunchedEffect(isMuted, volume, isPrepared) {
        mediaPlayer?.let { player ->
            try {
                if (isPrepared) {
                    val vol = if (isMuted) 0f else volume.coerceIn(0f, 1f)
                    player.setVolume(vol, vol)
                }
            } catch (ignored: Exception) {}
        }
    }

    // Play / Pause control
    LaunchedEffect(isPlaying, isPrepared) {
        mediaPlayer?.let { player ->
            try {
                if (isPrepared) {
                    if (isPlaying && !player.isPlaying) {
                        player.start()
                    } else if (!isPlaying && player.isPlaying) {
                        player.pause()
                    }
                }
            } catch (ignored: Exception) {}
        }
    }

    // Re-create media player when clip changes
    LaunchedEffect(clip?.uri, surface) {
        if (clip == null || surface == null) return@LaunchedEffect

        try {
            isPrepared = false
            mediaPlayer?.release()
            val player = MediaPlayer().apply {
                setSurface(surface)
                setDataSource(context, clip.uri)
                isLooping = true
                setOnPreparedListener { mp ->
                    isPrepared = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mp.playbackParams = PlaybackParams().apply { speed = playbackSpeed }
                    }
                    val vol = if (isMuted) 0f else volume.coerceIn(0f, 1f)
                    mp.setVolume(vol, vol)
                    mp.seekTo(clip.trimStartMs.toInt())
                    if (isPlaying) {
                        mp.start()
                    }
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e("VideoPlayerView", "MediaPlayer init failed: ${e.message}", e)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
            surface?.release()
            surface = null
        }
    }

    // Prepare ColorFilter for live video preview
    val colorFilter: ColorFilter? = remember(currentFilter) {
        FilterMatrixHelper.getColorFilter(currentFilter)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BgDark)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTogglePlay
            ),
        contentAlignment = Alignment.Center
    ) {
        if (clip == null) {
            // Empty placeholder matching web mockup
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = CardDark2,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A3A)),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.layout.Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "B",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = UltraPurple
                            )
                            Text(
                                text = "B-CUT ULTRA",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "ভিডিও আপলোড করে কাজ শুরু করুন",
                                fontSize = 13.sp,
                                color = Color(0xFF8B8BA7)
                            )
                        }
                    }
                }
            }
        } else {
            // Video Canvas Frame with custom Aspect Ratio
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .aspectRatio(aspectRatio.ratio)
                    .border(1.dp, Color(0xFF2A2A3E), RoundedCornerShape(8.dp))
                    .drawWithContent {
                        if (colorFilter != null) {
                            drawIntoCanvas { canvas ->
                                val paint = Paint().apply {
                                    this.colorFilter = colorFilter
                                }
                                canvas.saveLayer(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height), paint)
                                drawContent()
                                canvas.restore()
                            }
                        } else {
                            drawContent()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        TextureView(ctx).apply {
                            surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                                    surfaceTexture = st
                                    surface = Surface(st)
                                    mediaPlayer?.setSurface(surface)
                                }

                                override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {}

                                override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                                    surface?.release()
                                    surface = null
                                    surfaceTexture = null
                                    return true
                                }

                                override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("video_player_texture")
                )

                // Live Text & Watermark Overlay Layer
                textOverlay?.let { overlay ->
                    if (overlay.text.isNotBlank()) {
                        val alignment = when (overlay.position) {
                            TextPosition.BOTTOM -> Alignment.BottomCenter
                            TextPosition.TOP -> Alignment.TopCenter
                            TextPosition.CENTER -> Alignment.Center
                            TextPosition.TOP_RIGHT -> Alignment.TopEnd
                        }

                        val textColor = Color(overlay.textColorHex)

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = alignment
                        ) {
                            Box(
                                modifier = Modifier
                                    .then(
                                        if (overlay.hasBackgroundBox) {
                                            Modifier
                                                .background(
                                                    color = Color(0xCC11111A),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = Color(0x44FFFFFF),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        } else {
                                            Modifier.padding(4.dp)
                                        }
                                    )
                            ) {
                                Text(
                                    text = overlay.text,
                                    fontSize = overlay.fontSizeSp.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.then(
                                        if (overlay.hasDropShadow) {
                                            Modifier.shadow(8.dp)
                                        } else Modifier
                                    )
                                )
                            }
                        }
                    }
                }

                // Temporary Center Play/Pause Indicator on tap
                AnimatedVisibility(
                    visible = !isPlaying,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0x990A0A14), CircleShape)
                            .border(1.5.dp, UltraCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Top Floating Status Pill (Speed & Filter)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Surface(
                        color = Color(0xAA11111A),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0x33FFFFFF))
                    ) {
                        Text(
                            text = "${playbackSpeed}x • ${currentFilter.titleEn}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = UltraCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

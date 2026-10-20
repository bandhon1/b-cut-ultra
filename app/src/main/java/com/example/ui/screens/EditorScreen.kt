package com.example.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VideoClip
import com.example.ui.components.ControlsPanel
import com.example.ui.components.ExportCompleteDialog
import com.example.ui.components.ExportProgressDialog
import com.example.ui.components.FilterPanel
import com.example.ui.components.MediaPanel
import com.example.ui.components.ProjectsPanel
import com.example.ui.components.TextPanel
import com.example.ui.components.TimelineView
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.BgDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDark2
import com.example.ui.theme.UltraCyan
import com.example.ui.theme.UltraPink
import com.example.ui.theme.UltraPurple
import com.example.ui.theme.UltraPurpleLight
import com.example.viewmodel.EditorUiState
import com.example.viewmodel.VideoEditorViewModel

@Composable
fun EditorScreen(
    viewModel: VideoEditorViewModel,
    state: EditorUiState,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.handleImportedUris(uris)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgDark,
        contentWindowInsets = WindowInsets.systemBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EditorTopBar(
                isBengali = state.isBengali,
                onImportMediaClick = {
                    mediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                onExportClick = { viewModel.startExport() },
                onToggleLanguage = { viewModel.toggleLanguage() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Preview Player (occupies top area)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
            ) {
                VideoPlayerView(
                    clip = state.currentClip,
                    isPlaying = state.isPlaying,
                    currentPositionMs = state.currentPositionMs,
                    playbackSpeed = state.playbackSpeed,
                    currentFilter = state.currentFilter,
                    textOverlay = state.textOverlay,
                    aspectRatio = state.aspectRatio,
                    isMuted = state.isMuted,
                    volume = state.volume,
                    onTogglePlay = { viewModel.togglePlay() },
                    onProgressUpdate = { pos -> viewModel.updatePlaybackPosition(pos) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Studio Tabs Navigation Bar
            StudioTabBar(
                selectedTab = state.selectedTab,
                isBengali = state.isBengali,
                onTabSelected = { viewModel.setSelectedTab(it) }
            )

            // Active Tool Panel (Tab Content)
            Surface(
                color = CardDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                when (state.selectedTab) {
                    0 -> MediaPanel(
                        clips = state.clips,
                        currentClip = state.currentClip,
                        isBengali = state.isBengali,
                        isLoadingSamples = state.isLoadingSampleClips,
                        onImportUris = { uris -> viewModel.handleImportedUris(uris) },
                        onSelectClip = { clip -> viewModel.selectClip(clip) },
                        onRemoveClip = { id -> viewModel.removeClip(id) },
                        onReloadSamples = { viewModel.loadDefaultSampleClips() }
                    )
                    1 -> TextPanel(
                        activeOverlay = state.textOverlay,
                        isBengali = state.isBengali,
                        onApplyText = { text, pos, size, color, box, shadow ->
                            viewModel.setTextOverlay(text, pos, size, color, box, shadow)
                        },
                        onClearText = { viewModel.clearTextOverlay() }
                    )
                    2 -> FilterPanel(
                        currentFilter = state.currentFilter,
                        isBengali = state.isBengali,
                        onSelectFilter = { filter -> viewModel.setFilter(filter) }
                    )
                    3 -> ControlsPanel(
                        playbackSpeed = state.playbackSpeed,
                        aspectRatio = state.aspectRatio,
                        isMuted = state.isMuted,
                        volume = state.volume,
                        isPlaying = state.isPlaying,
                        isBengali = state.isBengali,
                        onSpeedChange = { speed -> viewModel.setPlaybackSpeed(speed) },
                        onAspectRatioChange = { ratio -> viewModel.setAspectRatio(ratio) },
                        onToggleMute = { viewModel.toggleMute() },
                        onVolumeChange = { vol -> viewModel.setVolume(vol) },
                        onTogglePlay = { viewModel.togglePlay() }
                    )
                    4 -> ProjectsPanel(
                        exportedRecords = state.exportedHistory,
                        isBengali = state.isBengali,
                        onPlayExported = { clip -> viewModel.selectClip(clip) }
                    )
                }
            }

            // Multi-track Interactive Timeline
            TimelineView(
                clip = state.currentClip,
                textOverlay = state.textOverlay,
                currentPositionMs = state.currentPositionMs,
                isPlaying = state.isPlaying,
                isBengali = state.isBengali,
                onTogglePlay = { viewModel.togglePlay() },
                onSeek = { pos -> viewModel.seekTo(pos) },
                onStepTime = { delta -> viewModel.stepPosition(delta) },
                onSplitClip = { viewModel.splitClipAtPlayhead() },
                onTrimChange = { start, end -> viewModel.updateTrim(start, end) }
            )
        }
    }

    // Export Progress Modal
    ExportProgressDialog(
        isExporting = state.isExporting,
        progress = state.exportProgress,
        statusMessage = state.exportStatusMessage,
        isBengali = state.isBengali
    )

    // Export Complete Modal
    ExportCompleteDialog(
        showDialog = state.showExportCompleteDialog,
        result = state.latestExportResult,
        isBengali = state.isBengali,
        onDismiss = { viewModel.dismissExportCompleteDialog() },
        onPlayExported = { clip -> viewModel.selectClip(clip) }
    )
}

@Composable
fun EditorTopBar(
    isBengali: Boolean,
    onImportMediaClick: () -> Unit,
    onExportClick: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    Surface(
        color = CardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding matching HTML
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Logo Icon "B"
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(UltraPurple, UltraCyan)),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "B",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "B-CUT ULTRA",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // PRO Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(listOf(UltraPurple, UltraCyan)),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "PRO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = "By Bandhon",
                        fontSize = 10.sp,
                        color = Color(0xFFA0A0BA)
                    )
                }
            }

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Language Switcher
                Surface(
                    onClick = onToggleLanguage,
                    color = CardDark2,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = UltraCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isBengali) "বাং" else "EN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Import Media Button
                Surface(
                    onClick = onImportMediaClick,
                    color = CardDark2,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.testTag("top_import_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBengali) "📁 মিডিয়া" else "📁 Import",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Export Video Button
                Surface(
                    onClick = onExportClick,
                    color = Color.Transparent,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(listOf(UltraPurple, UltraPurpleLight)),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .testTag("top_export_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBengali) "Export" else "Export",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudioTabBar(
    selectedTab: Int,
    isBengali: Boolean,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        Triple(0, if (isBengali) "🎬 মিডিয়া" else "🎬 Media", Icons.Default.Movie),
        Triple(1, if (isBengali) "🔤 টেক্সট" else "🔤 Text", Icons.Default.TextFields),
        Triple(2, if (isBengali) "✨ ফিল্টার" else "✨ Filter", Icons.Default.AutoAwesome),
        Triple(3, if (isBengali) "⚙️ কন্ট্রোল" else "⚙️ Controls", Icons.Default.Settings),
        Triple(4, if (isBengali) "📂 প্রজেক্ট" else "📂 History", Icons.Default.Folder)
    )

    Surface(
        color = Color(0xFF111119),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            tabs.forEach { (index, title, icon) ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) CardDark2 else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) UltraCyan.copy(alpha = 0.7f) else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("tab_item_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) UltraCyan else Color(0xFF8B8BA7)
                        )
                    }
                }
            }
        }
    }
}

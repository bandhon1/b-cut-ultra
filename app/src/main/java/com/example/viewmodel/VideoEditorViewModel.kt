package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AspectRatioMode
import com.example.model.ExportedVideoRecord
import com.example.model.FilterType
import com.example.model.TextOverlay
import com.example.model.TextPosition
import com.example.model.VideoClip
import com.example.util.SampleVideoGenerator
import com.example.util.TimeFormatter
import com.example.util.VideoExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class EditorUiState(
    val clips: List<VideoClip> = emptyList(),
    val currentClip: VideoClip? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val currentFilter: FilterType = FilterType.NORMAL,
    val textOverlay: TextOverlay? = null,
    val aspectRatio: AspectRatioMode = AspectRatioMode.LANDSCAPE,
    val isMuted: Boolean = false,
    val volume: Float = 1.0f,
    val selectedTab: Int = 0, // 0: Media, 1: Text, 2: Filter, 3: Controls, 4: Projects/History
    val isExporting: Boolean = false,
    val exportProgress: Int = 0,
    val exportStatusMessage: String = "",
    val latestExportResult: VideoExporter.ExportResult? = null,
    val showExportCompleteDialog: Boolean = false,
    val exportedHistory: List<ExportedVideoRecord> = emptyList(),
    val isBengali: Boolean = true,
    val isLoadingSampleClips: Boolean = false,
    val errorMessage: String? = null
)

class VideoEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        loadDefaultSampleClips()
        loadExportsHistory()
    }

    fun toggleLanguage() {
        _uiState.update { it.copy(isBengali = !it.isBengali) }
    }

    fun setSelectedTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun loadDefaultSampleClips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSampleClips = true) }
            try {
                val samples = SampleVideoGenerator.getOrCreateSampleClips(getApplication())
                _uiState.update { state ->
                    val combined = (state.clips + samples).distinctBy { it.id }
                    val active = state.currentClip ?: combined.firstOrNull()
                    state.copy(
                        clips = combined,
                        currentClip = active,
                        isLoadingSampleClips = false
                    )
                }
            } catch (e: Exception) {
                Log.e("VideoEditorViewModel", "Failed to load samples: ${e.message}")
                _uiState.update { it.copy(isLoadingSampleClips = false) }
            }
        }
    }

    fun handleImportedUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val context: Context = getApplication()
            val newClips = mutableListOf<VideoClip>()

            for (uri in uris) {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, uri)
                    val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    retriever.release()

                    val durationMs = durationStr?.toLongOrNull() ?: 10_000L
                    val width = widthStr?.toIntOrNull() ?: 1280
                    val height = heightStr?.toIntOrNull() ?: 720

                    val title = uri.lastPathSegment?.substringAfterLast('/') ?: "Clip_${System.currentTimeMillis() % 1000}"

                    newClips.add(
                        VideoClip(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            uri = uri,
                            durationMs = durationMs,
                            width = width,
                            height = height,
                            trimStartMs = 0L,
                            trimEndMs = durationMs
                        )
                    )
                } catch (e: Exception) {
                    Log.e("VideoEditorViewModel", "Failed to inspect imported video: ${e.message}")
                    newClips.add(
                        VideoClip(
                            id = UUID.randomUUID().toString(),
                            title = "Imported Video",
                            uri = uri,
                            durationMs = 15_000L,
                            width = 1280,
                            height = 720
                        )
                    )
                }
            }

            withContext(Dispatchers.Main) {
                _uiState.update { state ->
                    val updated = (state.clips + newClips).distinctBy { it.id }
                    val current = state.currentClip ?: newClips.firstOrNull()
                    state.copy(
                        clips = updated,
                        currentClip = current,
                        currentPositionMs = current?.trimStartMs ?: 0L,
                        isPlaying = true
                    )
                }
            }
        }
    }

    fun selectClip(clip: VideoClip) {
        _uiState.update {
            it.copy(
                currentClip = clip,
                currentPositionMs = clip.trimStartMs,
                isPlaying = true
            )
        }
    }

    fun removeClip(clipId: String) {
        _uiState.update { state ->
            val updated = state.clips.filterNot { it.id == clipId }
            val nextCurrent = if (state.currentClip?.id == clipId) updated.firstOrNull() else state.currentClip
            state.copy(
                clips = updated,
                currentClip = nextCurrent,
                currentPositionMs = nextCurrent?.trimStartMs ?: 0L
            )
        }
    }

    fun togglePlay() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun setPlaying(playing: Boolean) {
        _uiState.update { it.copy(isPlaying = playing) }
    }

    fun updatePlaybackPosition(posMs: Long) {
        _uiState.update { state ->
            val clip = state.currentClip
            if (clip != null && posMs > clip.trimEndMs) {
                // Loop or stop
                state.copy(currentPositionMs = clip.trimStartMs, isPlaying = true)
            } else {
                state.copy(currentPositionMs = posMs)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val clip = _uiState.value.currentClip ?: return
        val clamped = positionMs.coerceIn(clip.trimStartMs, clip.trimEndMs)
        _uiState.update { it.copy(currentPositionMs = clamped) }
    }

    fun stepPosition(deltaMs: Long) {
        val clip = _uiState.value.currentClip ?: return
        val target = (_uiState.value.currentPositionMs + deltaMs).coerceIn(clip.trimStartMs, clip.trimEndMs)
        _uiState.update { it.copy(currentPositionMs = target) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun setFilter(filter: FilterType) {
        _uiState.update { it.copy(currentFilter = filter) }
    }

    fun setAspectRatio(ratio: AspectRatioMode) {
        _uiState.update { it.copy(aspectRatio = ratio) }
    }

    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun setVolume(volume: Float) {
        _uiState.update { it.copy(volume = volume.coerceIn(0f, 2f), isMuted = volume <= 0.01f) }
    }

    fun setTextOverlay(
        text: String,
        position: TextPosition = TextPosition.BOTTOM,
        fontSizeSp: Float = 24f,
        colorHex: Long = 0xFFFFFFFF,
        hasBackground: Boolean = true,
        hasShadow: Boolean = true
    ) {
        if (text.isBlank()) {
            clearTextOverlay()
            return
        }
        val overlay = TextOverlay(
            id = UUID.randomUUID().toString(),
            text = text,
            position = position,
            fontSizeSp = fontSizeSp,
            textColorHex = colorHex,
            hasBackgroundBox = hasBackground,
            hasDropShadow = hasShadow
        )
        _uiState.update { it.copy(textOverlay = overlay) }
    }

    fun clearTextOverlay() {
        _uiState.update { it.copy(textOverlay = null) }
    }

    fun updateTrim(startMs: Long, endMs: Long) {
        val current = _uiState.value.currentClip ?: return
        val safeStart = startMs.coerceIn(0L, current.durationMs - 200L)
        val safeEnd = endMs.coerceIn(safeStart + 200L, current.durationMs)

        val updatedClip = current.copy(trimStartMs = safeStart, trimEndMs = safeEnd)
        _uiState.update { state ->
            val updatedList = state.clips.map { if (it.id == updatedClip.id) updatedClip else it }
            state.copy(
                clips = updatedList,
                currentClip = updatedClip,
                currentPositionMs = state.currentPositionMs.coerceIn(safeStart, safeEnd)
            )
        }
    }

    fun splitClipAtPlayhead() {
        val state = _uiState.value
        val clip = state.currentClip ?: return
        val playhead = state.currentPositionMs
        if (playhead <= clip.trimStartMs + 500L || playhead >= clip.trimEndMs - 500L) {
            return
        }

        val firstHalf = clip.copy(
            id = UUID.randomUUID().toString(),
            title = "${clip.title} (Part 1)",
            trimEndMs = playhead
        )
        val secondHalf = clip.copy(
            id = UUID.randomUUID().toString(),
            title = "${clip.title} (Part 2)",
            trimStartMs = playhead
        )

        val updatedList = mutableListOf<VideoClip>()
        for (item in state.clips) {
            if (item.id == clip.id) {
                updatedList.add(firstHalf)
                updatedList.add(secondHalf)
            } else {
                updatedList.add(item)
            }
        }

        _uiState.update {
            it.copy(
                clips = updatedList,
                currentClip = firstHalf,
                currentPositionMs = firstHalf.trimStartMs
            )
        }
    }

    fun startExport() {
        val clip = _uiState.value.currentClip ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isExporting = true,
                    exportProgress = 0,
                    exportStatusMessage = "Preparing video timeline..."
                )
            }

            try {
                val config = VideoExporter.ExportConfig(
                    clip = clip,
                    filter = _uiState.value.currentFilter,
                    textOverlay = _uiState.value.textOverlay,
                    playbackSpeed = _uiState.value.playbackSpeed,
                    targetWidth = if (_uiState.value.aspectRatio == AspectRatioMode.PORTRAIT) 720 else 1280,
                    targetHeight = if (_uiState.value.aspectRatio == AspectRatioMode.PORTRAIT) 1280 else 720,
                    saveToGallery = true
                )

                val result = VideoExporter.exportVideo(
                    context = getApplication(),
                    config = config,
                    onProgress = { progress, message ->
                        _uiState.update {
                            it.copy(
                                exportProgress = progress,
                                exportStatusMessage = message
                            )
                        }
                    }
                )

                // Record exported video
                val record = ExportedVideoRecord(
                    id = UUID.randomUUID().toString(),
                    title = "B-CUT_${clip.title}",
                    uri = result.contentUri ?: Uri.fromFile(result.outputFile),
                    timestamp = System.currentTimeMillis(),
                    durationMs = result.durationMs,
                    resolution = "${config.targetWidth}x${config.targetHeight}",
                    fileSizeFormatted = result.fileSizeFormatted
                )

                saveExportRecord(record)

                _uiState.update {
                    it.copy(
                        isExporting = false,
                        latestExportResult = result,
                        showExportCompleteDialog = true
                    )
                }

                loadExportsHistory()

            } catch (e: Exception) {
                Log.e("VideoEditorViewModel", "Export failed: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        errorMessage = "Export failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun dismissExportCompleteDialog() {
        _uiState.update { it.copy(showExportCompleteDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun loadExportsHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val exportsDir = File(getApplication<Application>().filesDir, "bcut_exports")
            if (exportsDir.exists()) {
                val files = exportsDir.listFiles { f -> f.extension == "mp4" } ?: emptyArray()
                val list = files.sortedByDescending { it.lastModified() }.map { f ->
                    ExportedVideoRecord(
                        id = f.name,
                        title = f.nameWithoutExtension,
                        uri = Uri.fromFile(f),
                        timestamp = f.lastModified(),
                        durationMs = 10_000L,
                        resolution = "720p HD",
                        fileSizeFormatted = TimeFormatter.formatFileSize(f.length())
                    )
                }
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(exportedHistory = list) }
                }
            }
        }
    }

    private fun saveExportRecord(record: ExportedVideoRecord) {
        _uiState.update {
            it.copy(exportedHistory = listOf(record) + it.exportedHistory)
        }
    }
}

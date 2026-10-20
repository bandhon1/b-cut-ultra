package com.example.model

import android.net.Uri

enum class FilterType(val id: String, val titleEn: String, val titleBn: String) {
    NORMAL("normal", "Normal", "স্বাভাবিক"),
    BW("bw", "B&W", "সাদাকালো"),
    SEPIA("sepia", "Vintage Sepia", "ভিন্টেজ"),
    HIGH_CONTRAST("contrast", "High Contrast", "হাই কন্ট্রাস্ট"),
    CYBERPUNK("cyberpunk", "Cyberpunk Neon", "সাইবারপাঙ্ক নিয়ন"),
    WARM("warm", "Golden Warm", "ওয়ার্ম গোল্ডেন"),
    COOL("cool", "Cinematic Cool", "কুল সিনেম্যাটিক"),
    INVERT("invert", "Invert / Negative", "ইনভার্ট"),
    VIBRANT("vibrant", "Vibrant Boost", "ভাইব্রেন্ট বুস্ট")
}

enum class TextPosition(val labelEn: String, val labelBn: String) {
    BOTTOM("Bottom Subtitle", "নিচে সাবটাইটেল"),
    TOP("Top Header", "উপরে হেডার"),
    CENTER("Center Title", "মাঝখানে টাইটেল"),
    TOP_RIGHT("Top Right Watermark", "উপরে ডানে ওয়াটারমার্ক")
}

enum class AspectRatioMode(val label: String, val ratio: Float, val iconName: String) {
    LANDSCAPE("16:9", 16f / 9f, "Landscape"),
    PORTRAIT("9:16", 9f / 16f, "Shorts / Reels"),
    SQUARE("1:1", 1f, "Square"),
    CLASSIC("4:3", 4f / 3f, "Classic")
}

data class VideoClip(
    val id: String,
    val title: String,
    val uri: Uri,
    val durationMs: Long,
    val width: Int = 1280,
    val height: Int = 720,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = durationMs
) {
    val activeDurationMs: Long
        get() = (trimEndMs - trimStartMs).coerceAtLeast(100L)
}

data class TextOverlay(
    val id: String,
    val text: String,
    val position: TextPosition = TextPosition.BOTTOM,
    val fontSizeSp: Float = 24f,
    val textColorHex: Long = 0xFFFFFFFF,
    val hasBackgroundBox: Boolean = true,
    val hasDropShadow: Boolean = true,
    val startMs: Long = 0L,
    val endMs: Long = Long.MAX_VALUE
)

data class ExportedVideoRecord(
    val id: String,
    val title: String,
    val uri: Uri,
    val timestamp: Long,
    val durationMs: Long,
    val resolution: String,
    val fileSizeFormatted: String
)

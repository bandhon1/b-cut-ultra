package com.example.util

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.example.model.FilterType

object FilterMatrixHelper {

    fun getColorMatrix(filter: FilterType): ColorMatrix {
        val matrix = ColorMatrix()
        when (filter) {
            FilterType.NORMAL -> {
                // Identity matrix
                matrix.reset()
            }
            FilterType.BW -> {
                // Luminance-preserving grayscale
                matrix.setToSaturation(0f)
            }
            FilterType.SEPIA -> {
                val sepiaValues = floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f,     0f,     0f,     1f, 0f
                )
                return ColorMatrix(sepiaValues)
            }
            FilterType.HIGH_CONTRAST -> {
                val c = 1.6f
                val t = (-0.5f * c + 0.5f) * 255f
                val contrastValues = floatArrayOf(
                    c,  0f, 0f, 0f, t,
                    0f, c,  0f, 0f, t,
                    0f, 0f, c,  0f, t,
                    0f, 0f, 0f, 1f, 0f
                )
                return ColorMatrix(contrastValues)
            }
            FilterType.CYBERPUNK -> {
                // Boost blue/cyan and purple/magenta, suppress muddy greens
                val cyberpunkValues = floatArrayOf(
                    1.4f,  -0.1f, 0.3f, 0f, 20f,
                    -0.2f, 0.8f,  0.4f, 0f, -10f,
                    0.2f,  0.3f,  1.7f, 0f, 35f,
                    0f,    0f,    0f,   1f, 0f
                )
                return ColorMatrix(cyberpunkValues)
            }
            FilterType.WARM -> {
                val warmValues = floatArrayOf(
                    1.25f, 0f,    0f,    0f, 20f,
                    0f,    1.05f, 0f,    0f, 10f,
                    0f,    0f,    0.85f, 0f, -15f,
                    0f,    0f,    0f,    1f, 0f
                )
                return ColorMatrix(warmValues)
            }
            FilterType.COOL -> {
                val coolValues = floatArrayOf(
                    0.85f, 0f,    0f,    0f, -15f,
                    0f,    1.0f,  0f,    0f, 5f,
                    0f,    0f,    1.35f, 0f, 25f,
                    0f,    0f,    0f,    1f, 0f
                )
                return ColorMatrix(coolValues)
            }
            FilterType.INVERT -> {
                val invertValues = floatArrayOf(
                    -1f, 0f,  0f,  0f, 255f,
                    0f,  -1f, 0f,  0f, 255f,
                    0f,  0f,  -1f, 0f, 255f,
                    0f,  0f,  0f,  1f, 0f
                )
                return ColorMatrix(invertValues)
            }
            FilterType.VIBRANT -> {
                matrix.setToSaturation(1.6f)
            }
        }
        return matrix
    }

    fun getColorFilter(filter: FilterType): ColorFilter? {
        return if (filter == FilterType.NORMAL) {
            null
        } else {
            ColorFilter.colorMatrix(getColorMatrix(filter))
        }
    }
}

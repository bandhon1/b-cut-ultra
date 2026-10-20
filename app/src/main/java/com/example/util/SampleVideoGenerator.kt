package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.model.VideoClip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

object SampleVideoGenerator {
    private const val TAG = "SampleVideoGenerator"

    suspend fun getOrCreateSampleClips(context: Context): List<VideoClip> = withContext(Dispatchers.IO) {
        val sampleDir = File(context.cacheDir, "sample_videos")
        if (!sampleDir.exists()) sampleDir.mkdirs()

        val clip1File = File(sampleDir, "bcut_cyberpunk_intro.mp4")
        val clip2File = File(sampleDir, "bcut_countdown_motion.mp4")

        val list = mutableListOf<VideoClip>()

        if (!clip1File.exists() || clip1File.length() < 1000) {
            generateClip(
                file = clip1File,
                title = "Cyberpunk Neon Intro",
                durationSeconds = 6,
                theme = 1
            )
        }

        if (clip1File.exists() && clip1File.length() > 0) {
            list.add(
                VideoClip(
                    id = "sample_1",
                    title = "B-CUT Neon Intro",
                    uri = Uri.fromFile(clip1File),
                    durationMs = 6000L,
                    width = 1280,
                    height = 720
                )
            )
        }

        if (!clip2File.exists() || clip2File.length() < 1000) {
            generateClip(
                file = clip2File,
                title = "Motion Countdown 4K",
                durationSeconds = 5,
                theme = 2
            )
        }

        if (clip2File.exists() && clip2File.length() > 0) {
            list.add(
                VideoClip(
                    id = "sample_2",
                    title = "Motion Countdown",
                    uri = Uri.fromFile(clip2File),
                    durationMs = 5000L,
                    width = 1280,
                    height = 720
                )
            )
        }

        list
    }

    private fun generateClip(
        file: File,
        title: String,
        durationSeconds: Int,
        theme: Int
    ) {
        val width = 1280
        val height = 720
        val fps = 30
        val totalFrames = durationSeconds * fps
        val bitRate = 2_500_000
        val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC

        try {
            val format = MediaFormat.createVideoFormat(mimeType, width, height).apply {
                setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                )
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            val encoder = MediaCodec.createEncoderByType(mimeType)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = encoder.createInputSurface()
            encoder.start()

            val muxer = MediaMuxer(file.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var videoTrackIndex = -1
            var muxerStarted = false

            val bufferInfo = MediaCodec.BufferInfo()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            for (frame in 0 until totalFrames) {
                // Render frame graphics
                drawFrame(canvas, paint, width, height, frame, totalFrames, title, theme)

                // Push bitmap to codec surface
                val surfaceCanvas = inputSurface.lockCanvas(null)
                surfaceCanvas.drawBitmap(bitmap, 0f, 0f, null)
                inputSurface.unlockCanvasAndPost(surfaceCanvas)

                // Drain encoder
                while (true) {
                    val status = encoder.dequeueOutputBuffer(bufferInfo, 10_000L)
                    if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break
                    } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (muxerStarted) {
                            throw RuntimeException("Format changed twice")
                        }
                        videoTrackIndex = muxer.addTrack(encoder.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    } else if (status >= 0) {
                        val outputBuffer = encoder.getOutputBuffer(status)
                        if (outputBuffer != null && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                            if (bufferInfo.size > 0 && muxerStarted) {
                                outputBuffer.position(bufferInfo.offset)
                                outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                                muxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo)
                            }
                        }
                        encoder.releaseOutputBuffer(status, false)
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            break
                        }
                    }
                }
            }

            // Signal End of Stream
            encoder.signalEndOfInputStream()

            // Drain remaining EOS
            var eosReached = false
            while (!eosReached) {
                val status = encoder.dequeueOutputBuffer(bufferInfo, 50_000L)
                if (status >= 0) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        eosReached = true
                    }
                    val outputBuffer = encoder.getOutputBuffer(status)
                    if (outputBuffer != null && bufferInfo.size > 0 && muxerStarted) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(status, false)
                } else if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break
                }
            }

            encoder.stop()
            encoder.release()
            inputSurface.release()

            if (muxerStarted) {
                muxer.stop()
            }
            muxer.release()
            bitmap.recycle()
            Log.d(TAG, "Generated sample clip at: ${file.absolutePath} size=${file.length()}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate sample clip: ${e.message}", e)
        }
    }

    private fun drawFrame(
        canvas: Canvas,
        paint: Paint,
        w: Int,
        h: Int,
        frame: Int,
        totalFrames: Int,
        title: String,
        theme: Int
    ) {
        val progress = frame.toFloat() / totalFrames
        val t = frame * 0.05f

        if (theme == 1) {
            // Cyberpunk Dark Background
            canvas.drawColor(Color.rgb(10, 10, 20))

            // Animated Neon Grid Lines
            paint.strokeWidth = 2f
            paint.style = Paint.Style.STROKE
            paint.color = Color.argb(40, 0, 245, 255)
            val gridStep = 60
            val offset = (frame * 2) % gridStep
            for (y in 0..h step gridStep) {
                canvas.drawLine(0f, (y + offset).toFloat(), w.toFloat(), (y + offset).toFloat(), paint)
            }
            for (x in 0..w step gridStep) {
                canvas.drawLine(x.toFloat(), 0f, x.toFloat(), h.toFloat(), paint)
            }

            // Glowing Orbiting Circles
            paint.style = Paint.Style.FILL
            val cx = w / 2f
            val cy = h / 2f - 40f
            val radius = 140f

            for (i in 0 until 8) {
                val angle = t + (i * Math.PI / 4).toFloat()
                val px = cx + cos(angle) * (radius + sin(t * 2 + i) * 30f)
                val py = cy + sin(angle) * (radius * 0.6f + cos(t * 2 + i) * 20f)
                paint.color = if (i % 2 == 0) Color.rgb(0, 245, 255) else Color.rgb(168, 85, 247)
                canvas.drawCircle(px, py, 14f + sin(t + i) * 6f, paint)
            }

            // Big "B" Emblem
            paint.color = Color.rgb(124, 58, 237)
            canvas.drawCircle(cx, cy, 75f, paint)
            paint.color = Color.WHITE
            paint.textSize = 80f
            paint.textAlign = Paint.Align.CENTER
            paint.isFakeBoldText = true
            canvas.drawText("B", cx, cy + 28f, paint)

            // Title & Subtitle
            paint.textSize = 42f
            paint.color = Color.rgb(0, 245, 255)
            canvas.drawText("B-CUT ULTRA", cx, cy + 150f, paint)

            paint.textSize = 24f
            paint.color = Color.rgb(200, 200, 220)
            canvas.drawText("By Bandhon • Cyberpunk Studio 4K", cx, cy + 195f, paint)

            // Dynamic Timecode
            val currentSec = (frame / 30f)
            paint.textSize = 26f
            paint.color = Color.rgb(255, 0, 127)
            canvas.drawText(String.format("REC 00:0%d:%02d", currentSec.toInt(), ((currentSec % 1) * 100).toInt()), cx, cy + 240f, paint)

        } else {
            // Motion Countdown Theme
            canvas.drawColor(Color.rgb(15, 23, 42))

            val cx = w / 2f
            val cy = h / 2f

            // Outer pulse ring
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 8f
            paint.color = Color.rgb(99, 102, 241)
            canvas.drawCircle(cx, cy, 180f + sin(t * 3) * 15f, paint)

            // Sweep arc progress
            paint.strokeWidth = 14f
            paint.color = Color.rgb(0, 245, 255)
            val sweepAngle = 360f * (1f - (frame % 30) / 30f)
            canvas.drawArc(cx - 150f, cy - 150f, cx + 150f, cy + 150f, -90f, sweepAngle, false, paint)

            // Countdown number
            val remainingSec = (totalFrames - frame) / 30 + 1
            paint.style = Paint.Style.FILL
            paint.textSize = 110f
            paint.textAlign = Paint.Align.CENTER
            paint.color = Color.WHITE
            paint.isFakeBoldText = true
            canvas.drawText("$remainingSec", cx, cy + 38f, paint)

            // Label
            paint.textSize = 34f
            paint.color = Color.rgb(168, 85, 247)
            canvas.drawText("MOTION GRAPHICS PRO", cx, cy + 230f, paint)
        }

        // Bottom Progress Bar
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(100, 50, 50, 70)
        canvas.drawRect(60f, h - 50f, w - 60f, h - 35f, paint)

        paint.color = Color.rgb(0, 245, 255)
        canvas.drawRect(60f, h - 50f, 60f + (w - 120f) * progress, h - 35f, paint)
    }
}

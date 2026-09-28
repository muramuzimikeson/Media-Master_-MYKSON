package com.example.data.sample

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

object SampleVideoGenerator {
    private const val TAG = "SampleVideoGenerator"

    fun generateSampleMp4(
        outputFile: File,
        durationSeconds: Int = 6,
        width: Int = 640,
        height: Int = 480
    ): Boolean {
        if (outputFile.exists() && outputFile.length() > 5000) {
            return true
        }

        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var surface: android.view.Surface? = null
        var muxerStarted = false

        try {
            val mime = MediaFormat.MIMETYPE_VIDEO_AVC
            val format = MediaFormat.createVideoFormat(mime, width, height).apply {
                setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                )
                setInteger(MediaFormat.KEY_BIT_RATE, 1_500_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            codec = MediaCodec.createEncoderByType(mime)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            surface = codec.createInputSurface()
            codec.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var videoTrackIndex = -1

            val bufferInfo = MediaCodec.BufferInfo()
            val totalFrames = durationSeconds * 30
            val frameDurationUs = 1_000_000L / 30

            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 34f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#38BDF8")
                textSize = 20f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            val timerPaint = Paint().apply {
                color = Color.parseColor("#F59E0B")
                textSize = 24f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            val barPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }

            for (frame in 0 until totalFrames) {
                val canvas = surface.lockCanvas(null)
                if (canvas != null) {
                    val t = frame / 30f
                    val cx = width / 2f
                    val cy = height / 2f
                    val pulse = (sin(t * 3.0) * 0.2 + 0.8).toFloat()

                    val bgGradient = RadialGradient(
                        cx + (sin(t * 1.5) * 120).toFloat(),
                        cy + (cos(t * 1.5) * 60).toFloat(),
                        (width * 0.7f * pulse),
                        Color.parseColor("#0F172A"),
                        Color.parseColor("#020617"),
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawPaint(Paint().apply { shader = bgGradient })

                    // Visualizer bars
                    val numBars = 20
                    val barWidth = 14f
                    val spacing = 8f
                    val totalBarsWidth = numBars * (barWidth + spacing)
                    val startX = (width - totalBarsWidth) / 2f

                    for (b in 0 until numBars) {
                        val barHeight = (sin(t * 6.0 + b * 0.4) * 50 + 70).toFloat() * pulse
                        val barX = startX + b * (barWidth + spacing)
                        val barY = cy + 40f

                        val hue = (b * 15 + (t * 50).toInt()) % 360
                        barPaint.color = Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.8f, 0.95f))
                        canvas.drawRoundRect(
                            barX, barY - barHeight / 2f,
                            barX + barWidth, barY + barHeight / 2f,
                            6f, 6f, barPaint
                        )
                    }

                    // Text labels
                    canvas.drawText("MediaMaster_#MYKSON#", cx, cy - 90f, titlePaint)
                    canvas.drawText("Offline Cinema Player", cx, cy - 55f, subtitlePaint)

                    val sec = frame / 30
                    val ms = (frame % 30) * 33
                    val timeStr = String.format("00:%02d.%02d / 00:%02d.00", sec, ms / 10, durationSeconds)
                    canvas.drawText(timeStr, cx, cy + 120f, timerPaint)

                    surface.unlockCanvasAndPost(canvas)
                }

                // Drain output with 10ms timeout
                while (true) {
                    val status = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                    if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (!muxerStarted) {
                            videoTrackIndex = muxer.addTrack(codec.outputFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                    } else if (status >= 0) {
                        val encodedData = codec.getOutputBuffer(status)
                        if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            bufferInfo.presentationTimeUs = frame * frameDurationUs
                            muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                        }
                        codec.releaseOutputBuffer(status, false)
                    } else {
                        break
                    }
                }
            }

            codec.signalEndOfInputStream()

            // Drain remaining with generous timeout
            var eos = false
            var attempts = 0
            while (!eos && attempts < 80) {
                attempts++
                val status = codec.dequeueOutputBuffer(bufferInfo, 20_000)
                if (status >= 0) {
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        eos = true
                    }
                    if (bufferInfo.size > 0 && muxerStarted) {
                        val encodedData = codec.getOutputBuffer(status)
                        if (encodedData != null) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                        }
                    }
                    codec.releaseOutputBuffer(status, false)
                } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (!muxerStarted) {
                        videoTrackIndex = muxer.addTrack(codec.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                } else if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (eos) break
                }
            }

            val success = muxerStarted && outputFile.exists() && outputFile.length() > 5000
            if (success) {
                Log.i(TAG, "Sample MP4 successfully created at: ${outputFile.absolutePath} (${outputFile.length()} bytes)")
            } else {
                Log.w(TAG, "Sample MP4 generation did not produce sufficient bytes")
                outputFile.delete()
            }
            return success
        } catch (e: Exception) {
            Log.e(TAG, "Failed generating MP4: ${e.message}", e)
            try {
                if (outputFile.exists()) outputFile.delete()
            } catch (ignored: Exception) {}
            return false
        } finally {
            try { surface?.release() } catch (ignored: Exception) {}
            try { codec?.stop() } catch (ignored: Exception) {}
            try { codec?.release() } catch (ignored: Exception) {}
            if (muxerStarted) {
                try {
                    muxer?.stop()
                } catch (e: Exception) {
                    Log.w(TAG, "Muxer stop ignored: ${e.message}")
                }
            }
            try { muxer?.release() } catch (ignored: Exception) {}
        }
    }
}

package com.example.data.sample

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object WavAudioGenerator {

    /**
     * Generates a pleasant synthesized WAV audio file with specified melody style.
     */
    fun generateTrack(
        outputFile: File,
        durationSeconds: Int = 20,
        style: TrackStyle = TrackStyle.LOFI_CHILL
    ) {
        if (outputFile.exists() && outputFile.length() > 1000) {
            return
        }

        val sampleRate = 44100
        val numChannels = 2
        val bitsPerSample = 16
        val totalSamples = sampleRate * durationSeconds
        val bytesPerSample = bitsPerSample / 8
        val dataSize = totalSamples * numChannels * bytesPerSample

        val buffer = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)

        val tempoBpm = when (style) {
            TrackStyle.LOFI_CHILL -> 80
            TrackStyle.CYBER_PULSE -> 124
            TrackStyle.ACOUSTIC_DAWN -> 95
        }
        val beatDuration = 60.0 / tempoBpm
        val chordNotes = when (style) {
            TrackStyle.LOFI_CHILL -> listOf(
                listOf(261.63, 329.63, 392.00, 493.88), // Cmaj7
                listOf(220.00, 261.63, 329.63, 392.00), // Am7
                listOf(174.61, 220.00, 261.63, 329.63), // Fmaj7
                listOf(196.00, 246.94, 293.66, 349.23)  // G7
            )
            TrackStyle.CYBER_PULSE -> listOf(
                listOf(146.83, 220.00, 293.66, 369.99), // D min/maj
                listOf(130.81, 196.00, 261.63, 329.63), // C
                listOf(116.54, 174.61, 233.08, 293.66), // Bb
                listOf(146.83, 220.00, 293.66, 440.00)  // Dm
            )
            TrackStyle.ACOUSTIC_DAWN -> listOf(
                listOf(329.63, 392.00, 493.88, 587.33), // Em7
                listOf(261.63, 329.63, 392.00, 523.25), // C
                listOf(196.00, 246.94, 293.66, 392.00), // G
                listOf(293.66, 369.99, 440.00, 587.33)  // D
            )
        }

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val currentBeat = t / beatDuration
            val chordIndex = ((currentBeat / 4).toInt()) % chordNotes.size
            val activeChord = chordNotes[chordIndex]
            val beatInBar = (currentBeat % 1.0)

            // Chord pad synthesis
            var leftWave = 0.0
            var rightWave = 0.0

            activeChord.forEachIndexed { noteIdx, freq ->
                val pan = (noteIdx.toDouble() / (activeChord.size - 1)) * 0.6 + 0.2 // 0.2 to 0.8 pan
                val harmonic1 = sin(2.0 * PI * freq * t)
                val harmonic2 = sin(2.0 * PI * freq * 2.0 * t) * 0.3
                val harmonic3 = sin(2.0 * PI * freq * 0.5 * t) * 0.2
                val noteVoice = (harmonic1 + harmonic2 + harmonic3) * 0.15

                leftWave += noteVoice * (1.0 - pan)
                rightWave += noteVoice * pan
            }

            // Lead arpeggio melody
            val arpNoteIndex = ((currentBeat * 2).toInt()) % activeChord.size
            val leadFreq = activeChord[arpNoteIndex] * 2.0
            val noteEnvelope = exp(-3.0 * beatInBar)
            val leadWave = sin(2.0 * PI * leadFreq * t) * noteEnvelope * 0.12
            leftWave += leadWave * 0.7
            rightWave += leadWave * 0.3

            // Bass pulse
            val bassFreq = activeChord[0] * 0.5
            val bassWave = sin(2.0 * PI * bassFreq * t) * 0.22
            leftWave += bassWave
            rightWave += bassWave

            // Percussive click/tick on each beat
            val beatTick = exp(-30.0 * beatInBar) * sin(2.0 * PI * 180.0 * t) * 0.08
            leftWave += beatTick
            rightWave += beatTick

            // Master envelope fade in / fade out
            val masterEnvelope = when {
                t < 1.0 -> t
                t > durationSeconds - 1.5 -> (durationSeconds - t) / 1.5
                else -> 1.0
            }.coerceIn(0.0, 1.0)

            val leftFinal = (leftWave * masterEnvelope * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
            val rightFinal = (rightWave * masterEnvelope * 28000.0).toInt().coerceIn(-32767, 32767).toShort()

            buffer.putShort(leftFinal)
            buffer.putShort(rightFinal)
        }

        FileOutputStream(outputFile).use { fos ->
            writeWavHeader(fos, totalSamples, sampleRate, numChannels, bitsPerSample)
            fos.write(buffer.array())
        }
    }

    private fun writeWavHeader(
        out: FileOutputStream,
        totalAudioFrames: Int,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ) {
        val bytesPerSample = bitsPerSample / 8
        val dataChunkSize = totalAudioFrames * channels * bytesPerSample
        val totalFileSize = 36 + dataChunkSize

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        // RIFF header
        header.put('R'.code.toByte()).put('I'.code.toByte()).put('F'.code.toByte()).put('F'.code.toByte())
        header.putInt(totalFileSize)
        header.put('W'.code.toByte()).put('A'.code.toByte()).put('V'.code.toByte()).put('E'.code.toByte())

        // fmt chunk
        header.put('f'.code.toByte()).put('m'.code.toByte()).put('t'.code.toByte()).put(' '.code.toByte())
        header.putInt(16) // Subchunk1Size for PCM
        header.putShort(1.toShort()) // AudioFormat 1 = PCM
        header.putShort(channels.toShort())
        header.putInt(sampleRate)
        header.putInt(sampleRate * channels * bytesPerSample) // ByteRate
        header.putShort((channels * bytesPerSample).toShort()) // BlockAlign
        header.putShort(bitsPerSample.toShort())

        // data chunk
        header.put('d'.code.toByte()).put('a'.code.toByte()).put('t'.code.toByte()).put('a'.code.toByte())
        header.putInt(dataChunkSize)

        out.write(header.array())
    }

    enum class TrackStyle {
        LOFI_CHILL,
        CYBER_PULSE,
        ACOUSTIC_DAWN
    }
}

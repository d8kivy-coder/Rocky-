package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Zero-permission procedural PCM arcade synthesizer for authentic slot cabinet & ringside cues.
 */
class SlotSoundEngine {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  @Volatile var enabled: Boolean = true

  fun playButtonClick() {
    if (!enabled) return
    playToneSequence(listOf(ToneStep(freqHz = 780.0, durationMs = 24, amplitude = 0.22)))
  }

  fun playSpinStart() {
    if (!enabled) return
    playToneSequence(
      listOf(
        ToneStep(240.0, 45, 0.25),
        ToneStep(340.0, 45, 0.25),
        ToneStep(460.0, 55, 0.22),
      )
    )
  }

  fun playReelStop(reelIndex: Int) {
    if (!enabled) return
    val baseFreq = 155.0 + reelIndex * 22.0
    playToneSequence(listOf(ToneStep(baseFreq, 55, 0.35), ToneStep(baseFreq * 0.72, 35, 0.18)))
  }

  fun playWinJingle(isBigWin: Boolean) {
    if (!enabled) return
    val notes =
      if (isBigWin) {
        listOf(
          ToneStep(523.25, 85, 0.30), // C5
          ToneStep(659.25, 85, 0.30), // E5
          ToneStep(783.99, 85, 0.32), // G5
          ToneStep(1046.50, 140, 0.35), // C6
          ToneStep(783.99, 80, 0.28), // G5
          ToneStep(1046.50, 220, 0.36), // C6
        )
      } else {
        listOf(
          ToneStep(523.25, 70, 0.24),
          ToneStep(659.25, 70, 0.24),
          ToneStep(783.99, 130, 0.28),
        )
      }
    playToneSequence(notes)
  }

  fun playRockyFanfare() {
    if (!enabled) return
    // Iconic triumphant boxing horn fanfare cadence
    playToneSequence(
      listOf(
        ToneStep(392.00, 110, 0.32), // G4
        ToneStep(523.25, 110, 0.34), // C5
        ToneStep(659.25, 110, 0.34), // E5
        ToneStep(783.99, 180, 0.36), // G5
        ToneStep(659.25, 90, 0.30), // E5
        ToneStep(1046.50, 300, 0.38), // C6
      )
    )
  }

  fun playBoxingBell() {
    if (!enabled) return
    // Metallic bell harmonics simulated via bright high-frequency shimmer
    playToneSequence(
      listOf(
        ToneStep(1180.0, 140, 0.34),
        ToneStep(0.0, 40, 0.0),
        ToneStep(1180.0, 220, 0.34),
      )
    )
  }

  fun playPunchImpact(isKnockout: Boolean) {
    if (!enabled) return
    if (isKnockout) {
      playToneSequence(
        listOf(
          ToneStep(110.0, 120, 0.45),
          ToneStep(85.0, 140, 0.40),
          ToneStep(1180.0, 240, 0.32),
        )
      )
    } else {
      playToneSequence(listOf(ToneStep(130.0, 75, 0.35), ToneStep(95.0, 55, 0.22)))
    }
  }

  fun playCardFlip(won: Boolean) {
    if (!enabled) return
    if (won) {
      playToneSequence(listOf(ToneStep(660.0, 70, 0.26), ToneStep(990.0, 150, 0.30)))
    } else {
      playToneSequence(listOf(ToneStep(280.0, 90, 0.26), ToneStep(185.0, 180, 0.24)))
    }
  }

  private data class ToneStep(val freqHz: Double, val durationMs: Int, val amplitude: Double)

  private fun playToneSequence(steps: List<ToneStep>) {
    scope.launch {
      try {
        val sampleRate = 22050
        val totalSamples = steps.sumOf { (sampleRate * it.durationMs) / 1000 }.coerceAtLeast(1)
        val buffer = ShortArray(totalSamples)
        var cursor = 0

        for (step in steps) {
          val stepSamples = (sampleRate * step.durationMs) / 1000
          for (i in 0 until stepSamples) {
            if (cursor >= totalSamples) break
            val sample =
              if (step.freqHz <= 0.0) {
                0
              } else {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / stepSamples.coerceAtLeast(1)
                val envelope =
                  when {
                    progress < 0.08 -> progress / 0.08
                    else -> exp(-3.5 * (progress - 0.08))
                  }
                val wave =
                  sin(2.0 * PI * step.freqHz * t) + 0.25 * sin(4.0 * PI * step.freqHz * t)
                (wave * envelope * step.amplitude * Short.MAX_VALUE * 0.75)
                  .toInt()
                  .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                  .toShort()
              }
            buffer[cursor++] = sample
          }
        }

        val track =
          AudioTrack.Builder()
            .setAudioAttributes(
              AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            )
            .setAudioFormat(
              AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(buffer.size * 2)
            .build()

        track.write(buffer, 0, buffer.size)
        track.play()
        val totalMs = steps.sumOf { it.durationMs }.toLong() + 60L
        kotlinx.coroutines.delay(totalMs)
        track.stop()
        track.release()
      } catch (_: Throwable) {
        // Ignore audio hardware failures on headless or muted test environments
      }
    }
  }
}

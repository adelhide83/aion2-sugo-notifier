package com.sugo.vibealarm

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object VibrationHelper {
    fun vibratePattern(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (!vibrator.hasVibrator()) return

        // "웅웅  웅웅" 느낌: 짧은 진동 2개를 2세트 반복
        val timings = longArrayOf(
            0L,
            220L, 140L, 220L, 420L,
            220L, 140L, 220L
        )
        val amplitudes = intArrayOf(
            0,
            255, 0, 255, 0,
            255, 0, 255
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            val alarmAudio = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            vibrator.vibrate(effect, alarmAudio)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings, -1)
        }
    }
}

package com.sugo.vibealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val powerManager = context.getSystemService(PowerManager::class.java)
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "vibealarm:alarm_wakelock"
        )
        wakeLock.acquire(10_000L)

        val isOneTime = intent?.getBooleanExtra(AlarmScheduler.EXTRA_ONE_TIME, false) == true
        val scheduledMinute = intent?.getIntExtra(AlarmScheduler.EXTRA_SCHEDULED_MINUTE, -1) ?: -1
        try {
            VibrationHelper.vibratePattern(context)
            AlarmNotifier.notifyAlarm(context, isOneTime, scheduledMinute)
            if (!isOneTime) {
                AlarmScheduler.scheduleNext(context)
            }
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}

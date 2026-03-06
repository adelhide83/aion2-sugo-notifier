package com.sugo.vibealarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object AlarmScheduler {
    private const val REQUEST_CODE_ALARM = 161949
    private const val REQUEST_CODE_ONE_TIME_ALARM = 271949
    const val EXTRA_ONE_TIME = "extra_one_time_alarm"
    const val EXTRA_SCHEDULED_MINUTE = "extra_scheduled_minute"
    private val ACTIVE_HOURS = 10..22
    private val ACTIVE_SLOTS = arrayOf(
        Slot(16, 0),
        Slot(21, 20), // reward time +20 seconds
        Slot(46, 0),
        Slot(50, 20)  // reward time +20 seconds
    )

    private data class Slot(
        val minute: Int,
        val second: Int
    )

    fun scheduleNext(context: Context): Long {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val nextMillis = getNextAlarmTimeMillis()
        val pendingIntent = alarmPendingIntent(context, nextMillis)
        scheduleReliableWakeup(context, alarmManager, nextMillis, pendingIntent)
        return nextMillis
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(alarmPendingIntent(context, System.currentTimeMillis()))
    }

    fun scheduleOneTime(context: Context, triggerAtMillis: Long): Long {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = oneTimePendingIntent(context, triggerAtMillis)
        scheduleReliableWakeup(context, alarmManager, triggerAtMillis, pendingIntent)
        return triggerAtMillis
    }

    private fun alarmPendingIntent(context: Context, triggerAtMillis: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_SCHEDULED_MINUTE, extractMinute(triggerAtMillis))
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun oneTimePendingIntent(context: Context, triggerAtMillis: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ONE_TIME, true)
            putExtra(EXTRA_SCHEDULED_MINUTE, extractMinute(triggerAtMillis))
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ONE_TIME_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun extractMinute(triggerAtMillis: Long): Int {
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(triggerAtMillis), ZoneId.systemDefault()).minute
    }

    private fun scheduleReliableWakeup(
        context: Context,
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        operation: PendingIntent
    ) {
        if (isExactAlarmAllowed(context)) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                operation
            )
            return
        }

        // Fallback for strict devices when exact permission is denied.
        val showIntent = appLaunchPendingIntent(context)
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent),
            operation
        )
    }

    private fun appLaunchPendingIntent(context: Context): PendingIntent {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun isExactAlarmAllowed(context: Context): Boolean {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun getNextAlarmTimeMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        return computeNextAlarmTimeMillis(nowMillis)
    }

    private fun computeNextAlarmTimeMillis(nowMillis: Long): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(nowMillis), zone)

        // Search forward for the first slot strictly after current moment.
        var date = LocalDate.from(now)
        repeat(366) {
            for (hour in ACTIVE_HOURS) {
                for (slot in ACTIVE_SLOTS) {
                    val candidate = date.atTime(hour, slot.minute, slot.second)
                    if (candidate.isAfter(now)) {
                        return candidate.atZone(zone).toInstant().toEpochMilli()
                    }
                }
            }
            date = date.plusDays(1)
        }
        throw IllegalStateException("No next alarm time found")
    }
}

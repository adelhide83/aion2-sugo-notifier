package com.sugo.vibealarm

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object AlarmNotifier {
    private const val CHANNEL_ID = "vibe_alarm_channel_v2"
    private const val CHANNEL_NAME = "Vibe Alarm"
    private const val CHANNEL_DESCRIPTION = "Alarm vibration notifications"

    fun notifyAlarm(context: Context, isOneTime: Boolean, scheduledMinute: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createChannelIfNeeded(context)
        val titleRes = if (isOneTime) R.string.notification_title_one_time else R.string.notification_title_regular
        val bodyRes = when (scheduledMinute) {
            16, 46 -> R.string.notification_text_start_time
            21, 50 -> R.string.notification_text_reward_time
            else -> R.string.notification_text
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(titleRes))
            .setContentText(context.getString(bodyRes))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSilent(false)
            .setVibrate(
                longArrayOf(
                    0L,
                    220L, 140L, 220L, 420L,
                    220L, 140L, 220L
                )
            )
            .build()

        NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }

    fun notifyWatchTest(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createChannelIfNeeded(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.notification_title_watch_test))
            .setContentText(context.getString(R.string.notification_text_watch_test))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSilent(false)
            .setVibrate(
                longArrayOf(
                    0L,
                    220L, 140L, 220L, 420L,
                    220L, 140L, 220L
                )
            )
            .build()

        NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }

    private fun createChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val alarmAudio = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            description = CHANNEL_DESCRIPTION
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(true)
            setSound(alarmSoundUri, alarmAudio)
            vibrationPattern = longArrayOf(
                0L,
                220L, 140L, 220L, 420L,
                220L, 140L, 220L
            )
        }
        manager.createNotificationChannel(channel)
    }
}

package com.sugo.vibealarm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 1001
    }

    private lateinit var statusText: TextView
    private val displayFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
    private val oneTimeInputFormatter = DateTimeFormatter.ofPattern("H:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestNotificationPermissionIfNeeded()

        statusText = findViewById(R.id.statusText)
        val startButton: Button = findViewById(R.id.startButton)
        val stopButton: Button = findViewById(R.id.stopButton)

        startButton.setOnClickListener {
            val nextMillis = AlarmScheduler.scheduleNext(this)
            updateStatus(nextMillis)
        }

        stopButton.setOnClickListener {
            AlarmScheduler.cancel(this)
            statusText.text = getString(R.string.stopped)
        }

        updateStatus()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_POST_NOTIFICATIONS
        )
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus(forcedNextMillis: Long? = null) {
        val exactAllowed = AlarmScheduler.isExactAlarmAllowed(this)

        val nextMillis = forcedNextMillis ?: AlarmScheduler.getNextAlarmTimeMillis()
        val nextText = Instant.ofEpochMilli(nextMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(displayFormatter)

        statusText.text = if (exactAllowed) {
            getString(R.string.ready_with_next, nextText)
        } else {
            getString(R.string.need_exact_alarm_permission_with_next, nextText)
        }
    }
}

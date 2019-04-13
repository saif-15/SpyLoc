package com.example.dell.saif.spyloc


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val manager=NotificationManagerCompat.from(context!!.applicationContext)
        val notification=NotificationCompat.Builder(context.applicationContext,App.CHANNEL_ID2)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setSmallIcon(R.drawable.ic_location)
        .setContentTitle("SpyLoc")
        .setContentText("Location Reached")
        .setColorized(true)
        .setAutoCancel(true)
        .build()
        manager.notify(3,notification)
        val notify=MediaPlayer.create(context.applicationContext,Settings.System.DEFAULT_ALARM_ALERT_URI)
        notify.start()
    }
}
package com.example.dell.saif.spyloc.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.multidex.MultiDexApplication
import com.example.dell.saif.spyloc.Constants.BLUE_LIGHT
import com.example.dell.saif.spyloc.Constants.CHANNEL_ID1
import com.example.dell.saif.spyloc.Constants.CHANNEL_ID2
import com.example.dell.saif.spyloc.Constants.CYAN_LIGHT
import com.example.dell.saif.spyloc.Constants.PURPLE_LIGHT
import com.example.dell.saif.spyloc.Constants.RED_LIGHT
import com.example.dell.saif.spyloc.Constants.WHITE_LIGHT
import com.example.dell.saif.spyloc.Constants.YELLOW_LIGHT

class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    fun createNotificationChannel() {
        val preference = applicationContext.getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val servicechannel1= NotificationChannel(CHANNEL_ID1, "SpyLoc", NotificationManager.IMPORTANCE_DEFAULT)
            val servicechannel2 = NotificationChannel(CHANNEL_ID2, "SpyLoc", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NotificationManager::class.java)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()
            servicechannel1.setSound(Uri.parse(preference.getString("notification_uri","")),audioAttributes)
            servicechannel2.setSound(Uri.parse(preference.getString("alarm_uri","")),audioAttributes)

               val light = when (preference.getString("lights","")) {
                "Red" -> RED_LIGHT
                "White" -> WHITE_LIGHT
                "Cyan" -> CYAN_LIGHT
                "Blue" -> BLUE_LIGHT
                "Purple" -> PURPLE_LIGHT
                "Yellow" -> YELLOW_LIGHT
                else -> PURPLE_LIGHT
            }
            servicechannel1.lightColor= light
            servicechannel2.lightColor=light
            manager.createNotificationChannel(servicechannel1)
            manager.createNotificationChannel(servicechannel2)
            }
    }
}
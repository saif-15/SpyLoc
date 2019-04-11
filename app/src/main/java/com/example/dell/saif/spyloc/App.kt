package com.example.dell.saif.spyloc

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.os.Build
import androidx.multidex.MultiDexApplication

class App: MultiDexApplication() {

    companion object {
        val CHANNEL_ID1="com.example.dell.saif.spyloc.channel.id1"
        val CHANNEL_ID2="com.example.dell.saif.spyloc.channel.id2"

    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

   fun createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            val servicechannel1=NotificationChannel(CHANNEL_ID1,"SpyLoc",NotificationManager.IMPORTANCE_DEFAULT)
            val servicechannel2=NotificationChannel(CHANNEL_ID2,"SpyLoc",NotificationManager.IMPORTANCE_HIGH)
            val manager=getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(servicechannel1)
            manager.createNotificationChannel(servicechannel2)
        }
    }
}
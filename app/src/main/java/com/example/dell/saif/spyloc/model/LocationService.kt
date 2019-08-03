package com.example.dell.saif.spyloc.model

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.dell.saif.spyloc.Constants.CHANNEL_ID1
import com.example.dell.saif.spyloc.Constants.CHANNEL_ID2
import com.example.dell.saif.spyloc.Constants.SHARED_PREF
import com.example.dell.saif.spyloc.R
import com.example.dell.saif.spyloc.view.MainActivity
import com.example.dell.saif.spyloc.viewModel.NoteRepository
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

class LocationService : Service() {

    private val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private lateinit var wifiManager: WifiManager
    private var blueAdapter: BluetoothAdapter? = null
    private lateinit var audioManager: AudioManager
    private lateinit var locationManager: LocationManager
    private lateinit var alarmManager: AlarmManager
    val TAG = "Map Activity"
    private lateinit var repository: NoteRepository
    private lateinit var geocoder: Geocoder
    private var wifi: Int? = null
    private var bluetooth: Int? = null
    private var ringtone: Int? = null
    private var alarm: Int? = null
    private var notify: Int? = null
    private var nextAddress: String? = null
    private var previousAddress: String? = null


    override fun onCreate() {
        super.onCreate()
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        blueAdapter = BluetoothAdapter.getDefaultAdapter()
        audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        repository = NoteRepository(application)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pending = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID1).apply {
            setContentTitle("SpyLoc")
            setContentText("SpyLoc is Following You")
            setSmallIcon(R.drawable.ic_location)
            setContentIntent(pending)
            priority = NotificationCompat.PRIORITY_HIGH
        }.build()
        startForeground(1, notification)
        getCurrentLocation()

        return START_NOT_STICKY
    }

    fun checkLocation(address: Address) {

        val preference = applicationContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        preference.edit {
            putString("feature_name", address.featureName)
            putString("sub_locality", address.subLocality)
            putString("sub_admin_area", address.subAdminArea)
            putString("details", address.adminArea.plus(", ${address.countryName}").plus(",${address.countryCode}"))
            putString("latitude", address.latitude.toString())
            putString("longitude", address.longitude.toString())
            putBoolean("isNotEmpty", true)
            apply()
        }
        val lattitute = BigDecimal(address.latitude).setScale(4, RoundingMode.HALF_EVEN)
        val longitude = BigDecimal(address.longitude).setScale(4, RoundingMode.HALF_EVEN)
        val allLoc = repository.getLocations()
        if (allLoc.isNotEmpty()) {
            for (i in 0 until repository.getNumbers()) {

                wifi = allLoc[i].wifi
                bluetooth = allLoc[i].bluetooth
                ringtone = allLoc[i].ringtone
                notify = allLoc[i].notification
                alarm = allLoc[i].alarm

                Log.d(TAG, "checking location")
                if ((allLoc[i].feature_name == address.featureName
                            && allLoc[i].sub_locality == address.subLocality)
                    || (BigDecimal(allLoc[i].lat!!).setScale(4, RoundingMode.HALF_EVEN) == lattitute
                            || BigDecimal(allLoc[i].lng!!).setScale(4, RoundingMode.HALF_EVEN) == longitude
                            )
                ) {
                    Log.d(TAG, "inside if")
                    checkNotification(allLoc[i].feature_name.toString(), allLoc[i].sub_locality.toString())
                    checkRinger()
                    checkBluetooth()
                    checkWifi()
                    checkAlarm(address)


                } else {
                    if (allLoc[i].feature_name == "UBIT-DCS"
                        && lattitute == BigDecimal(24.9455)
                        && longitude == BigDecimal(67.1150)
                        && address.subLocality == "University Of Karachi"
                    ) {
                        checkNotification(allLoc[i].feature_name.toString(), allLoc[i].sub_locality.toString())
                        checkRinger()
                        checkBluetooth()
                        checkWifi()
                        checkAlarm(address)
                    }

                }
            }
        }
    }

    private fun getCurrentLocation() {
        Log.d("map Activity", "getting current location")
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(applicationContext, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(applicationContext, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    0f,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location?) {
                            Log.d("map Activity", "on location changes")
                            val latitute: Double = location!!.latitude
                            val longitute: Double = location.longitude
                            geocoder = Geocoder(applicationContext)
                            try {
                                val address = geocoder.getFromLocation(latitute, longitute, 1)
                                checkLocation(address[0])
                                Log.d("map Activity", "marker moving camera")
                                val lat = BigDecimal(address[0].latitude).setScale(4, RoundingMode.HALF_EVEN)
                                val lng = BigDecimal(address[0].longitude).setScale(4, RoundingMode.HALF_EVEN)
                                Log.d(TAG, lat.toString().plus("  $lng"))
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                            Log.d("status", status.toString())

                        }

                        override fun onProviderEnabled(provider: String?) {

                        }

                        override fun onProviderDisabled(provider: String?) {

                        }

                    })


            } else
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        10000,
                        0f,
                        object : LocationListener {
                            override fun onLocationChanged(location: Location?) {
                                Log.d("map Activity", "on location changes")
                                val latitute: Double = location!!.latitude
                                val longitute: Double = location.longitude
                                Log.d(
                                    "map Activity",
                                    latitute.toString().plus("long".plus(longitute.toString()) + "Network")
                                )
                                geocoder = Geocoder(applicationContext)
                                try {
                                    val address = geocoder.getFromLocation(latitute, longitute, 1)
                                    checkLocation(address[0])
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }

                            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                            }

                            override fun onProviderEnabled(provider: String?) {

                            }

                            override fun onProviderDisabled(provider: String?) {

                            }
                        })
                }
        }
    }

    private fun checkRinger() {
        val preference=applicationContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        if(preference.getBoolean("dont_disturb",false) && Build.VERSION.SDK_INT>=21)
        {
            //Ringtone Checking
            Log.d(TAG, "before checking ringtone")
            if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                Log.d(TAG, "ringtone is normal")
                if (ringtone == 1)
                    return
                else audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            } else {
                Log.d(TAG, "ringtone is silent")

                if (ringtone == 1)
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                else return
            }
            Log.d(TAG, ringtone.toString().plus(" after checking ringtone " + audioManager.ringerMode.toString()))


        }
        if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.KITKAT)
        {
            Log.d(TAG, "before checking ringtone")
            if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                Log.d(TAG, "ringtone is normal")
                if (ringtone == 1)
                    return
                else audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            } else {
                Log.d(TAG, "ringtone is silent")

                if (ringtone == 1)
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                else return
            }
            Log.d(TAG, ringtone.toString().plus(" after checking ringtone " + audioManager.ringerMode.toString()))

        }
    }

    private fun checkBluetooth() {
        if (blueAdapter != null) {
            Log.d(TAG, "Before Checking Bluettoth".plus(bluetooth).plus(" " + blueAdapter!!.isEnabled))
            if (blueAdapter!!.isEnabled) {
                Log.d(TAG, "bluetooth is enable")
                if (bluetooth == 1)
                    return
                else {
                    blueAdapter!!.disable()
                }
            } else {
                Log.d(TAG, "bluetooth is disable")
                if (bluetooth == 0) return
                else {
                    blueAdapter!!.enable();Log.d(TAG, "bluetooth enable")
                }
            }
            Log.d(TAG, bluetooth.toString().plus(" After checking blue" + blueAdapter?.enable().toString()))
        }
    }

    private fun checkWifi() {
        //Wifi Checking
        Log.d(TAG, "Before checking wifi")
        if (wifiManager.isWifiEnabled) {
            Log.d(TAG, "wifi is enable")

            if (wifi == 1)
                return
            else {
                wifiManager.isWifiEnabled = false;Log.d(TAG, "wifi disable")
            }
        } else {
            Log.d(TAG, "wifi is disable")

            if (wifi == 1) {
                wifiManager.isWifiEnabled = true;Log.d(TAG, "Wifi enable")
            } else return
        }
        Log.d(TAG, wifi.toString().plus("  After checking  wifi  " + wifiManager.isWifiEnabled.toString()))

    }

    private fun checkNotification(feature: String, locality: String) {
        if (notify == 1) {
            val notificationmanager = NotificationManagerCompat.from(applicationContext)
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID2)
                .apply {
                    setContentTitle("SpyLoc")
                    setContentText(feature.plus(" $locality"))
                    setSmallIcon(R.drawable.ic_location)
                    setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    priority = NotificationCompat.PRIORITY_HIGH
                    color = Color.rgb(35, 193, 235)
                }.build()

            val mediaPlayer = MediaPlayer.create(applicationContext, Settings.System.DEFAULT_NOTIFICATION_URI)
            mediaPlayer.start()

            notificationmanager.notify(2, notification)
        }
    }

    private fun checkAlarm(address: Address) {

        if (alarm == 1) {
            nextAddress = address.featureName
            if (previousAddress != nextAddress) {

                val notificationmanager = NotificationManagerCompat.from(applicationContext)
                val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID2).apply {
                    setAutoCancel(true)
                    setCategory(NotificationCompat.CATEGORY_ALARM)
                    setContentTitle("SpyLoc")
                    setContentText("Location Reached")
                    setSmallIcon(R.drawable.ic_location)
                    priority = NotificationCompat.PRIORITY_HIGH
                    color = Color.rgb(35, 193, 235)
                }.build()
                val mediaPlayer = MediaPlayer.create(applicationContext, Settings.System.DEFAULT_ALARM_ALERT_URI)
                mediaPlayer.start()
                notificationmanager.notify(2, notification)
                Thread(Runnable {
                    Thread.sleep(10000)
                    mediaPlayer.stop()
                }).start()
            }
            previousAddress = nextAddress
        }
    }

}
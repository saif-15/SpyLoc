package com.spyloc.model

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
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.spyloc.Constants
import com.spyloc.Constants.CHANNEL_ID1
import com.spyloc.Constants.CHANNEL_ID2
import com.spyloc.Constants.CHANNEL_ID3
import com.spyloc.Constants.SHARED_PREF
import com.spyloc.R
import com.spyloc.vibration
import com.spyloc.view.DashboardActivity
import com.spyloc.view.MainActivity
import com.spyloc.viewModel.NoteRepository
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
    private val TAG = "Map Activity"
    private lateinit var repository: NoteRepository
    private lateinit var geocoder: Geocoder
    private var wifi: Int? = null
    private var bluetooth: Int? = null
    private var ringtone: Int? = null
    private var alarm: Int? = null
    private var notify: Int? = null
    private var nextAddress: String? = null
    private lateinit var locationListener: LocationListener
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

        val intent = Intent(applicationContext, DashboardActivity::class.java)
        val pending = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID1).apply {
            setContentTitle("SpyLoc")
            setContentText("SpyLoc is Following You")
            setSmallIcon(R.drawable.ic_location_on)
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
            putInt("today", preference.getInt("today", 0) + 1)

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

                locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        val latitute: Double = location!!.latitude
                        val longitute: Double = location.longitude
                        geocoder = Geocoder(applicationContext)
                        try {
                            val address = geocoder.getFromLocation(latitute, longitute, 1)
                            checkLocation(address[0])
                            Log.d("map Activity", "marker moving camera")
                            val lat = BigDecimal(address[0].latitude).setScale(4, RoundingMode.HALF_EVEN)
                            val lng = BigDecimal(address[0].longitude).setScale(4, RoundingMode.HALF_EVEN)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                        Log.d("status", status.toString())
                        getCurrentLocation()

                    }

                    override fun onProviderEnabled(provider: String?) {
                        getCurrentLocation()
                    }

                    override fun onProviderDisabled(provider: String?) {
                        getCurrentLocation()
                    }

                }
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5*60*1000,
                    0f, locationListener
                )


            } else
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        5*60*1000,
                        0f,
                       locationListener)
                }
        }
    }

    private fun checkRinger() {
        val preference = applicationContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        if (preference.getBoolean("dont_disturb", false) && Build.VERSION.SDK_INT >= 21) {
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
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
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
                    blueAdapter!!.enable()
                }
            }
        }
    }

    private fun checkWifi() {
        //Wifi Checking
        if (wifiManager.isWifiEnabled) {
            if (wifi == 1)
                return
            else {
                wifiManager.isWifiEnabled = false
            }
        } else {

            if (wifi == 1) {
                wifiManager.isWifiEnabled = true
            } else return
        }
    }

    private fun checkNotification(feature: String, locality: String) {
        val preferenceDefault = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val preference = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        val notificationCompat = NotificationManagerCompat.from(applicationContext)
        when (notify) {
            1 -> {
                val intent = Intent(applicationContext, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(applicationContext, 1, intent, 0)

                val light = when (preferenceDefault.getString("lights", "")) {
                    "Red" -> Constants.RED_LIGHT
                    "White" -> Constants.WHITE_LIGHT
                    "Cyan" -> Constants.CYAN_LIGHT
                    "Blue" -> Constants.BLUE_LIGHT
                    "Purple" -> Constants.PURPLE_LIGHT
                    "Yellow" -> Constants.YELLOW_LIGHT
                    else -> Constants.PURPLE_LIGHT
                }
                val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID2)
                    .apply {
                        setContentTitle("SpyLoc")
                        setSmallIcon(R.drawable.ic_location_on)
                        if (preferenceDefault.getBoolean("led_notification", true)) {
                            setLights(light, 500, 500)
                        }
                        setChannelId(CHANNEL_ID1)
                        setContentText(feature)
                        setContentIntent(pendingIntent)
                        setSubText(locality)
                        setSound(Uri.parse(preference.getString("notification_uri", "")))
                        setCategory(NotificationCompat.CATEGORY_EVENT)
                        priority = NotificationCompat.PRIORITY_HIGH
                        color = Color.rgb(35, 193, 235)
                    }.build()
                if (preferenceDefault.getBoolean("vibrate", true))
                    vibration(applicationContext, preferenceDefault.getString("vibration_pattern", "Short")!!)
                notificationCompat.notify(2, notification)
            }
            else -> return
        }
    }

    private fun checkAlarm(address: Address) {

        val preferenceDefault = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val preference = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        when (alarm) {
            1 -> {
                val intent = Intent(applicationContext, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(applicationContext, 1, intent, 0)

                nextAddress = address.featureName
                if (previousAddress != nextAddress) {
                    val light = when (preferenceDefault.getString("lights", "")) {
                        "Red" -> Constants.RED_LIGHT
                        "White" -> Constants.WHITE_LIGHT
                        "Cyan" -> Constants.CYAN_LIGHT
                        "Blue" -> Constants.BLUE_LIGHT
                        "Purple" -> Constants.PURPLE_LIGHT
                        "Yellow" -> Constants.YELLOW_LIGHT
                        else -> Constants.PURPLE_LIGHT
                    }

                    val notificationManager = NotificationManagerCompat.from(applicationContext)
                    val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID3).apply {
                        setAutoCancel(true)
                        if (preferenceDefault.getBoolean("led_notification", true)) {
                            setLights(light, 500, 500)
                        }
                        setCategory(NotificationCompat.CATEGORY_ALARM)
                        setContentTitle("SpyLoc")
                        setContentIntent(pendingIntent)
                        setSound(Uri.parse(preference.getString("alarm_uri", "")))
                        setContentText("Location Reached")
                        setSmallIcon(R.drawable.ic_location_on)
                        priority = NotificationCompat.PRIORITY_HIGH
                        if (preferenceDefault.getBoolean("vibrate", true)) {
                            vibration(applicationContext, preferenceDefault.getString("vibration_pattern", "")!!)
                        }
                        color = Color.rgb(35, 193, 235)
                    }.build()

                    notificationManager.notify(3, notification)
                }
                previousAddress = nextAddress
            }
            else -> return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
    }

}
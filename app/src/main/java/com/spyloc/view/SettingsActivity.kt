package com.spyloc.view

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.*
import com.spyloc.Constants.CHANNEL_ID2
import com.spyloc.Constants.CHANNEL_ID3
import com.spyloc.Constants.SHARED_PREF
import com.spyloc.R
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.File

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val preference = applicationContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment.getInstance(preference))
            .commit()
        setSupportActionBar(settings_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var pref: Preference
        private lateinit var pref1: Preference
        private lateinit var pref2: Preference
        private lateinit var switch: SwitchPreferenceCompat
        private lateinit var list: ListPreference
        private lateinit var light_checkbox: CheckBoxPreference
        private lateinit var vibration_pattern: ListPreference


        companion object {
            lateinit var preference: SharedPreferences

            fun getInstance(pref: SharedPreferences): SettingsFragment {
                preference = pref
                val frag = SettingsFragment()
                return frag
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            list = findPreference<ListPreference>("lights")!!
            vibration_pattern = findPreference<ListPreference>("vibration_pattern")!!
            light_checkbox = findPreference<CheckBoxPreference>("led_notification")!!
            switch = findPreference<SwitchPreferenceCompat>("vibrate")!!
            pref2 = findPreference<Preference>("memory")!!



            pref = findPreference<Preference>("alarm_sound")!!
            if (Build.VERSION.SDK_INT < 26) {
                pref.summary = RingtoneManager.getRingtone(
                    activity!!.applicationContext,
                    Uri.parse(preference.getString("alarm_uri", ""))
                ).getTitle(activity?.applicationContext)
            } else {
                pref.summary = "Select Alarm Sound"
            }


            pref.setOnPreferenceClickListener {
                if (Build.VERSION.SDK_INT < 26) {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                    intent.putExtra(
                        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        Uri.parse(preference.getString("alarm_uri", ""))
                    )
                    startActivityForResult(intent, 1)
                } else {
                    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, "com.spyloc")
                        putExtra(
                            Settings.EXTRA_CHANNEL_ID,
                            NotificationChannel(CHANNEL_ID3, "SpyLoc", NotificationManager.IMPORTANCE_HIGH).id
                        )
                    }
                    startActivity(intent)

                }
                true

            }
            pref1 = findPreference<Preference>("notification_sound")!!


            if (Build.VERSION.SDK_INT < 26) {
                pref1.summary = RingtoneManager.getRingtone(
                    activity?.applicationContext,
                    Uri.parse(preference.getString("notification_uri", ""))
                ).getTitle(activity?.applicationContext)
            } else {
                pref1.summary = "Select Notification sound"
            }

            pref1.setOnPreferenceClickListener {
                if (Build.VERSION.SDK_INT < 26) {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                    intent.putExtra(
                        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        Uri.parse(preference.getString("notification_uri", ""))
                    )
                    startActivityForResult(intent, 2)
                } else {
                    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, "com.spyloc")
                        putExtra(
                            Settings.EXTRA_CHANNEL_ID,
                            NotificationChannel(CHANNEL_ID2, "Spyloc", NotificationManager.IMPORTANCE_HIGH).id
                        )
                    }
                    startActivity(intent)
                }
                true
            }

            pref2.summary =
                (getFreeMemory()).toString().plus(" ").plus("MB out of ").plus(getMemory()).plus(" ").plus("MB")
                    .plus(" (" + ((getFreeMemory() * 100 / getMemory() * 100) / 100) + "%)")

            preference.edit { putBoolean("vibration", switch.isChecked) }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    1 -> {
                        val uri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                        saveAlarm(uri)

                        val Ringtone = RingtoneManager.getRingtone(activity?.applicationContext, uri)
                        pref.summary = Ringtone.getTitle(activity?.applicationContext)

                    }
                    2 -> {
                        val uri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                        val Ringtone = RingtoneManager.getRingtone(activity?.applicationContext, uri)
                        pref1.summary = Ringtone.getTitle(activity?.applicationContext)
                        saveNotification(uri)
                    }
                }
            }
        }

        private fun getFreeMemory() =
            (File(activity?.applicationContext!!.filesDir.absoluteFile.toString()).freeSpace) / 1048576

        private fun getMemory(): Long {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            return ((totalBlocks * blockSize) / 1048576)
        }

        private fun saveAlarm(alarmUri: Uri?) {
            alarmUri.let {
                preference.edit { putString("alarm_uri", alarmUri.toString()) }

            }
        }

        private fun saveNotification(notificationUri: Uri?) {
            notificationUri.let {
                preference.edit { putString("notification_uri", notificationUri.toString()) }

            }
        }
    }

}

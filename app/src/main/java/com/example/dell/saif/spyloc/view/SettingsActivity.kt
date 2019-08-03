package com.example.dell.saif.spyloc.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.preference.*
import com.example.dell.saif.spyloc.Constants.BLUE_LIGHT
import com.example.dell.saif.spyloc.Constants.CHANNEL_ID1
import com.example.dell.saif.spyloc.Constants.CYAN_LIGHT
import com.example.dell.saif.spyloc.Constants.PURPLE_LIGHT
import com.example.dell.saif.spyloc.Constants.RED_LIGHT
import com.example.dell.saif.spyloc.Constants.WHITE_LIGHT
import com.example.dell.saif.spyloc.Constants.YELLOW_LIGHT
import com.example.dell.saif.spyloc.R
import com.example.dell.saif.spyloc.Vibration
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.File

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val preference=applicationContext.getSharedPreferences("shared_pref", Context.MODE_PRIVATE)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment.getInstance(preference))
            .commit()
        setSupportActionBar(settings_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


    }

    class SettingsFragment: PreferenceFragmentCompat() {

       private lateinit var pref: Preference
       private lateinit var pref1: Preference
      private  lateinit var pref2: Preference
       private lateinit var switch: SwitchPreferenceCompat
      private lateinit var list: ListPreference
       private lateinit var light_checkbox: CheckBoxPreference
        private lateinit var vibration_pattern: ListPreference



companion object{
    lateinit var preference:SharedPreferences

    fun getInstance(pref:SharedPreferences):SettingsFragment{
      preference=pref
        val frag=SettingsFragment()
        return  frag
    }
}

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            list = findPreference<ListPreference>("lights")!!
            vibration_pattern = findPreference<ListPreference>("vibration_pattern")!!
            light_checkbox = findPreference<CheckBoxPreference>("led_notification")!!
            switch = findPreference<SwitchPreferenceCompat>("vibrate")!!
            pref2 = findPreference("memory")!!
            pref2.setOnPreferenceClickListener {

                val notif = NotificationManagerCompat.from(context!!.applicationContext)
                preference.edit { putString("light_color", list.value) }
                preference.edit { putBoolean("led_notification", light_checkbox.isChecked) }
                val light = when (list.value) {
                    "Red" -> RED_LIGHT
                    "White" -> WHITE_LIGHT
                    "Cyan" -> CYAN_LIGHT
                    "Blue" -> BLUE_LIGHT
                    "Purple" -> PURPLE_LIGHT
                    "Yellow" -> YELLOW_LIGHT
                    else -> PURPLE_LIGHT
                }
                val notification = NotificationCompat.Builder(activity!!.applicationContext, CHANNEL_ID1)
                    .apply {
                        setContentTitle("SpyLoc")
                        setSmallIcon(R.drawable.ic_location)
                        if (light_checkbox.isChecked){
                            setLights(light, 700, 700)}
                        setChannelId(CHANNEL_ID1)
                        setSound(Uri.parse(preference.getString("notification_uri","")))
                        setCategory(NotificationCompat.CATEGORY_EVENT)
                        priority = NotificationCompat.PRIORITY_HIGH
                        /*setSound(
                            Uri.parse(preference.getString("notification_uri","")))*/
                        color = Color.rgb(35, 193, 235)
                    }.build()
                if (switch.isChecked)
                    activity!!.Vibration(activity!!.applicationContext, vibration_pattern.value)
                notif.notify(1, notification)

                true
            }

            pref = findPreference<Preference>("alarm_sound")!!
            pref.summary = RingtoneManager.getRingtone(
                activity!!.applicationContext,
                Uri.parse(preference.getString("alarm_uri", ""))
            ).getTitle(activity?.applicationContext)

            pref.setOnPreferenceClickListener {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                intent.putExtra(
                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                    Uri.parse(preference.getString("alarm_uri", ""))
                )

                startActivityForResult(intent, 1)
                true
            }
            pref1 = findPreference<Preference>("notification_sound")!!

            pref1.summary = RingtoneManager.getRingtone(
                activity?.applicationContext,
                Uri.parse(preference.getString("notification_uri", ""))
            ).getTitle(activity?.applicationContext)

            pref1.setOnPreferenceClickListener {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                intent.putExtra(
                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                    Uri.parse(preference.getString("notification_uri", ""))
                )
                startActivityForResult(intent, 2)
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

      private  fun saveAlarm(alarmUri: Uri?) {
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

package com.spyloc.view

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.spyloc.Constants
import com.spyloc.Constants.SHARED_PREF
import com.spyloc.Constants.SWITCH
import com.spyloc.R
import com.spyloc.model.LocationService
import com.spyloc.viewModel.NoteViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.math.BigDecimal
import java.math.RoundingMode


class DashboardActivity : AppCompatActivity(), View.OnClickListener {

    var isPermissionGranted: Boolean = false
    lateinit var ad: InterstitialAd
    var permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET
    )
    val TAG = "DashboardActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setSupportActionBar(toolbar_dashboard)

        val preference = applicationContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        preference.edit {
            putLong(
                "time",
                preference.getLong("time", System.currentTimeMillis())
            ).apply()
        }
        val image = Uri.parse(preference.getString("imageUri", R.drawable.user.toString()))
        val transform = RoundedTransformationBuilder().apply {
            cornerRadiusDp(100f)
            oval(false)
        }.build()
        Picasso.get().load(image).fit().transform(transform).into(profile_image)


        profile_email.text = preference.getString("user_email", "username")
        profile_name.text = preference.getString("user_name", "name")

        cardview_map.setOnClickListener(this)
        cardview_1.setOnClickListener(this)
        cardview_6.setOnClickListener(this)
        profile_image.setOnClickListener(this)
        cardview_5.setOnClickListener(this)
        cardview_7.setOnClickListener(this)

        val viewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        viewModel.getLiveDataNumber().observe(this, Observer {

            location_added_number.text = it.toString()
        })
        delete_all.setOnClickListener {
            viewModel.deleteAllNotes()
        }
        writeValues(preference.getBoolean("isNotEmpty", false))

        checkPermissions()
        checkTime()
        notificationPermission()
        loadAd()
    }

    private fun writeValues(bool: Boolean) {
        if (bool) {
            cardview_4.setCardBackgroundColor(Color.WHITE)
            last_location.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            error_text.visibility = View.GONE
            error_text_details.visibility = View.GONE
            error_icon.visibility = View.GONE
            val preference =
                applicationContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

            val featureName = preference.getString("feature_name", "")
            val subLocality = preference.getString("sub_locality", "")
            val subAdminArea = preference.getString("sub_admin_area", "")
            val details = preference.getString("details", "")
            val longitude = preference.getString("longitude", "")
            val latitude = preference.getString("latitude", "")

            featureName.let {
                dashboard_feature_name.visibility = View.VISIBLE
                dashboard_feature_name.text = it
            }
            subLocality.let {
                dashboard_sub_locality.visibility = View.VISIBLE
                dashboard_sub_locality.text = it
            }
            subAdminArea.let {
                dashboard_sub_admin_area.visibility = View.VISIBLE
                dashboard_sub_admin_area.text = it
            }
            details.let {
                dashboard_details.visibility = View.VISIBLE
                dashboard_details.text = it
            }
            latitude.let {
                dashboard_lattitute.visibility = View.VISIBLE
                dashboard_lattitute.text = "Latitude:  $it"
            }
            longitude.let {
                dashboard_longitude.visibility = View.VISIBLE
                dashboard_longitude.text = "Longitude:  $it"
            }
        } else {
            cardview_4.setCardBackgroundColor(Color.parseColor("#B00020"))
            last_location.setTextColor(Color.WHITE)
            error_text.visibility = View.VISIBLE
            error_icon.visibility = View.VISIBLE
            error_text_details.visibility = View.VISIBLE
            dashboard_sub_locality.visibility = View.GONE

            dashboard_longitude.visibility = View.GONE
            dashboard_feature_name.visibility = View.GONE
            dashboard_details.visibility = View.GONE
            dashboard_sub_admin_area.visibility = View.GONE
            dashboard_lattitute.visibility = View.GONE
        }


    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.cardview_map -> {
                ad.adListener= object :AdListener() {
                    override fun onAdClosed() {
                        startActivity(Intent(this@DashboardActivity, MapsActivity::class.java))
                    }
                }
                if (ad.isLoaded) ad.show()
                else startActivity(
                    Intent(
                        this@DashboardActivity,
                        MapsActivity::class.java
                    )
                )
            }
            R.id.cardview_1 -> openDialog()
            R.id.cardview_6 -> {
                ad.adListener= object :AdListener() {
                    override fun onAdClosed() {
                        startActivity(Intent(this@DashboardActivity, SettingsActivity::class.java))
                    }
                }
                if (ad.isLoaded) ad.show()
                else startActivity(
                    Intent(
                        this@DashboardActivity,
                        SettingsActivity::class.java
                    )
                )
            }
            R.id.profile_image -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                val mineTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
                intent.action = Intent.ACTION_GET_CONTENT
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mineTypes)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                startActivityForResult(intent, 1)

            }
            R.id.cardview_5 ->{
                ad.adListener= object :AdListener() {
                    override fun onAdClosed() {
                        startActivity(Intent(this@DashboardActivity, MainActivity::class.java))
                    }
                }
                if(ad.isLoaded) ad.show()
               else startActivity(Intent(this@DashboardActivity,MainActivity::class.java))
            }
            R.id.cardview_7 -> About().show(supportFragmentManager, "About")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.about -> About().show(supportFragmentManager, "About")
            R.id.settings_menu -> startActivity(
                Intent(
                    this@DashboardActivity,
                    SettingsActivity::class.java
                )
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun startService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(applicationContext, intent)
    }

    private fun stopService() {
        val intent = Intent(this, LocationService::class.java)
        stopService(intent)
    }

    private fun openDialog() {
        val dialog = ProfileFragment()
        val preference = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        val bundle = Bundle().apply {
            putString("user_name_transfer", profile_name.text.toString())
            putString("user_email_transfer", profile_email.text.toString())
            if (preference.getString("imageUri", null) != null)
                putString("user_image_transfer", preference.getString("imageUri", ""))

        }
        dialog.show(supportFragmentManager, "Edit Details")
        dialog.arguments = bundle
        dialog.getValuesOf { name: String, email: String ->
            profile_name.text = name
            profile_email.text = email
        }

    }

    override fun onStart() {
        super.onStart()
        val preference = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        dashboard_switch.isChecked = preference.getBoolean(SWITCH, false)
        dashboard_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                startService()
                preference.edit { putBoolean("switch", isChecked) }
            } else {
                stopService()
                preference.edit { putBoolean("switch", isChecked) }
            }
        }
        setValuesOnViews(preference)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK && data != null && data.data != null) {
                val imageUri = data.data

                /*this.grantUriPermission(this.packageName,imageUri,Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val flag=Intent.FLAG_GRANT_READ_URI_PERMISSION
                this.contentResolver.takePersistableUriPermission(imageUri!!,flag)*/

                val preference = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
                preference.edit { putString("imageUri", imageUri?.toString()).commit() }
                val transform = RoundedTransformationBuilder().apply {
                    cornerRadiusDp(100f)
                    oval(false)
                }.build()
                Picasso.get().load(imageUri).fit().transform(transform).into(profile_image)

            }
        }
    }

    private fun checkTime() {

        val preference = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        if (System.currentTimeMillis() - preference.getLong("time", 0) >= 24 * 60 * 60 * 1000) {
            preference.edit { putInt("yesterday", preference.getInt("today", 0)) }
            preference.edit { putInt("today", 0).apply() }
            preference.edit { putLong("time", System.currentTimeMillis()) }
        }

    }

    private fun setValuesOnViews(preference: SharedPreferences) {
        number_of_requests_today.text = preference.getInt("today", 0).toString()
        number_of_requests_yesterday.text = preference.getInt("yesterday", 0).toString()

        if (preference.getInt("today", 0) >= preference.getInt("yesterday", 0)) {
            trend.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_trending_up))

        } else {
            trend.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_trending_down))
        }

        if (preference.getInt("today", 0) > preference.getInt("yesterday", 0)) {
            if (preference.getInt("today", 1) != 0 && preference.getInt("yesterday", 1) != 0) {


                val number = BigDecimal(
                    (((preference.getInt("today", 1).toDouble() / preference.getInt(
                        "yesterday",
                        1
                    ).toDouble()) * 100))
                ).setScale(2, RoundingMode.HALF_EVEN)
                percentage.text = number.toString().plus("% increased")
            } else if (preference.getInt("today", 0) == 0 && preference.getInt(
                    "yesterday",
                    0
                ) != 0
            ) {
                percentage.text = (preference.getInt("yesterday", 0) * 100).toString()
                    .plus("% increased")
            } else if (preference.getInt("today", 0) != 0 && preference.getInt(
                    "yesterday",
                    0
                ) == 0
            ) {
                percentage.text = preference.getInt("today", 0).toString().plus("% increased")
            }
        } else {
            if (preference.getInt("today", 1) != 0 && preference.getInt("yesterday", 1) != 0) {

                val number = BigDecimal(
                    (100 - ((preference.getInt("today", 1).toDouble() / preference.getInt(
                        "yesterday",
                        1
                    ).toDouble()) * 100))
                ).setScale(2, RoundingMode.HALF_EVEN)
                percentage.text = number.toString().plus("% decreased")
            } else if (preference.getInt("today", 0) != 0 && preference.getInt(
                    "yesterday",
                    0
                ) == 0
            ) {
                percentage.text = preference.getInt("today", 0).toString().plus(" % increased")
            } else if (preference.getInt("today", 0) == 0 && preference.getInt(
                    "yesterday",
                    0
                ) != 0
            ) {
                val number = BigDecimal(
                    (100 - ((1 / preference.getInt(
                        "yesterday",
                        0
                    ).toDouble()) * 100))
                ).setScale(
                    2,
                    RoundingMode.HALF_EVEN
                )
                percentage.text = number.toString().plus("% decreased")
            } else if (preference.getInt("today", 0) == 0 && preference.getInt(
                    "yesterday",
                    0
                ) == 0
            ) {
                percentage.text = ""
            }
        }
        if (preference.getInt("today", 0) == preference.getInt("yesterday", 0)) {
            if (preference.getInt("today", 0) == 0 && preference.getInt("yesterday", 0) == 0) {
                percentage.text = ""
            } else
                percentage.text = "0% increased"
        }
    }

    private fun checkPermissions() {

        Log.d(TAG, "checking permissions")

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                isPermissionGranted = true

            } else {
                ActivityCompat.requestPermissions(this, permissions, 1234)
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, 1234)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        isPermissionGranted = false

        when (requestCode) {
            1234 -> if (grantResults.isNotEmpty()) {
                for (i in 0 until grantResults.size) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        isPermissionGranted = false
                        return
                    }
                }
                isPermissionGranted = true
            }
        }
    }

    private fun notificationPermission() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {

            val intent =
                Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivityForResult(intent, Constants.DONT_DISTURB)

        }
    }
    private fun loadAd(){
        MobileAds.initialize(applicationContext,"ca-app-pub-2304912645023659~6563009661")
        ad= InterstitialAd(applicationContext)
        ad.adUnitId="ca-app-pub-3940256099942544/1033173712"
        ad.loadAd(AdRequest.Builder().build())

    }

}

package com.spyloc.view

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.spyloc.Constants.SHARED_PREF
import com.spyloc.Constants.SWITCH
import com.spyloc.makeToasty
import com.spyloc.model.LocationService
import com.spyloc.viewModel.NoteViewModel
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.spyloc.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_main.*


class DashboardActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setSupportActionBar(toolbar_dashboard)

        val preference = applicationContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        val transform = RoundedTransformationBuilder().apply {
            cornerRadiusDp(100f)
            oval(false)
        }.build()
        if (preference.getBoolean("isImage", false)) {
            val imageUri = Uri.parse(preference.getString("imageUri", R.drawable.user.toString()))
            Picasso.get().load(imageUri).transform(transform).fit().into(profile_image)
            Log.d("IMAGEURI", imageUri.toString())
        } else {
            Picasso.get().load(R.drawable.user).fit().into(profile_image)
        }

        profile_email.text = preference.getString("user_email", "example.gmail.com")
        profile_name.text = preference.getString("user_name", "name not available")

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

        MobileAds.initialize(applicationContext,"ca-app-pub-3940256099942544/6300978111")
        val request= AdRequest.Builder().build()
        adView.loadAd(request)

    }

    fun writeValues(bool: Boolean) {
        if (bool) {
            cardview_4.setCardBackgroundColor(Color.WHITE)
            last_location.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            error_text.visibility = View.GONE
            error_text_details.visibility = View.GONE
            error_icon.visibility=View.GONE
            val preference = applicationContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

            val featureName = preference.getString("feature_name", "")
            val subLocality = preference.getString("sub_locality", "")
            val subAdminArea = preference.getString("sub_admin_area", "")
            val details = preference.getString("details", "")
            val longitude = preference.getString("longitude", "")
            val latitute = preference.getString("latitude", "")

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
            latitute.let {
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
            error_icon.visibility=View.VISIBLE
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
            R.id.cardview_map -> startActivity(Intent(this@DashboardActivity, MapsActivity::class.java))
            R.id.cardview_1 -> openDialog()
            R.id.cardview_6 -> startActivity(Intent(this@DashboardActivity, SettingsActivity::class.java))
            R.id.profile_image -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, 1)

            }
            R.id.cardview_5 -> startActivity(Intent(this@DashboardActivity, MainActivity::class.java))
            R.id.cardview_7->About().show(supportFragmentManager,"About")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.about -> About().show(supportFragmentManager,"About")
            R.id.settings_menu -> startActivity(Intent(this@DashboardActivity, SettingsActivity::class.java))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun startService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(applicationContext, intent)
    }

    fun stopService() {
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

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val preference = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                val imageUri = data?.data
                val transform = RoundedTransformationBuilder().apply {
                    cornerRadiusDp(100f)
                    oval(false)

                }.build()
                Picasso.get().load(imageUri).fit().transform(transform).into(profile_image)
                preference.edit { putString("imageUri", imageUri.toString()).apply() }
                Log.d("IMAGEURI", imageUri.toString())
                preference.edit { putBoolean("isImage", true).apply() }

            }
        }
    }
}

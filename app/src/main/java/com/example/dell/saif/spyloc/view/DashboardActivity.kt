package com.example.dell.saif.spyloc.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.dell.saif.spyloc.Constants.SHARED_PREF
import com.example.dell.saif.spyloc.Constants.SWITCH
import com.example.dell.saif.spyloc.R
import com.example.dell.saif.spyloc.makeToasty
import com.example.dell.saif.spyloc.model.LocationService
import com.example.dell.saif.spyloc.viewModel.NoteViewModel
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.io.File


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
        val image=preference.getString("imageUri","")
        Picasso.get().load(Uri.parse(image)).fit().transform(transform).into(profile_image)

        profile_email.text = preference.getString("user_email", "example.gmail.com")
        profile_name.text = preference.getString("user_name", "name not available")

        cardview_map.setOnClickListener(this)
        cardview_1.setOnClickListener(this)
        cardview_6.setOnClickListener(this)
        profile_image.setOnClickListener(this)
        cardview_5.setOnClickListener(this)

        val viewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        viewModel.getLiveDataNumber().observe(this, Observer {

            location_added_number.text = it.toString()
        })
        delete_all.setOnClickListener {
            viewModel.deleteAllNotes()
        }
        writeValues(preference.getBoolean("isNotEmpty", false))


    }

    fun writeValues(bool: Boolean) {
        if (bool) {
            cardview_4.setCardBackgroundColor(Color.WHITE)
            last_location.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            error_text.visibility = View.GONE
            error_text_details.visibility = View.GONE
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
                dashboard_lattitute.text = it
            }
            longitude.let {
                dashboard_longitude.visibility = View.VISIBLE
                dashboard_longitude.text = it
            }
        } else {
            cardview_4.setCardBackgroundColor(Color.parseColor("#B00020"))
            last_location.setTextColor(Color.WHITE)
            error_text.visibility = View.VISIBLE
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
            R.id.cardview_1 -> {
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
            R.id.cardview_6 -> startActivity(Intent(this@DashboardActivity, SettingsActivity::class.java))
            R.id.profile_image -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, 1)
            }

            R.id.cardview_5 -> startActivity(Intent(this@DashboardActivity, MainActivity::class.java))
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val preference = applicationContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                val imageUri = data?.data
                val transform = RoundedTransformationBuilder().apply {
                    cornerRadiusDp(100f)
                    oval(false)

                }.build()
                Picasso.get().load(imageUri).fit().transform(transform).into(profile_image)
                preference.edit { putString("imageUri", imageUri.toString()).apply() }

            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.about -> makeToasty("About")
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

    fun intializeChart() {
        val NoOfEmp = ArrayList<BarEntry>()

        NoOfEmp.add(BarEntry(945f, 0f))
        NoOfEmp.add(BarEntry(1040f, 1f))
        NoOfEmp.add(BarEntry(1440f, 2f))

        val year = ArrayList<String>()

        year.add("2008")
        year.add("2009")
        year.add("2010")

        val bardataset = BarDataSet(NoOfEmp, "No Of Employee")
        chart.animateY(5000)
        try {
            val data = BarData(bardataset)
            bardataset.setColors(Color.WHITE)
            chart.data = data
        } catch (ex: ClassCastException) {
            ex.printStackTrace()
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


}

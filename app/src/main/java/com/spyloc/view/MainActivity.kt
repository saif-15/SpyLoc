package com.spyloc.view

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginStart
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.polyak.iconswitch.IconSwitch
import com.spyloc.Constants.DONT_DISTURB
import com.spyloc.Constants.SHARED_PREF
import com.spyloc.Constants.SWITCH
import com.spyloc.R
import com.spyloc.makeSnackBar
import com.spyloc.makeToasty
import com.spyloc.model.LocNote
import com.spyloc.model.LocationService
import com.spyloc.model.MyAdapter
import com.spyloc.viewModel.NoteRepository
import com.spyloc.viewModel.NoteViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ConfigDialog.ConfigDialogListener {


    lateinit var viewModel: NoteViewModel
    var isPermissionGranted: Boolean = false
    lateinit var animation1: Animation
    lateinit var animation: Animation
    var TAG = "Main Activity"

    var switch = false
    lateinit var adView: AdView
    var permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val preference = applicationContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

        checkPermissions()
         MobileAds.initialize(applicationContext,"ca-app-pub-3940256099942544/6300978111")

            val request= AdRequest.Builder().build()
            adView1.loadAd(request)

        adView1.adListener=object : AdListener()
        {
            override fun onAdFailedToLoad(errorCode : Int) {
                Toast.makeText(applicationContext,"error code :$errorCode",Toast.LENGTH_SHORT).show()
            }

        }
        switch = preference.getBoolean(SWITCH, false)
        animation=AnimationUtils.loadAnimation(applicationContext,R.anim.fab_animation)
        animation1 = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.recyclerview_animation
        )
        fab.startAnimation(animation)
        recyclerview.layoutManager = LinearLayoutManager(applicationContext)
        recyclerview.setHasFixedSize(false)
        recyclerview.startAnimation(animation1)
        viewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        val myAdapter =
            MyAdapter(
                this@MainActivity,
                viewModel,
                colayout,
                supportFragmentManager
            )
        recyclerview.adapter = myAdapter
        if(NoteRepository(application).getNumbers()!=0){
            recyclerview.visibility=View.VISIBLE
        viewModel.getAllNotes().observe(this,
            Observer<List<LocNote>> { t -> myAdapter.submitList(t) }
        )}


        fab.setOnClickListener {
            if (checkServices())
                init()
        }

        ItemTouchHelper(object : SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val locNote = myAdapter.getNoteAt(viewHolder.adapterPosition)
                viewModel.delete(locNote)
                Snackbar.make(colayout, "Location deleted", Snackbar.LENGTH_LONG)
                    .setActionTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorAccent))
                    .setAction("UNDO") {
                        viewModel.insert(locNote)
                        Snackbar.make(colayout, "Location Added", Snackbar.LENGTH_SHORT).show()
                    }
                    .show()

            }

        }).attachToRecyclerView(recyclerview)

        if (preference.getBoolean("switch", false)) {

        }

        start_stop_service.setCheckedChangeListener { current ->

            when (current) {
                IconSwitch.Checked.LEFT -> {
                    stopService()
                    switch = false
                    Toast.makeText(applicationContext, "Spyloc stops Following You", Toast.LENGTH_SHORT).show()
                    val shared = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
                    val editor = shared.edit()
                    editor.putBoolean(SWITCH, switch)
                    editor.apply()
                }
                IconSwitch.Checked.RIGHT -> {
                    switch = true
                    startService()
                    Toast.makeText(applicationContext, "SpyLoc is Following You", Toast.LENGTH_SHORT).show()
                    val shared = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
                    val editor = shared.edit()
                    editor.putBoolean(SWITCH, switch)
                    editor.apply()
                }
            }
        }
        if (switch)
            start_stop_service.checked = IconSwitch.Checked.RIGHT
        else start_stop_service.checked = IconSwitch.Checked.LEFT

        notificationPermission()

        recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab.show()
                } else fab.hide()

                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    fun init() {
        val intent = Intent(applicationContext, MapsActivity::class.java)
        startActivity(intent)
    }


    fun checkServices(): Boolean {
        val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext)

        when {
            available == ConnectionResult.SUCCESS -> return true
            GoogleApiAvailability.getInstance().isUserResolvableError(9001) -> {
                val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, 9001)
                dialog.show()
            }
            else -> makeToasty("Cant request map")
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val repo = NoteRepository(application)
        when (item?.itemId) {
            R.id.deleteallnotes -> {
                if (repo.number != 0) {
                    viewModel.deleteAllNotes()
                    makeSnackBar("All locations deleted", colayout)
                } else {
                    makeSnackBar("Nothing to delete", colayout)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun backData(wifi: Int, bluetooth: Int, ringtone: Int, alarm: Int, notification: Int) {

        Log.d(
            TAG,
            MyAdapter.locNote.feature_name.toString().plus(MyAdapter.locNote.sub_locality.toString())
        )
        MyAdapter.locNote.wifi = wifi
        MyAdapter.locNote.bluetooth = bluetooth
        MyAdapter.locNote.ringtone = ringtone
        MyAdapter.locNote.alarm = alarm
        MyAdapter.locNote.notification = notification
        viewModel.update(MyAdapter.locNote)

    }

    fun startService() {
        val intent = Intent(applicationContext, LocationService::class.java)
        ContextCompat.startForegroundService(applicationContext, intent)
    }

    fun stopService() {
        val intent = Intent(applicationContext, LocationService::class.java)
        stopService(intent)
    }

    fun notificationPermission() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {

            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivityForResult(intent, DONT_DISTURB)

        }
    }


    fun checkPermissions() {

        Log.d(TAG, "checking permissions")

        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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


}

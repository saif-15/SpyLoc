package com.spyloc.view

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.*
import com.spyloc.R
import com.spyloc.R.id.*
import com.spyloc.makeToasty
import com.spyloc.model.LocNote
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.spyloc.viewModel.NoteRepository
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException
import java.lang.Thread.sleep


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    var isPermissionGranted: Boolean = false
    lateinit var mMap: GoogleMap
    var permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET
    )
    lateinit var geocoder: Geocoder
    var locationManager: LocationManager? = null
    var locNote: LocNote? = null
    lateinit var repository: NoteRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


        checkPermissions()
        loadAdd()

        repository = NoteRepository(application)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        search_location.setOnClickListener {
            searchLocation()
        }

        map_type.setOnClickListener {
            val popupMenu = PopupMenu(this@MapsActivity, it)
            popupMenu.inflate(R.menu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { item ->
                when (item?.itemId) {
                    normal -> {
                        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                        true
                    }
                    terrain -> {
                        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                        true
                    }
                    hybird -> {
                        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                        true
                    }
                    satellite -> {
                        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        true
                    }
                    else -> false
                }
            }
        }

        search_mylocation.setOnClickListener {
            progress_bar.visibility = View.VISIBLE
            getCurrentLocation()

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        Log.d("map Activity", "on map ready")

        mMap = googleMap
        try {
            mMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            e.printStackTrace()
        }


        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMapLongClickListener {

            geocoder = Geocoder(applicationContext)
            try {
                val loc_address = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                if (!loc_address.isEmpty()) {
                    val latlng = LatLng(loc_address[0].latitude, loc_address[0].longitude)
                    locNote = LocNote(
                        loc_address[0].countryCode,
                        loc_address[0].countryName,
                        loc_address[0].adminArea,
                        loc_address[0].featureName,
                        loc_address[0].locality,
                        loc_address[0].subAdminArea,
                        loc_address[0].subLocality,
                        loc_address[0].latitude,
                        loc_address[0].longitude
                    )
                    mMap.clear()
                    mMap.addMarker(
                        MarkerOptions().position(latlng).title(locNote?.feature_name.plus("," + locNote?.sub_locality))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    )
                    moveCamera(latlng, 16f)
                    progress_bar.visibility = View.GONE
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val bottomSheetExample = BottomSheetExample()
            bottomSheetExample.show(supportFragmentManager, "bottomsheet")
            bottomSheetExample.bottomSheetData({
                if (it && locNote != null) {
                    repository.insert(locNote)
                    finish()
                    locationManager = null
                } else {
                    makeToasty("Please Select Location")
                }
            }, {
                if (it && locNote != null) {
                    repository.insert(locNote)
                    makeToasty("Location Added")
                } else {
                    makeToasty("Please Select Location")
                }

            })
        }
        mMap.setOnMapClickListener {
            mMap.clear()
        }
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false
    }

    fun initMap() {

        Log.d("map Activity", "intializing map")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    fun checkPermissions() {

        Log.d("map Activity", "checking permissions")

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
                initMap()
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
                initMap()
            }
        }
    }

    fun getCurrentLocation() {
        locationManager = getSystemService(Service.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                permissions[1]
            ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                applicationContext,
                permissions[0]
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000000,
                    1000000f,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location?) {
                            val latLng = LatLng(location!!.latitude, location.longitude)
                            try {
                                moveCamera(latLng, zoom = 15f)
                                progress_bar.visibility = View.GONE
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?
                        ) {
                            Log.d("status", status.toString())

                        }

                        override fun onProviderEnabled(provider: String?) {

                        }

                        override fun onProviderDisabled(provider: String?) {

                        }

                    })


            } else
                if (locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000000,
                        1000000f,
                        object : LocationListener {
                            override fun onStatusChanged(
                                provider: String?,
                                status: Int,
                                extras: Bundle?
                            ) {
                            }

                            override fun onLocationChanged(location: Location?) {
                                val latLng = LatLng(location!!.latitude, location.longitude)
                                try {
                                    moveCamera(latLng, zoom = 15f)
                                    progress_bar.visibility = View.GONE
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }


                            override fun onProviderEnabled(provider: String?) {}

                            override fun onProviderDisabled(provider: String?) {}
                        })
                }
        }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun searchLocation() {
        if (search.text.toString().trim().isNotEmpty()) {
            geocoder = Geocoder(applicationContext)
            progress_bar.visibility = View.VISIBLE
            try {
                val location = search.text.toString()
                val loc_address = geocoder.getFromLocationName(location, 1)
                if (loc_address.isNotEmpty()) {
                    val latlng = LatLng(loc_address[0].latitude, loc_address[0].longitude)
                    locNote = LocNote(
                        loc_address[0].countryCode,
                        loc_address[0].countryName,
                        loc_address[0].adminArea,
                        loc_address[0].featureName,
                        loc_address[0].locality,
                        loc_address[0].subAdminArea,
                        loc_address[0].subLocality,
                        loc_address[0].latitude,
                        loc_address[0].longitude
                    )
                    mMap.addMarker(
                        MarkerOptions().position(latlng).title(locNote?.feature_name.plus("," + locNote?.sub_locality))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    )
                    moveCamera(latlng, 16f)
                    progress_bar.visibility = View.GONE
                } else {
                    makeToasty("Location not found")
                    progress_bar.visibility = View.GONE
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else {
            makeToasty("Enter Location")
        }
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    private fun loadAdd(){


        MobileAds.initialize(applicationContext,"ca-app-pub-2304912645023659~6563009661")

        val request = AdRequest.Builder().build()
       adView.loadAd(request)
    }

}

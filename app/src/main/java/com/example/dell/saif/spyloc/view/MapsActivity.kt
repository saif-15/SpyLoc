package com.example.dell.saif.spyloc.view

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.example.dell.saif.spyloc.R
import com.example.dell.saif.spyloc.R.id.*
import com.example.dell.saif.spyloc.makeToasty
import com.example.dell.saif.spyloc.model.LocNote
import com.example.dell.saif.spyloc.viewModel.NoteViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode


class MapsActivity : AppCompatActivity(), OnMapReadyCallback{

    var isPermissionGranted: Boolean = false
    lateinit var mMap: GoogleMap
    var permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET
    )
    var locationManager: LocationManager? = null
    lateinit var geocoder: Geocoder
    var locNote: LocNote? = null
    lateinit var viewModel: NoteViewModel
    val TAG = "Main Activity"
    val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        checkPermissions()

        viewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        search_button.setOnClickListener {
            if (search.text.toString().trim().isNotEmpty()) {
                geocoder = Geocoder(applicationContext)
                try {
                    val location_detail = search.text.toString()
                    val loc_address = geocoder.getFromLocationName(location_detail, 1)
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
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        )
                        moveCamera(latlng, 15f)
                    } else
                        makeToasty("Location not found")
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            } else {
                makeToasty("Enter Location")
            }
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

        my_location.setOnClickListener {
            getCurrentLocation()
        }
       /* add_button.setOnClickListener {


            if (locNote == null) {
                makeToasty("Please Select Location")
            } else {
                viewModel.insert(locNote)
                finish()
                locationManager = null
            }
        }*/
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
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )
                    moveCamera(latlng, 15f)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val bottomSheetExample=BottomSheetExample()
            bottomSheetExample.show(supportFragmentManager,"bottomsheet")
            bottomSheetExample.bottomSheetData({
                if(it && locNote != null)
                {
                    viewModel.insert(locNote)
                    finish()
                    locationManager = null
                } else { makeToasty("Please Select Location")}
        },{
                if(it && locNote != null)
                {
                    viewModel.insert(locNote)
                    makeToasty("Location Added")
                } else { makeToasty("Please Select Location")}

            })
        }
        mMap.setOnMapClickListener {
            mMap.clear()
        }
        mMap.uiSettings.setAllGesturesEnabled(true)
        mMap.uiSettings.isCompassEnabled = true
    }

    fun initMap() {

        Log.d("map Activity", "intializing map")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    fun checkPermissions() {

        Log.d("map Activity", "checking permissions")

        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION)
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
                initMap()
            }
        }
    }

    fun getCurrentLocation() {
        Log.d("map Activity", "getting current location")
        locationManager = getSystemService(Service.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(applicationContext, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(applicationContext, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000000,
                    1000000f,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location?) {
                            Log.d("map Activity", "on location changes")
                            val latitute: Double = location!!.latitude
                            val longitute: Double = location.longitude
                            val latLng = LatLng(latitute, longitute)
                            Log.d("map Activity", latitute.toString().plus("long".plus(longitute.toString()) + "GPS"))

                           /* Log.d(TAG, R.id.lattitude.toString())*/
                            geocoder = Geocoder(applicationContext)
                            try {
                                val address = geocoder.getFromLocation(latitute, longitute, 1)
                                Log.d("map Activity", "marker moving camera")
                                val lat = BigDecimal(address[0].latitude).setScale(4, RoundingMode.HALF_EVEN)
                                val lng = BigDecimal(address[0].longitude).setScale(4, RoundingMode.HALF_EVEN)
                                moveCamera(latLng, zoom = 15f)
                                Log.d(TAG, lat.toString().plus("  " + lng.toString()))
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
                if (locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000000,
                        1000000f,
                        object : LocationListener {
                            override fun onLocationChanged(location: Location?) {
                                Log.d("map Activity", "on location changes")

                                val latitute: Double = location!!.latitude
                                val longitute: Double = location.longitude
                                val latLng = LatLng(latitute, longitute)
                                Log.d(
                                    "map Activity",
                                    latitute.toString().plus("long".plus(longitute.toString()) + "Network")
                                )
                                geocoder = Geocoder(applicationContext)
                                try {
                                    val address = geocoder.getFromLocation(latitute, longitute, 1)
                                    moveCamera(latLng, zoom = 15f)
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

    private fun moveCamera(latLng: LatLng, zoom: Float) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    /*override fun onClickBottomSheet(pressed: Boolean) {

        if(pressed && locNote != null)
        {
            viewModel.insert(locNote)
            finish()
            locationManager = null
        } else { makeToasty("Please Select Location")

        }

    }*/

}

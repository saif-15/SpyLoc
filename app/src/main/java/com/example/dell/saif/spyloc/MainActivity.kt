package com.example.dell.saif.spyloc

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.polyak.iconswitch.IconSwitch
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),ConfigDialog.ConfigDialogListener{


    lateinit var viewModel: NoteViewModel
    lateinit var animation:Animation
    lateinit var animation1:Animation
    var TAG="MAin Activity"
    val SWITCH="switch"
    var switch=false
    val SHARED_PREF="sharedperferences"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        Log.d("MainActitvty", "Oncreate")

        val sharedPreferences=getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        switch=sharedPreferences.getBoolean(SWITCH,false)
        animation=AnimationUtils.loadAnimation(applicationContext,R.anim.fab_animation)
        animation1=AnimationUtils.loadAnimation(applicationContext,R.anim.recyclerview_animation)
        fab.startAnimation(animation)
        recyclerview.layoutManager = LinearLayoutManager(applicationContext)
        recyclerview.setHasFixedSize(false)
        recyclerview.startAnimation(animation1)
        viewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        val myAdapter = MyAdapter(applicationContext,viewModel,colayout,supportFragmentManager)
        recyclerview.adapter = myAdapter
        viewModel.getAllNotes().observe(this,
            Observer<List<LocNote>> { t -> myAdapter.submitList(t) })


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

                val locNote=myAdapter.getNoteAt(viewHolder.adapterPosition)
                    viewModel.delete(locNote)
                    Snackbar.make(colayout, "Location deleted", Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.rgb(35,193,235))
                        .setAction("UNDO") {
                            viewModel.insert(locNote)
                            Snackbar.make(colayout, "Location Added", Snackbar.LENGTH_SHORT).show()
                        }
                        .show()

            }

        }).attachToRecyclerView(recyclerview)


        start_stop_service.setCheckedChangeListener(object:IconSwitch.CheckedChangeListener{
            override fun onCheckChanged(current: IconSwitch.Checked?) {

                when(current){
                    IconSwitch.Checked.LEFT -> {stopService()
                        switch=false
                    Toast.makeText(applicationContext,"Spyloc stops Following You",Toast.LENGTH_SHORT).show()
                    val shared=getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
                    val editor=shared.edit()
                    editor.putBoolean(SWITCH,switch)
                    editor.apply()}
                    IconSwitch.Checked.RIGHT -> {
                        switch=true
                        startService()
                    Toast.makeText(applicationContext,"SpyLoc is Following You",Toast.LENGTH_SHORT).show()
                        val shared=getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
                        val editor=shared.edit()
                        editor.putBoolean(SWITCH,switch)
                        editor.apply()}
                }
            }

        })
        if(switch)
        start_stop_service.checked=IconSwitch.Checked.RIGHT
        else start_stop_service.checked=IconSwitch.Checked.LEFT
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
            else -> Toast.makeText(this, "Cant request map", Toast.LENGTH_SHORT).show()
        }
        return false
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.deleteallnotes -> {
                    viewModel.deleteAllNotes()
                    Snackbar.make(colayout, "All location deleted", Snackbar.LENGTH_SHORT).show()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
    override fun backData(wifi: Int, bluetooth: Int, ringtone: Int, alarm: Int,notification:Int) {

        Log.d(TAG,MyAdapter.locNote.feature_name.toString().plus(MyAdapter.locNote.sub_locality.toString()))
        MyAdapter.locNote.wifi=wifi
        MyAdapter.locNote.bluetooth=bluetooth
        MyAdapter.locNote.ringtone=ringtone
        MyAdapter.locNote.alarm=alarm
        MyAdapter.locNote.notification=notification
      viewModel.delete(MyAdapter.locNote)
        viewModel.insert(MyAdapter.locNote)
    }

    fun startService()
    {
        val intent=Intent(applicationContext,LocationService::class.java)
        ContextCompat.startForegroundService(applicationContext,intent)
    }
    fun stopService()
    {
        val intent=Intent(applicationContext,LocationService::class.java)
        stopService(intent)
    }


}

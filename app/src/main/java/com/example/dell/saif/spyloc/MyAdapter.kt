package com.example.dell.saif.spyloc

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.single.view.*

class MyAdapter(var context: Context, var viewModel:NoteViewModel,var colayout:CoordinatorLayout,
var support:FragmentManager):
    ListAdapter<LocNote, MyAdapter.ViewHolder>(diffCallback) {

    companion object {
        val diffCallback=object:DiffUtil.ItemCallback<LocNote>(){
            override fun areItemsTheSame(oldItem: LocNote, newItem: LocNote): Boolean {
                return oldItem.id==newItem.id
            }

            override fun areContentsTheSame(oldItem: LocNote, newItem: LocNote): Boolean {

                return oldItem.lat!!.equals(newItem.lat).and(oldItem.lng!!.equals(newItem.lng))
            }
        }
        lateinit var locNote: LocNote
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutInflater=LayoutInflater.from(parent.context)
        val view=layoutInflater.inflate(R.layout.single,parent,false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val current:LocNote=getItem(position)
        holder.feature_name.text=current.feature_name
        holder.sub_locality.text=current.sub_locality
        holder.sub_adminarea.text=current.subadmin_area
        holder.details.text=current.admin_area.plus(","+current.country_name.plus(","+current.country_code))
        holder.latitude.text="Latitude :${current.lat.toString()}"
        holder.longitude.text="Longitude :${current.lng.toString()}"
        holder.card_options.setOnClickListener {
            val popupMenu=PopupMenu(context.applicationContext,it)
            popupMenu.inflate(R.menu.card_menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener(object :PopupMenu.OnMenuItemClickListener
            {

                override fun onMenuItemClick(item: MenuItem?): Boolean {
                     return when(item?.itemId)
                    {
                        R.id.delete_card -> {
                            locNote=getNoteAt(holder.adapterPosition)
                            viewModel.delete(locNote)
                                Snackbar.make(colayout, "Location deleted", Snackbar.LENGTH_LONG)
                                    .setActionTextColor(Color.rgb(35,193,235))
                                    .setAction("UNDO") {
                                        viewModel.insert(locNote)
                                        Snackbar.make(colayout, "Location Added", Snackbar.LENGTH_SHORT).show()
                                    }
                                    .show()
                            true
                        }
                         R.id.configure_card -> {
                             locNote=getNoteAt(holder.adapterPosition)
                             val bundle=Bundle()
                             Log.d("My Adapter",locNote.wifi.toString().plus(locNote.bluetooth.toString().plus(locNote.ringtone.toString().plus(locNote.alarm.toString()))))
                             bundle.putInt("wifi",locNote.wifi)
                             bundle.putInt("bluetooth",locNote.bluetooth)
                             bundle.putInt("ringtone",locNote.ringtone)
                             bundle.putInt("alarm",locNote.alarm)
                             bundle.putInt("notification", locNote.notification)
                             val configDialog=ConfigDialog()
                             configDialog.arguments=bundle
                             configDialog.show(support,"Select Configuration")

                             true
                         }
                         else -> false
                    }
                }
            })
        }
    }

    fun getNoteAt(position: Int):LocNote
    {
        return getItem(position)
    }
   inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val feature_name: TextView =itemView.feature_name
        val sub_locality:TextView=itemView.sub_locality
       val sub_adminarea:TextView=itemView.sub_adminarea
       val details:TextView=itemView.details
       val latitude:TextView=itemView.lattitude
       val longitude:TextView=itemView.longitude
       val card_options:ImageView=itemView.options_card
    }

}
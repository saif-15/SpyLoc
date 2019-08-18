package com.spyloc.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spyloc.view.ConfigDialog
import com.spyloc.viewModel.NoteViewModel
import com.google.android.material.snackbar.Snackbar
import com.spyloc.R
import kotlinx.android.synthetic.main.single.view.*

class MyAdapter(private var context: Context, private var viewModel: NoteViewModel, private var colayout:CoordinatorLayout,
                private var support:FragmentManager):
    ListAdapter<LocNote, MyAdapter.ViewHolder>(diffCallback) {

    companion object {
        val diffCallback=object:DiffUtil.ItemCallback<LocNote>(){
            override fun areItemsTheSame(oldItem: LocNote, newItem: LocNote): Boolean {
                return oldItem.id==newItem.id
            }

            override fun areContentsTheSame(oldItem: LocNote, newItem: LocNote): Boolean {

                return (oldItem.lat!! == newItem.lat).and(oldItem.lng!! == newItem.lng)
            }
        }
         lateinit var locNote: LocNote
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutInflater=LayoutInflater.from(parent.context)
        val view=layoutInflater.inflate(R.layout.single,parent,false)
        return ViewHolder(view)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val current: LocNote =getItem(position)
        holder.feature_name.text=current.feature_name
        holder.sub_locality.text=current.sub_locality
        holder.sub_adminarea.text=current.subadmin_area
        holder.details.text=current.admin_area.plus(","+current.country_name.plus(","+current.country_code))
        holder.card_options.setOnClickListener {
            val popupMenu=PopupMenu(context,it)
            popupMenu.inflate(R.menu.card_menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { item ->
                when(item?.itemId) {
                    R.id.delete_card -> {
                        locNote =getNoteAt(holder.adapterPosition)
                        viewModel.delete(locNote)
                        Snackbar.make(colayout, "Location deleted", Snackbar.LENGTH_LONG)
                            .setActionTextColor(ContextCompat.getColor(context,R.color.colorAccent))
                            .setAction("UNDO") {
                                viewModel.insert(locNote)
                                Snackbar.make(colayout, "Location Added", Snackbar.LENGTH_SHORT).show()
                            }
                            .show()
                        true
                    }
                    R.id.configure_card -> {
                        locNote =getNoteAt(holder.adapterPosition)
                        val bundle=Bundle()
                        bundle.putInt("wifi", locNote.wifi)
                        bundle.putInt("bluetooth", locNote.bluetooth)
                        bundle.putInt("ringtone", locNote.ringtone)
                        bundle.putInt("alarm", locNote.alarm)
                        bundle.putInt("notification", locNote.notification)
                        val configDialog= ConfigDialog()
                        configDialog.arguments=bundle
                        configDialog.show(support,"Select Configuration")
                        true
                    }
                    else -> false
                }
            }
        }
    }

    fun getNoteAt(position: Int): LocNote
    {
        return getItem(position)
    }
   inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val feature_name: TextView =itemView.feature_name
        val sub_locality:TextView=itemView.sub_locality
       val sub_adminarea:TextView=itemView.sub_adminarea
       val details:TextView=itemView.details
       val card_options:ImageView=itemView.options_card
    }

}
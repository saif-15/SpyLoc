package com.example.dell.saif.spyloc.view

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dell.saif.spyloc.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import java.lang.ClassCastException

class BottomSheetExample : BottomSheetDialogFragment() {


   lateinit var listener1:(Boolean) -> Unit
    lateinit var listener2:(Boolean) -> Unit

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.bottom_sheet,container,false)
        val button1=view.add_location
        val button2=view.add_location_more
        button1.setOnClickListener {
            listener1(it.isPressed)
            dismiss()
        }
        button2.setOnClickListener {
            listener2(it.isPressed)
            dismiss()
        }

        return  view
    }
fun bottomSheetData(listener1:(Boolean)-> Unit,listener2:(Boolean)-> Unit)
{
    this.listener1=listener1
    this.listener2=listener2

}
}
package com.spyloc.view

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.spyloc.R
import kotlinx.android.synthetic.main.bottom_sheet.view.*

class BottomSheetExample : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP)
        setStyle(DialogFragment.STYLE_NORMAL,R.style.BottomSheetDialog)
    }

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
package com.spyloc.view

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.spyloc.R

class About : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity!!)
        val inflator = activity!!.layoutInflater
        val view = inflator.inflate(R.layout.about, null)
        builder.apply {
            setNegativeButton("Close"){dialog,_ ->
                dialog.dismiss()
            }
            setView(view)
            setCancelable(true)
        }
        return builder.create()
    }
}

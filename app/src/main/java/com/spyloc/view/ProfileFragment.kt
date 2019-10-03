package com.spyloc.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.edit
import com.spyloc.Constants.SHARED_PREF
import com.google.android.material.textfield.TextInputEditText
import com.makeramen.roundedimageview.RoundedImageView
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.spyloc.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.profile_view.view.*

class ProfileFragment : AppCompatDialogFragment() {

    lateinit var listener: (String, String) -> Unit
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity!!)
        val inflater: LayoutInflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.profile_view, null)

        val name: TextInputEditText = view.user_name
        val email: TextInputEditText = view.user_email
        val image:ImageView=view.user_image

        builder.apply {
            setView(view)
            name.setText(arguments!!.getString("user_name_transfer"))
            email.setText(arguments!!.getString("user_email_transfer"))
            if (arguments!!.getString("user_image_transfer") != null) {
                val img = Uri.parse(arguments!!.getString("user_image_transfer"))
                val transform = RoundedTransformationBuilder().apply {
                    cornerRadiusDp(100f)
                    oval(false)
                }.build()
                Picasso.get().load(img).fit().transform(transform).into(image)

            }
            setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.cancel()
            }
            setPositiveButton("Save") { dialogInterface: DialogInterface, i: Int ->
                if(name.text.toString().trim().isEmpty() && email.text.toString().trim().isEmpty()){
                    listener("name","username")
                }else{
                    listener(name.text.toString(), email.text.toString())
                }
                val preference = activity!!.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
                preference.edit {
                    putString("user_name", name.text.toString())
                    putString("user_email", email.text.toString())
                }
            }
        }
        return builder.create()

    }

    fun getValuesOf(listener: (String, String) -> Unit) {
        this.listener = listener
    }

}
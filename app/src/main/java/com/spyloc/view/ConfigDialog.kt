package com.spyloc.view


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.polyak.iconswitch.IconSwitch
import com.spyloc.R

class ConfigDialog : DialogFragment() {

    lateinit var wifi: IconSwitch
    lateinit var bluetooth: IconSwitch
    lateinit var ringer: IconSwitch
    lateinit var alarm: IconSwitch
    lateinit var notification: IconSwitch
    lateinit var listener: ConfigDialogListener
    var wifi_int = false
    var bluetooth_int = false
    var ringer_int = false
    var alarm_int = false
    var notification_int = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity!!)
        val inflater: LayoutInflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.config_dialog, null)
        wifi_int = arguments!!.getBoolean("wifi")
        bluetooth_int = arguments!!.getBoolean("bluetooth")
        ringer_int = arguments!!.getBoolean("ringtone")
        alarm_int = arguments!!.getBoolean("alarm")
        notification_int = arguments!!.getBoolean("notification")
        builder.setView(view)
            .setTitle("Select Configuration")
            .setNegativeButton(
                "Cancel"

            ) { dialog, which -> dialog!!.cancel() }
            .setPositiveButton("Save") { dialog, which ->
                wifi_int = when (wifi.checked) {
                    IconSwitch.Checked.LEFT -> false
                    else -> true
                }

                bluetooth_int = when (bluetooth.checked) {
                    IconSwitch.Checked.LEFT -> false
                    else -> true
                }

                ringer_int = when (ringer.checked) {
                    IconSwitch.Checked.LEFT -> false
                    else -> true
                }

                alarm_int = when (alarm.checked) {
                    IconSwitch.Checked.LEFT -> false
                    else -> true
                }

                notification_int = when (notification.checked) {
                    IconSwitch.Checked.LEFT -> false
                    else -> true
                }

                listener.backData(wifi_int, bluetooth_int, ringer_int, alarm_int, notification_int)
            }

        wifi = view.findViewById(R.id.wifi)
        bluetooth = view.findViewById(R.id.bluetooth)
        ringer = view.findViewById(R.id.ringer)
        alarm = view.findViewById(R.id.alarm)
        notification = view.findViewById(R.id.notification)


        wifi.checked = if (wifi_int) IconSwitch.Checked.RIGHT else IconSwitch.Checked.LEFT
        bluetooth.checked = if (bluetooth_int) IconSwitch.Checked.RIGHT else IconSwitch.Checked.LEFT
        ringer.checked = if (ringer_int) IconSwitch.Checked.RIGHT else IconSwitch.Checked.LEFT
        alarm.checked = if (alarm_int) IconSwitch.Checked.RIGHT else IconSwitch.Checked.LEFT
        notification.checked = if (notification_int) IconSwitch.Checked.RIGHT else IconSwitch.Checked.LEFT


        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as ConfigDialogListener

        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString().plus("should ovveride methods"))
        }
    }

    interface ConfigDialogListener {
        fun backData(wifi: Boolean, bluetooth: Boolean, ringtone: Boolean, alarm: Boolean, notification: Boolean)
    }
}
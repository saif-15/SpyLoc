package com.example.dell.saif.spyloc.view


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.dell.saif.spyloc.R
import com.polyak.iconswitch.IconSwitch

class ConfigDialog : DialogFragment() {

    lateinit var wifi: IconSwitch
    lateinit var bluetooth: IconSwitch
    lateinit var ringer: IconSwitch
    lateinit var alarm: IconSwitch
    lateinit var notification: IconSwitch
    lateinit var listener: ConfigDialogListener
    var wifi_int = 0
    var bluetooth_int = 0
    var ringer_int = 0
    var alarm_int = 0
    var notification_int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity!!)
        val inflater: LayoutInflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.config_dialog, null)
        wifi_int = arguments!!.getInt("wifi")
        bluetooth_int = arguments!!.getInt("bluetooth")
        ringer_int = arguments!!.getInt("ringtone")
        alarm_int = arguments!!.getInt("alarm")
        notification_int = arguments!!.getInt("notification")
        builder.setView(view)
            .setTitle("Select Configuration")
            .setNegativeButton(
                "Cancel"

            ) { dialog, which -> dialog!!.cancel() }
            .setPositiveButton("Save") { dialog, which ->
                wifi_int = when (wifi.checked) {
                    IconSwitch.Checked.LEFT -> 0
                    else -> 1
                }

                bluetooth_int = when (bluetooth.checked) {
                    IconSwitch.Checked.LEFT -> 0
                    else -> 1
                }

                ringer_int = when (ringer.checked) {
                    IconSwitch.Checked.LEFT -> 0
                    else -> 1
                }

                alarm_int = when (alarm.checked) {
                    IconSwitch.Checked.LEFT -> 0
                    else -> 1
                }

                notification_int = when (notification.checked) {
                    IconSwitch.Checked.LEFT -> 0
                    else -> 1
                }

                listener.backData(wifi_int, bluetooth_int, ringer_int, alarm_int, notification_int)
            }

        wifi = view.findViewById(R.id.wifi)
        bluetooth = view.findViewById(R.id.bluetooth)
        ringer = view.findViewById(R.id.ringer)
        alarm = view.findViewById(R.id.alarm)
        notification = view.findViewById(R.id.notification)


        wifi.checked = if (wifi_int == 1) IconSwitch.Checked.RIGHT else IconSwitch.Checked.LEFT
        bluetooth.checked = if (bluetooth_int == 1) IconSwitch.Checked.RIGHT else IconSwitch.Checked.LEFT
        ringer.checked = if (ringer_int == 1) IconSwitch.Checked.RIGHT else IconSwitch.Checked.LEFT
        alarm.checked = if (alarm_int == 1) IconSwitch.Checked.RIGHT else IconSwitch.Checked.LEFT
        notification.checked = if (notification_int == 1) IconSwitch.Checked.RIGHT else IconSwitch.Checked.LEFT


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
        fun backData(wifi: Int, bluetooth: Int, ringtone: Int, alarm: Int, notification: Int)
    }
}
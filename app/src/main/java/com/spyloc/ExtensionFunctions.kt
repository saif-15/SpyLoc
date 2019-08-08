package com.spyloc

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar


fun Vibration(context: Context, mode: String) {
    val time = when (mode) {
        "Short" -> 150L;"Long" -> 1000L; else -> 400L
    }
    if (Build.VERSION.SDK_INT >= 26) {
        (context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator).vibrate(
            VibrationEffect.createOneShot(
                time,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    } else {
        (context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator).vibrate(time)
    }
}

fun Context.makeToasty(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, duration).show()
}

fun makeSnackBar(msg: String, layout: CoordinatorLayout, length: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(layout, msg, length)
}

object Constants {
    const val CHANNEL_ID1 = "com.example.dell.saif.spyloc.channel.id1"
    const val CHANNEL_ID2 = "com.example.dell.saif.spyloc.channel.id2"
    const val WHITE_LIGHT = 0xFFFFF
    const val BLUE_LIGHT = 0x0000FF
    const val YELLOW_LIGHT = 0xFFFF00
    const val RED_LIGHT = 0xFF0000
    const val CYAN_LIGHT = 0x00FFFF
    const val PURPLE_LIGHT = 0xFF00FF
    const val SHARED_PREF = "shared_preference"
    const val DONT_DISTURB = 1998
    const val SWITCH = "switch"

}

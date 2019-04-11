package com.example.dell.saif.spyloc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_splash.*
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Handler


class Splash : AppCompatActivity() {

    var handler=Handler()
    var progressStatus=0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
        {
            progress.progressBackgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            progress.progressTintList= ColorStateList.valueOf(Color.WHITE)

        }
       button.setOnClickListener { start()
       button.isEnabled=false
       quitbutton.isEnabled=false}
        quitbutton.setOnClickListener { finish()
        quitbutton.isEnabled=false
        button.isEnabled=false}

    }
    fun start()
    {
       Thread(Runnable {
           while (progressStatus < 100) {
               progressStatus += 1

               handler.post {
                   progress.progress=progressStatus

               }
               try {

                   Thread.sleep(15)
               } catch (ex:InterruptedException ) {
                   ex.printStackTrace()
               } finally {

               }
           }
           if (progressStatus == 100) {
               val intent =Intent(applicationContext,MainActivity::class.java)
               startActivity(intent)
               finish()
           }
       }).start()
    }

}

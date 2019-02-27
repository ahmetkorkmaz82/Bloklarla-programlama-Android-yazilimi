package com.ahmetkorkmaz.kotlin1

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.content.Context
import android.content.IntentFilter
import android.view.animation.AnimationUtils
import com.ahmetkorkmaz.kotlin1.R.anim.giris
import com.ahmetkorkmaz.kotlin1.R.id.girisYazi

import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreenActivity : AppCompatActivity() {


override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_splash_screen)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    val mesaj = intent.getStringExtra("Mesaj")
    val intent = Intent(this@SplashScreenActivity,FullscreenActivity::class.java)


    if(mesaj == getString(R.string.see_you)){
        girisYazi.text=mesaj
        object: CountDownTimer(1500,1000){
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                this@SplashScreenActivity.finish()
            }

        }.start()

    }
    else{
        object: CountDownTimer(1000,1000){
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                startActivity(intent)
                this@SplashScreenActivity.finish()
            }

        }.start()

    }


}
override fun onDestroy() {
    super.onDestroy()

}


}

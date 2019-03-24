package com.example.fishingapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class SelectDeviceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_device_layout)
    }

    fun backToActivity(view: View) {
        val Intent = Intent(this, MotorActivity::class.java)
        finish()
    }

}

package com.example.fishingapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

//Test
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /** Called when the user taps the button */
    fun goActivityMotor(view: View) {
        val intent = Intent(this, MotorActivity::class.java)
        startActivity(intent)
    }

    fun goActivityStatus(view: View) {
        val intent = Intent(this,StatusActivity::class.java)
        startActivity(intent)
    }
}


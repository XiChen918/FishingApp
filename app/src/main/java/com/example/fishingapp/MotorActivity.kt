package com.example.fishingapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MotorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motor)
    }
    /** Called when the user taps the button */
    fun goActivityMain(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        finish()
    }
}

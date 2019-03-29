package com.example.fishingapp

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.control_layout.*
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*


class ControlActivity: AppCompatActivity() {

    //Needs be checked
    companion object {
        val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var myBluetoothSocket: BluetoothSocket? = null
        lateinit var myProgress: ProgressDialog
        lateinit var myBluetoothAdapter: BluetoothAdapter
        var myIsConnected: Boolean = false
        lateinit var myAddress: String

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)
        myAddress = intent.getStringExtra(SelectDeviceActivity.EXTRA_ADDRESS)

        ConnectToDevice(this).execute()
        val btnShow = findViewById<Button>(R.id.btnShow)
        var inputRPM: String
        //Read in value and store it as String
        btnShow.setOnClickListener{
            inputRPM = receiveInput()
            sendCommand(inputRPM)
        }
        //Test
        //Send data to Microcontroller
        /*btnShow.setOnClickListener{
            sendCommand(inputRPM)
        }*/
        /*
        control_off.setOnClickListener{
            sendCommand("b")
        }*/

        control_disconnect.setOnClickListener{
            disconnect()
        }
    }

    private fun receiveInput(): String {
        val input = findViewById<EditText>(R.id.editText)
        println(input)
        return input.text.toString()
    }

    private fun sendCommand(input: String) {
        if (myBluetoothSocket != null) {
            try{
                myBluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun disconnect() {
        if (myBluetoothSocket != null) {
            try {
                myBluetoothSocket!!.close()
                myBluetoothSocket = null
                myIsConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }
    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String> () {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            myProgress = ProgressDialog.show(context, "Connecting", "Please wait")
        }

        override fun doInBackground(vararg params: Void?): String? {
            try {
                if (myBluetoothSocket == null || !myIsConnected) {
                    myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = myBluetoothAdapter.getRemoteDevice(myAddress)
                    myBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    myBluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            //Needs be fixed
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                myIsConnected = true
            }
            myProgress.dismiss()
        }
    }
}
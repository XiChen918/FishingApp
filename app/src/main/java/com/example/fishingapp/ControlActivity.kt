package com.example.fishingapp

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.control_layout.*
import kotlinx.android.synthetic.main.control_layout.view.*
import kotlinx.android.synthetic.main.select_device_layout.*
import org.jetbrains.anko.toast
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule


class ControlActivity: AppCompatActivity() {
    //Needs be checked
    companion object {
        val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var myBluetoothSocket: BluetoothSocket? = null
        lateinit var myProgress: ProgressDialog
        lateinit var myBluetoothAdapter: BluetoothAdapter
        var myIsConnected: Boolean = false
        lateinit var myAddress: String
        val mmInStream: InputStream? = null
        private val mInterval:Long = 5000 // 5 seconds by default, can be changed later
        private var mHandler: Handler? = null
    }


    private inner class MyTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String {
            val buffer = ByteArray(256)
            val bytes:Int
            var tmpIn: InputStream? = null
            if (myBluetoothSocket != null) {
                try {
                    tmpIn = myBluetoothSocket!!.inputStream
                    val mmInStream = DataInputStream(tmpIn)
                    bytes = mmInStream.read(buffer)
                    val readMessage = String(buffer, 0, bytes)
                    //input.text=""
                    //input.text = readMessage
                    return readMessage
                } catch (e:IOException) {
                    e.printStackTrace()
                }
            }
            return "Nothing"
        }

        override fun onPostExecute(result: String) {
            val showCountTextView = findViewById<TextView>(R.id.textView)
            showCountTextView.text = result
        }
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


        //Read RPM from microcontroller (bluetooth)

        //val showCountTextView = findViewById<TextView>(R.id.textView)

        mHandler = Handler()
        startRepeatingTask()

        /*
        mHandler.postDelayed({
            receiveBluetooth(showCountTextView)
        }, 1000)
        */

        /*Timer().schedule(1000){
            receiveBluetooth(showCountTextView)
            // do something after 1 second
        }*/
        //Manually Refresh
        /*
        val btnRefresh = findViewById<Button>(R.id.btnRefresh)
        btnRefresh.setOnClickListener {
            receiveBluetooth(showCountTextView)
        }*/
        //Try to automatically refresh


        /*
        handler.postDelayed({
            receiveBluetooth(showCountTextView)
        }, 2000)
        */


        //val handler:Handler = Handler()
        /*
        val thread = object : Thread() {
            override fun run() {
                try {
                    while (!this.isInterrupted) {
                        Thread.sleep(2000)
                        runOnUiThread {
                            receiveBluetooth(showCountTextView)
                        }
                    }
                } catch (e: InterruptedException) {
                }

            }
        }

        thread.start()
        */

        //btnRefresh.setOnClickListener {
        //thread.start()
        //}


        //Stop Retraction process
        val btnStop = findViewById<Button>(R.id.btnStop)
        btnStop.setOnClickListener{
            sendCommand("0")
        }

        //Start Retraction Process directly

        val btnStart = findViewById<Button>(R.id.btnStart)
        btnStart.setOnClickListener{
            sendCommand("1")
        }

        control_disconnect.setOnClickListener{
            disconnect()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        stopRepeatingTask()
    }

    var mStatusChecker: Runnable = object : Runnable {
        override fun run() {
            try {
                /*
                val showCountTextView = findViewById<TextView>(R.id.textView)
                receiveBluetooth(showCountTextView) //this function can change value of mInterval.
                */
                MyTask().execute()
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler?.postDelayed(this, mInterval)
            }
        }
    }

    fun startRepeatingTask() {
        mStatusChecker.run()
    }

    fun stopRepeatingTask() {
        mHandler?.removeCallbacks(mStatusChecker)
    }

    private fun receiveInput(): String {
        val input = findViewById<EditText>(R.id.editText)
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
    /*
    private fun receiveBluetooth(input: TextView) {
        val buffer = ByteArray(256)
        val bytes:Int
        var tmpIn: InputStream? = null
        if (myBluetoothSocket != null) {
            try {
                tmpIn = myBluetoothSocket!!.inputStream
                val mmInStream = DataInputStream(tmpIn)
                bytes = mmInStream.read(buffer)
                val readMessage = String(buffer, 0, bytes)
                input.text=""
                input.text = readMessage
            } catch (e:IOException) {
                e.printStackTrace()
            }
        }
    }*/


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

package com.example.fishingapp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.select_device_layout.*
import org.jetbrains.anko.toast

//It needs first connect to Bluetooth Device (ESP32 microcontroller)
//Then get the data from microcontroller
class SelectDeviceActivity : AppCompatActivity() {


    private var myBluetoothAdapter: BluetoothAdapter? = null
    private lateinit var myPairedDevices: Set<BluetoothDevice>
    private val REQUEST_ENABLE_BLUETOOTH = 1

    companion object {
        val EXTRA_ADDRESS: String = "Device_address"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_device_layout)

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (myBluetoothAdapter == null) {
            toast("This device doesn't support bluetooth")
            return
        }

        //already check Null in previous if condition
        if (!myBluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        select_device_refresh.setOnClickListener{
            pairedDeviceList()
        }
    }

    private fun pairedDeviceList(){
        myPairedDevices = myBluetoothAdapter!!.bondedDevices
        val list : ArrayList<BluetoothDevice> = ArrayList()

        if (!myPairedDevices.isEmpty()) {
            for (device: BluetoothDevice in myPairedDevices) {
                list.add(device)
                Log.i("device", ""+device)
            }
        } else {
            toast("No paired bluetooth device found")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        select_device_list.adapter = adapter
        select_device_list.onItemClickListener = AdapterView.OnItemClickListener { _,_, position, _ ->
            val device: BluetoothDevice = list[position]
            val address: String = device.address

            val intent = Intent(this, ControlActivity::class.java)
            intent.putExtra(EXTRA_ADDRESS, address)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                if (myBluetoothAdapter!!.isEnabled) {
                    toast("Bluetooth has been enabled")
                } else {
                    toast("Bluetooth has been disabled")
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                toast ("Bluetooth enabling has been stopped" )
            }
        }
    }

    //Back to the Motor Activity
    fun backToActivity(view: View) {
        val Intent = Intent(this, MotorActivity::class.java)
        finish()
    }

}

package com.example.fishingapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import kotlinx.android.synthetic.main.activity_status.view.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import org.osmdroid.util.GeoPoint
import android.location.Criteria
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import kotlinx.android.synthetic.main.activity_status.*
import org.osmdroid.util.LocationUtils.getLastKnownLocation
import java.io.IOException


class StatusActivity : AppCompatActivity() {
    var map: MapView? = null


    //Map ends in here
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        //Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_status)

        val map = findViewById<MapView>(R.id.map)
        map!!.setTileSource(TileSourceFactory.MAPNIK)
        //Enable zoom feature
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)
        val mapController = map.getController()
        mapController.setZoom(18)

        var startPoint = GeoPoint(40.4286, -86.91)

        mapController.setCenter(startPoint)
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager


        //User Location display

        val myLocationoverlay = MyLocationNewOverlay(map)
        myLocationoverlay.enableFollowLocation()
        myLocationoverlay.enableMyLocation()
        map.getOverlays().add(myLocationoverlay)

        //Find user's location with button click
        currentLocation.setOnClickListener{
            startPoint = manuallyGetLocation(locationManager)
            mapController.setCenter(startPoint)
        }

    }

    //Needs check
    private fun manuallyGetLocation(input:LocationManager): GeoPoint {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED) {
            try {
                val locationGPS = input.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val longitude = locationGPS.getLongitude()
                val latitude = locationGPS.getLatitude()
                val startPoint = GeoPoint(longitude, latitude)
                //mapController.setCenter(startPoint)
                return startPoint
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return GeoPoint(40.428, -86.912)
    }

/*
    private fun getLastBestLocation(input: LocationManager): Location {
        val locationGPS = input.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val locationNet = input.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        var GPSLocationTime: Long = 0
        if (null != locationGPS) {
            GPSLocationTime = locationGPS!!.getTime()
        }

        var NetLocationTime: Long = 0

        if (null != locationNet) {
            NetLocationTime = locationNet!!.getTime()
        }

        return if (0 < GPSLocationTime - NetLocationTime) {
            locationGPS
        } else {
            locationNet
        }
    }
*/
    public override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map?.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    public override fun onPause() {
        super.onPause()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map?.onPause()  //needed for compass, my location overlays, v6.0.0 and up
    }

    /** Called when the user taps the button */
    fun goActivityMain(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        finish()
    }
}

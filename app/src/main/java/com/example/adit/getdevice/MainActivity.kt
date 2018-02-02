package com.example.adit.getdevice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.TextView
import com.example.adit.paint.services.LocationTrackingService
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainActivity : AppCompatActivity() {

    val MY_PERMISSIONS_STATE = 0
    var devicePermission = false
    var locationPermission = false
    val phoneState:String = Manifest.permission.READ_PHONE_STATE
    val fineLocation:String =  Manifest.permission.ACCESS_FINE_LOCATION
    val permissionGranted = PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getPermissions(phoneState)
        getPermissions(fineLocation)
        buildLayout()
        this.startService(Intent(this, LocationTrackingService::class.java))
    }

    fun buildLayout(){
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        verticalLayout{
            button("Get IMEI"){
                id=R.id.imeiBtn
                onClick { getDeviceInfo(tm) }
            }
            textView("imeiTxt"){
                id=R.id.imeiTxt
                text="imei = "
            }
            button("Get Location"){
                id=R.id.locBtn
                onClick { getLocation() }
            }
            textView("locTxt"){
                id=R.id.locTxt
                text= "Location = "
            }
        }
    }

    fun getDeviceInfo(tm:TelephonyManager){
        val imeiTxt = find<TextView>(R.id.imeiTxt)
        if (devicePermission)
            imeiTxt.text = tm.getDeviceId()
        else
            getPermissions(phoneState)
    }

    fun getLocation(){
        val locTxt = find<TextView>(R.id.locTxt)
        Log.d("tag", "$locationPermission")
        if (locationPermission) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGPSEnabled || !isNetworkEnabled)
                showSettingsAlert()
            var latestLocation = getLatestLocation(LocationManager.GPS_PROVIDER)
            if (latestLocation == "null"){
                latestLocation = getLatestLocation(LocationManager.NETWORK_PROVIDER)
            }
            locTxt.text = latestLocation
        } else {
            getPermissions(fineLocation)
        }
    }

    fun showSettingsAlert(){
        alert("Can not get location", "Please enable GPS and Network") {
            yesButton{}
        }.show()
    }

    fun getLatestLocation(provider:String):String{
        var result:String = "null"

        try {
            val location = locationManager.getLastKnownLocation(provider)
            toast( "$provider getting latest location $location")
            if (location != null) {
                val lat = location.latitude
                var long = location.longitude
                result = "Latitude = $lat Longitude = $long"
            }

        } catch (e: SecurityException){
            Log.d("tag", "error = $e")
        }
        return result
    }

    fun getPermissions(permissions:String){
        when(permissions){
            phoneState -> {devicePermission = checkPermissions(phoneState)}
            fineLocation -> {locationPermission = checkPermissions(fineLocation)}
        }
    }

    fun checkPermissions(permissions:String):Boolean{
        var isPermission = false
        if(ActivityCompat.checkSelfPermission(this, permissions) != permissionGranted)
            requestPermission(permissions)
        else
            isPermission = true
        return isPermission
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
       // super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_STATE) {
            if (permissions.size == 1 && permissions.contains(phoneState) && grantResults.contains(permissionGranted))
                devicePermission = true
            if (permissions.size == 1 && permissions.contains(fineLocation) && grantResults.contains(permissionGranted))
                locationPermission = true
        }
    }

    fun requestPermission(permission:String){
        ActivityCompat.requestPermissions(this, arrayOf(permission), MY_PERMISSIONS_STATE)
    }

    override fun onResume() {
        super.onResume()

    }


}


package com.example.adit.paint.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import org.jetbrains.anko.toast

/**
 * Created by adit on 7/10/2017.
 */
class LocationTrackingService:Service() {


    var locationManager: LocationManager? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }
    override fun onCreate() {
        toast("service created")
        Log.d(TAG, "service created")
        if(locationManager == null)
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, DISTANCE, locationListeners[1])
        } catch (e:SecurityException){
            toast("fail to request location update $e")
        } catch (e:IllegalArgumentException){
            toast("Network does not exist $e")
        }

        updateLocation(LocationManager.GPS_PROVIDER, "fail", "error")

    }

    fun updateLocation(provider: String, securityMsg:String, illegalMsg:String){
        try {
            locationManager?.requestLocationUpdates(provider, INTERVAL, DISTANCE, locationListeners[1])
        } catch (e:SecurityException){
            toast(securityMsg + e)
        } catch (e:IllegalArgumentException){
            toast(illegalMsg + e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null){
            for(i in 0..locationListeners.size){
                try{
                    locationManager?.removeUpdates(locationListeners[1])
                } catch (e:Exception){
                    Log.w(TAG, "failed to remove location listeners")
                }
            }
        }
    }

    companion object {

        val TAG = "Location Service"

        val INTERVAL = 1000.toLong()
        val DISTANCE = 10.toFloat()

        val locationListeners = arrayOf(
                LTRLocationListener(LocationManager.GPS_PROVIDER),
                LTRLocationListener(LocationManager.NETWORK_PROVIDER)
        )

        class LTRLocationListener(provider: String):LocationListener{
            val lastLocation = Location(provider)

            override fun onLocationChanged(location: Location?) {
                lastLocation.set(location)
            }

            override fun onProviderDisabled(p0: String?) {

            }

            override fun onProviderEnabled(p0: String?) {

            }

            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

            }
        }
    }


}
package com.labzapp.customer.activities


import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.labzapp.customer.R
import com.labzapp.customer.databinding.ActivityProfileUpdateBinding
import com.labzapp.customer.utilities.maps.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.labzapp.customer.utilities.maps.PermissionUtils.isPermissionGranted
import com.labzapp.customer.utilities.maps.PermissionUtils.requestPermission

class ProfileUpdateActivity : AppCompatActivity(), GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {
    private  lateinit var  binding:ActivityProfileUpdateBinding

    private var permissionDenied = false
    private var lastKnownLocation: Location? = null

    private lateinit var map: GoogleMap
    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    val DEF_LOCATION = LatLng(9.9312, 76.2673)
    val ZOOM_LEVEL = 16f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileUpdateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding.textVwLocation.setOnClickListener(){
            getDeviceLocation()
        }

        binding.locSearchMap.setOnClickListener(){

            //MapDialog().show(supportFragmentManager, "MapWindow")

        }

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        enableMyLocation()
        with(map.uiSettings) {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = false
        }
        getDeviceLocation()

    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
        // [END maps_check_location_permission]
    }

    override fun onMyLocationButtonClick(): Boolean {
       // Toast.makeText(this, "-----MyLocation button clicked----", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        //CameraUpdateFactory.newLatLngZoom(DEF_LOCATION, ZOOM_LEVEL)
        //addMarker(MarkerOptions().position(SYDNEY))

        return false
    }

    override fun onMyLocationClick(location: Location) {
        //Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }

    // [START maps_check_location_permission_result]
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
            // [END_EXCLUDE]
        }
    }

    // [END maps_check_location_permission_result]
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        newInstance(true).show(supportFragmentManager, "dialog")
    }


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (!permissionDenied) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        //Log.d("-----Last Location-----", task.result?.latitude.toString()+", "+task.result?.longitude.toString())
                        if(task.result != null) {
                            val geocoder = Geocoder(this)
                            val list = geocoder.getFromLocation(
                                task.result.latitude,
                                task.result.longitude,
                                1
                            )
                            val fullAddress = list[0].getAddressLine(0)
                            // Log.d("-----FULL ADDRESS-----", fullAddress)
                            binding.locationAddress.text = fullAddress
                        }
                        else
                        {
                            binding.locationAddress.text = "Location not available."
                        }

                        if (lastKnownLocation != null) {
                            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), ZOOM_LEVEL
                            ))
                        }
                    } else {
                        Log.d("Dloc", "Current location is null. Using defaults.")
                        Log.e("Dloc", "Exception: %s", task.exception)
                        map?.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(DEF_LOCATION, ZOOM_LEVEL))
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }








    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }




}
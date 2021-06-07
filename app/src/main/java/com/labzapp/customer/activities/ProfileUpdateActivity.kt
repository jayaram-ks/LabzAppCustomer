package com.labzapp.customer.activities


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.labzapp.customer.R
import com.labzapp.customer.databinding.ActivityProfileUpdateBinding
import com.labzapp.customer.models.ProfileResponse
import com.labzapp.customer.models.SaveCustomerResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.districtz
import com.labzapp.customer.utilities.maps.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.labzapp.customer.utilities.maps.PermissionUtils.isPermissionGranted
import com.labzapp.customer.utilities.maps.PermissionUtils.requestPermission
import com.labzapp.customer.utilities.snackze
import com.labzapp.customer.utilities.toastz
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProfileUpdateActivity : AppCompatActivity(), GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback,AdapterView.OnItemSelectedListener{
    private  lateinit var  binding:ActivityProfileUpdateBinding

    private lateinit var locationManager: LocationManager
    var gpsStatus = false
    private lateinit var context: Context
    var intentgps: Intent? = null

    private var permissionDenied = false
    private var lastKnownLocation: Location? = null
    private var districtid = 0
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val DEF_LOCATION = LatLng(9.3475, 76.7638)
    val ZOOM_LEVEL = 16f

    private var custGender: Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileUpdateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        context = applicationContext
        checkGpsStatus()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding.textVwLocation.setOnClickListener {
            checkGpsStatus()
            map.clear()
            enableMyLocation()
            getDeviceLocation()
        }

        binding.locSearchMap.setOnClickListener {

            checkGpsStatus()

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = false
                map.clear()
                if (lastKnownLocation != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude), ZOOM_LEVEL))
                    map.addMarker(MarkerOptions().draggable(true).position( LatLng(lastKnownLocation!!.latitude,
                    lastKnownLocation!!.longitude)))
                    setMarkerDragListener(map)
                }
                else
                {

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(DEF_LOCATION, 7f))
                    map.addMarker(MarkerOptions().draggable(true).position( DEF_LOCATION))
                    setMarkerDragListener(map)
                }

            } else {
                // Permission to access the location is missing. Show rationale and request permission
                requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true
                )
            }
        }


        val values: ArrayList<String> = ArrayList(districtz.values)
        val adapter = ArrayAdapter(this, R.layout.spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner = findViewById<View>(R.id.cdistrict) as Spinner
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        val authTokn: String? = "Bearer "+SharedPrefManager.getInstance(this).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(this).apiToken

        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getProfile(authTokn, apiTokn)
        requestCall.enqueue(object : Callback<ProfileResponse> {

            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    resp.let {
                        val cname = it.customer.name
                        val cmobile = it.customer.phone
                        val cpincode = it.customer.pincode
                        val titleTxt = "$cname($cmobile), Pin:$cpincode"
                        binding.custTitle.text = titleTxt
                   }

                } else {
                    snackze(view, resp?.message.toString(), binding.custAge.id)
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                snackze(view, t.message.toString(), binding.custAge.id)
            }
        })


        binding.updateProfile.setOnClickListener{

            val custLatitude = binding.usrLat.text.toString()
            val custLongitude = binding.usrLong.text.toString()
            val custAge = binding.custAge.text.toString()
            val custAddrs = binding.custAddress.text.toString()
            val custDistrict = districtid.toString()

            var errmsg: String? = null

            if((custLatitude == "") or (custLongitude == "" ))
            {
                errmsg = "Please select a location from map"
                binding.textVwLocation.performClick()
            }

            if(errmsg != null){
                snackze(view,errmsg, binding.custAge.id)
                return@setOnClickListener
            }

            val authTokn: String? = "Bearer "+SharedPrefManager.getInstance(this).authKey
            val apiTokn: String? = SharedPrefManager.getInstance(this).apiToken

            val apiService = ServiceBuilder.buildService(ApiService::class.java)
            val requestCall = apiService.saveCustomer(authTokn, apiTokn,custLatitude,custLongitude,custAge,custGender.toString(),custAddrs,custDistrict)
            requestCall.enqueue(object : Callback<SaveCustomerResponse> {

                override fun onResponse(call: Call<SaveCustomerResponse>, response: Response<SaveCustomerResponse>) {
                    val resp = response.body()
                    if (resp?.code == 200) {
                        SharedPrefManager.getInstance(applicationContext).profileCompleted()
                        val intent = Intent(this@ProfileUpdateActivity, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                    } else {
                        snackze(view,resp?.message.toString(), binding.custAge.id)
                    }
                }

                override fun onFailure(call: Call<SaveCustomerResponse>, t: Throwable) {
                    snackze(view,t.message.toString(), binding.custAge.id)
                }
            })

        }

    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio1 ->
                    if (checked) {
                       custGender = 1
                    }
                R.id.radio2 ->
                    if (checked) {
                        custGender = 2
                    }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
       //  parent.getItemAtPosition(pos)
        val keysz: ArrayList<Int> = ArrayList(districtz.keys)
        districtid =  keysz[pos]

    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)

        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        enableMyLocation()
        with(map.uiSettings) {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
        }
        getDeviceLocation()
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
        // Return false so that we don't consume the event and the default behavior still occurs
        return false
    }

    override fun onMyLocationClick(location: Location) {
        //Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }

    // [START maps_check_location_permission_result]
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
                        if(task.result != null) {
                            binding.usrLat.text = lastKnownLocation!!.latitude.toString()
                            binding.usrLong.text = lastKnownLocation!!.longitude.toString()

                            val geocoder = Geocoder(this)
                            val list = geocoder.getFromLocation(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude, 1)
                            var fullAddress = "Location address not Available"
                            if(list.size > 0)
                            {
                                fullAddress = list[0].getAddressLine(0)
                            }
                            binding.locationAddress.text = fullAddress

                        }
                        else {
                            binding.locationAddress.text = "Location not available."
                        }
                        if (lastKnownLocation != null) {
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude), ZOOM_LEVEL))

                            map.addMarker(MarkerOptions().draggable(true).position( LatLng(lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                            setMarkerDragListener(map)
                        }

                    } else {
                        Log.d("Dloc", "Current location is null. Using defaults.")
                        Log.e("Dloc", "Exception: %s", task.exception)
                        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(DEF_LOCATION, ZOOM_LEVEL))
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun setMarkerDragListener(map: GoogleMap) {
        map.setOnMarkerDragListener(object : OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
            }

            override fun onMarkerDrag(marker: Marker) {
                //val p = marker.position
            }

            override fun onMarkerDragEnd(marker: Marker) {
                val actualLatLng: LatLng = marker.position
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(actualLatLng!!.latitude, actualLatLng!!.longitude), ZOOM_LEVEL))
                val geocoder = Geocoder(this@ProfileUpdateActivity)
                val list = geocoder.getFromLocation(actualLatLng!!.latitude, actualLatLng!!.longitude, 1)
                binding.usrLat.text = actualLatLng!!.latitude.toString()
                binding.usrLong.text = actualLatLng!!.longitude.toString()
                var fullAddress = "Location address not Available"
                if(list.size > 0)
                {
                    fullAddress = list[0].getAddressLine(0)
                }
                binding.locationAddress.text = fullAddress
            }
        })
    }

    private fun checkGpsStatus() {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (gpsStatus) {

        } else {
            toastz(this,"Please enable Location service(GPS) on your Device")
            gpsStatus()
        }
    }
    fun gpsStatus() {
        intentgps = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intentgps);
    }

    override fun onStart() {
        super.onStart()

        if(SharedPrefManager.getInstance(this).completedProfile){
            val intent = Intent(applicationContext, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
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
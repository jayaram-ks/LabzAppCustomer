package com.labzapp.customer.fragments

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.labzapp.customer.R
import com.labzapp.customer.databinding.FragmentBookingHomeBinding
import com.labzapp.customer.models.BookingLabsResponse
import com.labzapp.customer.models.ProfileResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.maps.PermissionUtils
import com.labzapp.customer.utilities.toastz
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class BookingHomeFragment : Fragment() , GoogleMap.OnMyLocationButtonClickListener,
GoogleMap.OnMyLocationClickListener, OnMapReadyCallback,
ActivityCompat.OnRequestPermissionsResultCallback{


    private lateinit var locationManager: LocationManager
    var gpsStatus = false
   
    var intentgps: Intent? = null

    private var permissionDenied = false
    private var lastKnownLocation: Location? = null

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val DEF_LOCATION = LatLng(9.9312, 76.2673)
    val ZOOM_LEVEL = 16f


    private var patientLat: Double = 0.0
    private var patientLong: Double = 0.0

    private var _binding: FragmentBookingHomeBinding? = null
    private val binding get() = _binding!!

    private var bookedTestpos: String? = null

    var cal: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lastKnownLocation?.latitude  = DEF_LOCATION.latitude
        lastKnownLocation?.longitude = DEF_LOCATION.longitude
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookingHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val frgment = SelfDialogFragment()
        frgment.isCancelable = false
       // frgment.show(childFragmentManager, SelfDialogFragment.TAG)

        val transaction = childFragmentManager.beginTransaction()
        transaction.addToBackStack(null)
        frgment.show(transaction, SelfDialogFragment.TAG)

        childFragmentManager.setFragmentResultListener("bookingKey", this) { key, bundle ->
            val bookingFor = bundle.getString("booking_for")
            if(bookingFor == "1"){
                checkGpsStatus()
                fillMyData()
            }else{
                checkGpsStatus()
                getDeviceLocation()
               // fillOtherData()
            }
        }


        // create an OnDateSetListener
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        binding.buttonDate1.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_book) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.reqLabTests.setOnClickListener{

            val bundle = Bundle()
            bundle.putString("selcted_tests_pos", bookedTestpos)
            val fragmentTests = BookingTestsFragment()
            fragmentTests.arguments = bundle
            val transaction2 = childFragmentManager.beginTransaction()
            transaction2.addToBackStack(BookingTestsFragment.TAG)
            fragmentTests.show(transaction2,BookingTestsFragment.TAG)
        }

        childFragmentManager.setFragmentResultListener("testKey", this) { key, bundle ->

            bookedTestpos =  bundle.getString("sel_pos")
            var bookTestIds: String? = bundle.getString("sel_test_ids")

            val tIdArray: MutableList<String>? = bookTestIds?.let { Json.decodeFromString(it) }

            if (tIdArray != null) {
                binding.homeTestCount.text = tIdArray.size.toString() + " Tests Selected"
                if (tIdArray.size > 0) {
                    fetchMatchingLabs(tIdArray)
                }
            }

            Log.d("POSITIOnzzzz",bookedTestpos.toString())
            Log.d("testzzzzzzz",bookTestIds.toString())
        }

        binding.selectedLab.setOnClickListener{

        }

    }

    private fun updateDateInView() {
        val myFormat = "yyyy-MM-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.textViewDate1.text = sdf.format(cal.time)
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
       // getDeviceLocation()
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(
                activity as AppCompatActivity, LOCATION_PERMISSION_REQUEST_CODE,
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
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (PermissionUtils.isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
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
    override fun onResume() {
        super.onResume()
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
        PermissionUtils.PermissionDeniedDialog.newInstance(true).show(childFragmentManager, "dialog")
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
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if(task.result != null) {
                            binding.usrLat.text = lastKnownLocation!!.latitude.toString()
                            binding.usrLong.text = lastKnownLocation!!.longitude.toString()

                            val geocoder = Geocoder(requireContext())
                            val list = geocoder.getFromLocation(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude, 1)
                            val fullAddress = list[0].getAddressLine(0)
                            binding.locationAddress.text = fullAddress

                        }
                        else {
                            binding.locationAddress.text = "Location not available."
                        }
                        if (lastKnownLocation != null) {
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude), ZOOM_LEVEL))

                            map.addMarker(MarkerOptions().draggable(true).position( LatLng(lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude)).icon(
                                BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_AZURE)))
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
        map.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
            }

            override fun onMarkerDrag(marker: Marker) {
                //val p = marker.position
            }

            override fun onMarkerDragEnd(marker: Marker) {
                val actualLatLng: LatLng = marker.position
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(actualLatLng!!.latitude, actualLatLng!!.longitude), ZOOM_LEVEL))
                val geocoder = Geocoder(requireActivity())
                val list = geocoder.getFromLocation(actualLatLng!!.latitude, actualLatLng!!.longitude, 1)
                val fullAddress = list[0].getAddressLine(0)
                binding.usrLat.text = actualLatLng!!.latitude.toString()
                binding.usrLong.text = actualLatLng!!.longitude.toString()
                binding.locationAddress.text = fullAddress
            }
        })
    }

    private fun checkGpsStatus() {
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (gpsStatus) {
            //toastz(this,"GPS is Enabled")
        } else {
            toastz(requireContext(),"Please enable Location service(GPS) on your Device")
            gpsStatus()
        }
    }
    private fun gpsStatus() {
        intentgps = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intentgps);
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchMatchingLabs(selectedTests: MutableList<String>){
        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getLabsHavingTests(authTokn, apiTokn,patientLat,patientLong,selectedTests)

        requestCall.enqueue(object : Callback<BookingLabsResponse> {
            override fun onResponse(call: Call<BookingLabsResponse>, response: Response<BookingLabsResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    resp.let {
                        if (!isAdded) return
                            Log.d("--sads",it.labswithtest.toString())
                    }

                } else {
                    toastz(requireActivity(),resp?.message.toString())
                }
            }
            override fun onFailure(call: Call<BookingLabsResponse>, t: Throwable) {
                toastz(requireActivity(),t.message.toString())
            }
        })
    }

    private fun fillMyData(){
        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getProfile(authTokn, apiTokn)
        requestCall.enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    resp.let {
                        if (!isAdded) return
                        val clatitude  = it.customer.latitude!!
                        val clongitude   = it.customer.longitude!!
                        patientLat = clatitude
                        patientLong = clongitude
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(clatitude,clongitude), ZOOM_LEVEL))
                        map.addMarker(MarkerOptions().draggable(true).position( LatLng(clatitude,clongitude)).icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_GREEN)))
                        setMarkerDragListener(map)

                        val geocoder = Geocoder(requireContext())
                        val list = geocoder.getFromLocation(clatitude, clongitude, 1)
                        val fullAddress = list[0].getAddressLine(0)
                        binding.locationAddress.text = fullAddress
                        binding.usrLat.text = clatitude.toString()
                        binding.usrLong.text = clongitude.toString()
                    }

                } else {
                    toastz(requireActivity(),resp?.message.toString())
                }
            }
            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                toastz(requireActivity(),t.message.toString())
            }
        })
    }

    private fun fillOtherData(){
        //clear userdata form
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val TAG = "BookingHomeFragment"
    }


}
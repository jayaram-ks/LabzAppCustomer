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
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
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
import com.labzapp.customer.models.BookingDataTransfer
import com.labzapp.customer.models.Labswithtest
import com.labzapp.customer.models.ProfileResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.maps.PermissionUtils
import com.labzapp.customer.utilities.snackze
import com.squareup.picasso.Picasso
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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
    private var _binding: FragmentBookingHomeBinding? = null
    private val binding get() = _binding!!
    private var bookingFor: String? = null
    private var bookedTestpos: String? = null
    private var bookTestIds: String? = null
    private var tIdArray: MutableList<String> = ArrayList()
    private var bookedLabpos: String? = null
    private var bookLabstring: String? = null
    private var labDataArray: ArrayList<Labswithtest> = ArrayList()
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
        //---SELF / OTHER pop up
        val frgment = SelfDialogFragment()
        frgment.isCancelable = true
        val transaction = childFragmentManager.beginTransaction()
        frgment.show(transaction, SelfDialogFragment.TAG)

        childFragmentManager.setFragmentResultListener("bookingKey", this) { key, bundle ->
            bookingFor = bundle.getString("booking_for")
            if(bookingFor == "1"){ //self
                //checkGpsStatus()
                fillMyData()
            }else if(bookingFor == "2"){
                checkGpsStatus()
                getDeviceLocation()
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

        updateDateInView()

        val datedialog =  DatePickerDialog(
            requireContext(),
            dateSetListener,
            // set DatePickerDialog to point to today's date when it loads up
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        datedialog.datePicker.minDate = cal.timeInMillis

        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        binding.buttonDate1.setOnClickListener {
            datedialog.show()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_book) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        //----TESTS POP-UP-----
        binding.reqLabTests.setOnClickListener{
            clearLabdata()
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
            bookTestIds = bundle.getString("sel_test_ids")

            tIdArray = bookTestIds?.let { Json.decodeFromString(it) }!!

            if (tIdArray != null) {
                if (tIdArray.size > 0) {
                    binding.homeTestCount.text = tIdArray.size.toString() + " Tests Selected"
                } else{
                    binding.homeTestCount.text ="No Tests Selected"
                    clearLabdata()
                }
            }

        }
        //----LABS POPUP-----
        binding.selectedLab.setOnClickListener{
            val selatitude = binding.usrLat.text.toString()
            val selongitude = binding.usrLong.text.toString()
            if(tIdArray.size < 1){
                snackze(requireView(),"Please select required tests",binding.homeTestCount.id)
                return@setOnClickListener
            }else if((selatitude == "") or (selongitude == "")){
                snackze(requireView(),"Please enable Location and select Location to search for nearest labs",binding.homeTestCount.id)
                return@setOnClickListener
            }

            val bundle = Bundle()
            bundle.putString("param1", Json.encodeToString(tIdArray))
            bundle.putString("param2", selatitude)
            bundle.putString("param3", selongitude)
            bundle.putString("param4", bookedLabpos)
            val fragmentSelectLab = BookingLabsFragment()
            fragmentSelectLab.arguments = bundle
            val transactionlab = childFragmentManager.beginTransaction()
            transactionlab.addToBackStack(BookingLabsFragment.TAG)
            fragmentSelectLab.show(transactionlab,BookingLabsFragment.TAG)
        }

        childFragmentManager.setFragmentResultListener("labKey", this) { key, bundle ->

            bookedLabpos =  bundle.getString("selpos")
            bookLabstring = bundle.getString("sel_labs")

            labDataArray = bookLabstring?.let { Json.decodeFromString(it) }!!

            if (labDataArray.size > 0) {
                binding.selectedLabName.text = labDataArray[0].name
                binding.selectedLabAddress.text = labDataArray[0].address
                if(labDataArray[0].thumbnail != null) {
                    Picasso.with(context).load(labDataArray[0].thumbnail).fit().centerCrop()
                        .into(binding.selectedLablogo)
                }else {
                    Picasso.with(context).load(R.drawable.no_lab).fit().centerCrop()
                        .into(binding.selectedLablogo)
                }
            } else{
                clearLabdata()
            }
        }

        ///SUBMIT COLLECTED DATA TO PREVIEW
        binding.continueBookBtn.setOnClickListener {
            val patbookfor =  bookingFor
            val patlatitude = binding.usrLat.text.toString()
            val patlongitude = binding.usrLong.text.toString()
            val patprefdate = binding.textViewDate1.text.toString()

            var errmessage: String? = null
            if(patbookfor == null)
            {
                return@setOnClickListener
            }
            else if( (patlatitude == "") or (patlongitude == "") )
            {
                errmessage = "Please select patient's Location from Map"
            }
            else if(patprefdate == "")
            {
                errmessage = "Please select Preferred Date"
            }
            else if(tIdArray.size < 1)
            {
                errmessage = "Please select required Tests"
            }
            else if(labDataArray.size < 1)
            {
                errmessage = "Please select preferred Lab"
            }

            if(errmessage != null) {
                snackze(requireView(),errmessage.toString(),binding.continueBookBtn.id)
                return@setOnClickListener
            } else{

                var arrayBookData = BookingDataTransfer(patbookfor.toString(),patlatitude,patlongitude,patprefdate,tIdArray,labDataArray)

                val bundle = Bundle()
                bundle.putString("param1", Json.encodeToString(arrayBookData))
                val prevFragment = BookingPreviewFragment()
                prevFragment.arguments = bundle
                val transPrev = parentFragmentManager.beginTransaction()
                transPrev.replace(R.id.booking_container,prevFragment)
               // transPrev.addToBackStack(BookingPreviewFragment.TAG)
                transPrev.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                transPrev.commit()
            }
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
        if(bookingFor == "2")
        {
            lifecycleScope.launch {
                delay(3000)
                checkGpsStatus()
                getDeviceLocation()
            }
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
                binding.usrLat.text = actualLatLng!!.latitude.toString()
                binding.usrLong.text = actualLatLng!!.longitude.toString()
                var fullAddress = "Location address not Available"
                if(list.size > 0)
                {
                    fullAddress = list[0].getAddressLine(0)
                }
                binding.locationAddress.text = fullAddress
                clearLabdata()
            }
        })
    }

    private fun clearLabdata(){
        bookedLabpos = null
        bookLabstring = null
        labDataArray.clear()
        binding.selectedLabAddress.text = null
        binding.selectedLabName.text = "Select a Lab"
        binding.selectedLablogo.setImageResource(0)
    }

    private fun checkGpsStatus() {
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (gpsStatus) {

        } else {
            snackze(requireView(),"Please enable Location service(GPS) on your Device",binding.homeTestCount.id)
            lifecycleScope.launch {
                delay(2000)
                gpsStatus()
            }
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
                        map.clear()
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(clatitude,clongitude), ZOOM_LEVEL))
                        map.addMarker(MarkerOptions().draggable(true).position( LatLng(clatitude,clongitude)).icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_GREEN)))
                        setMarkerDragListener(map)

                        val geocoder = Geocoder(requireContext())
                        val list = geocoder.getFromLocation(clatitude, clongitude, 1)
                        var fullAddress = "Location address not Available"
                        if(list.size > 0)
                        {
                            fullAddress = list[0].getAddressLine(0)
                        }
                        binding.locationAddress.text = fullAddress
                        binding.usrLat.text = clatitude.toString()
                        binding.usrLong.text = clongitude.toString()
                    }

                } else {
                    view?.let{ snackze(it,resp?.message.toString(),binding.homeTestCount.id)}
                }
            }
            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                view?.let{ snackze(it,t.message.toString(),binding.homeTestCount.id)}
            }
        })
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
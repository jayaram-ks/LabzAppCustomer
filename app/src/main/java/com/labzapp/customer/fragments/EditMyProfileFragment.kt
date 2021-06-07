package com.labzapp.customer.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
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
import com.labzapp.customer.databinding.FragmentEditMyProfileBinding
import com.labzapp.customer.models.EditProfileResponse
import com.labzapp.customer.models.ProfileResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.districtz
import com.labzapp.customer.utilities.maps.PermissionUtils
import com.labzapp.customer.utilities.snackze
import com.labzapp.customer.utilities.snackzsucc
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class EditMyProfileFragment : Fragment(), AdapterView.OnItemSelectedListener, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback  {

    private var _binding: FragmentEditMyProfileBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null

    private var districtid = 0
    private var custGender: Int = 1

    private lateinit var locationManager: LocationManager
    var gpsStatus = false
    var intentgps: Intent? = null
    private var permissionDenied = false
    private var lastKnownLocation: Location? = null
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val DEF_LOCATION = LatLng(9.9312, 76.2673)
    val ZOOM_LEVEL = 16f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        lastKnownLocation?.latitude  = DEF_LOCATION.latitude
        lastKnownLocation?.longitude = DEF_LOCATION.longitude

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_profiledit) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        val values: ArrayList<String> = ArrayList(districtz.values)
        val adapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner = binding.cdistrict
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
        binding.radio1.setOnClickListener{ view -> onRadioButtonClicked(view) }
        binding.radio2.setOnClickListener { view -> onRadioButtonClicked(view) }

        fetchProfile()

        binding.updateProfileBtn.setOnClickListener {
            binding.updateProfileBtn.isClickable = false
            submitMyProfileData()
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

    private fun onRadioButtonClicked(view: View) {
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

    private fun fetchProfile(){
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
                        binding.myName.setText(it.customer.name)
                        binding.myAge.setText(it.customer.age.toString())
                        custGender = it.customer.gender!!
                        binding.myAddress.setText(it.customer.address)
                        binding.myPincode.setText(it.customer.pincode.toString())
                        districtid = it.customer.district!!
                        binding.cdistrict.setSelection(districtid)
                        if(custGender == 1){
                            binding.cgender.check(R.id.radio1);
                        }
                        else
                        {
                            binding.cgender.check(R.id.radio2)
                        }

                        val clatitude  = it.customer.latitude!!
                        val clongitude   = it.customer.longitude!!
                        map.clear()
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(clatitude,clongitude), ZOOM_LEVEL))
                        map.addMarker(
                            MarkerOptions().draggable(true).position( LatLng(clatitude,clongitude)).icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_VIOLET)))
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
                    view?.let{ snackze(it,resp?.message.toString(),binding.myName.id)}
                }
            }
            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                view?.let{ snackze(it,t.message.toString(),binding.myName.id)}
            }
        })
    }

    private fun submitMyProfileData(){

        val userName = binding.myName.text.toString()
        val userAge = binding.myAge.text.toString()
        val userAddress = binding.myAddress.text.toString()
        val userPincode = binding.myPincode.text.toString()
        val userGender = custGender.toString()
        val userDistrict = districtid.toString()
        val userLat = binding.usrLat.text.toString()
        val userLng = binding.usrLong.text.toString()


        if ((userName != "") and (userAge != "")  and (userGender != "")
            and (userAddress != "") and (userPincode != "") and (userDistrict != "")) {

            val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
            val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken

            val apibookService = ServiceBuilder.buildService(ApiService::class.java)
            val bookrequestCall = apibookService.updateProfile(authTokn, apiTokn,userName,userAge,userGender,userAddress,userPincode,userDistrict,userLat,userLng)
            bookrequestCall.enqueue(object : Callback<EditProfileResponse> {
                override fun onResponse(call: Call<EditProfileResponse>, response: Response<EditProfileResponse>) {
                    val resp = response.body()
                    if (resp?.code == 200) {
                        view?.let{mss -> snackzsucc(mss,resp?.message.toString(),binding.myName.id) }
                        val succFragment = MyprofileFragment()
                        val trans = parentFragmentManager.beginTransaction()
                        trans.replace(R.id.frame_container,succFragment)
                        trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        trans.commit()
                    } else {
                        view?.let{err -> snackze(err,resp?.message.toString(),binding.myName.id) }
                        binding.updateProfileBtn.isClickable = true
                    }
                }
                override fun onFailure(call: Call<EditProfileResponse>, t: Throwable) {
                    view?.let{err -> snackze(err,t.message.toString(),binding.myName.id) }
                }
            })
        }
        else
        {
            view?.let{err -> snackze(err,"Please fill all Patient Details Fields",binding.myName.id) }
            binding.updateProfileBtn.isClickable = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        with(map.uiSettings) {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = false
        }
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
                //on map drag force reset and reselect new  labs based on location

            }
        })
    }



    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditMyProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "EditMyProfileFragment"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
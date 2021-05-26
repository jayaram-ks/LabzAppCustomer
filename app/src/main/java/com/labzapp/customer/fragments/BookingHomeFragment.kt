package com.labzapp.customer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.labzapp.customer.databinding.FragmentBookingHomeBinding
import com.labzapp.customer.models.ProfileResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.districtz
import com.labzapp.customer.utilities.genderz
import com.labzapp.customer.utilities.toastz
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class BookingHomeFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentBookingHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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
        frgment.show(childFragmentManager, SelfDialogFragment.TAG)

        childFragmentManager.setFragmentResultListener("bookingKey", this) { key, bundle ->
            val bookingFor = bundle.getString("booking_for")
            if(bookingFor == "1"){
                fillMyData()
            }else{
                fillOtherData()
            }
        }
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
                       // binding.userName.text = it.customer.name
                       // binding.userAge.text = it.customer.age.toString()
                       // binding.userPhone.text = it.customer.phone
                        //binding.userGender.text = genderz[it.customer.gender]
                      //  binding.userAddress.text = it.customer.address
                       // binding.userPincode.text = it.customer.pincode.toString()
                      //  binding.userDistrict.text = districtz[it.customer.district]
                       // binding.userLat.text = it.customer.latitude
                      //  binding.userLong.text = it.customer.longitude
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
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BookingHomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
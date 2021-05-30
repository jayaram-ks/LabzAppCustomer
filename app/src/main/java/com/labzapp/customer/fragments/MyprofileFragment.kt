package com.labzapp.customer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.labzapp.customer.databinding.FragmentMyprofileBinding
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

class MyprofileFragment : Fragment() {

    private var _binding: FragmentMyprofileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyprofileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                        binding.userName.text = it.customer.name
                        binding.userAge.text = it.customer.age.toString()
                        binding.userPhone.text = it.customer.phone
                        binding.userGender.text = genderz[it.customer.gender]
                        binding.userAddress.text = it.customer.address
                        binding.userPincode.text = it.customer.pincode.toString()
                        binding.userDistrict.text = districtz[it.customer.district]
                        binding.userLat.text = it.customer.latitude.toString()
                        binding.userLong.text = it.customer.longitude.toString()
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


}
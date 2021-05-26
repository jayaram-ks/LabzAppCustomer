package com.labzapp.customer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.labzapp.customer.R
import com.labzapp.customer.databinding.FragmentProfileBinding
import com.labzapp.customer.models.ProfileResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.toastz
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val homeF = HomeFragment()
    private val aboutF = AboutFragment()
    private val myprofileF = MyprofileFragment()
    private val mybookingsF = MybookingsFragment()
    private val myresultsF = MyresultsFragment()
    private val termsF  = TermsFragment()
    private val contactF = ContactFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchProfile()

        binding.goHome.setOnClickListener { view -> menuClick(view) }
        binding.goAbout.setOnClickListener { view -> menuClick(view) }
        binding.goProfile.setOnClickListener { view -> menuClick(view) }
        binding.goBookings.setOnClickListener { view -> menuClick(view) }
        binding.goResults.setOnClickListener { view -> menuClick(view) }
        binding.goTerms.setOnClickListener { view -> menuClick(view) }
        binding.goContact.setOnClickListener { view -> menuClick(view) }
        binding.goLogout.setOnClickListener { view -> menuClick(view) }
    }

    private fun menuClick(v: View?) {
        when (v?.id) {
            binding.goHome.id -> { setCurrentFragment(homeF) }
            binding.goAbout.id -> { setCurrentFragment(aboutF) }
            binding.goProfile.id -> { setCurrentFragment(myprofileF) }
            binding.goBookings.id -> { setCurrentFragment(mybookingsF) }
            binding.goResults.id -> { setCurrentFragment(myresultsF) }
            binding.goTerms.id -> { setCurrentFragment(termsF) }
            binding.goContact.id -> { setCurrentFragment(contactF) }
            binding.goLogout.id -> {
                //TODO
            }
            else -> {
                //TODO
            }
        }
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
                        binding.userPhone.text = it.customer.phone
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

    private fun setCurrentFragment(openfragmt: Fragment){
        val appCompatActivity = context as AppCompatActivity
        val transaction = appCompatActivity.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, openfragmt)
        transaction.addToBackStack(null)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.commit()
    }

}
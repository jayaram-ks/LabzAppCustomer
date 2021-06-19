package com.labzapp.customer.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.labzapp.customer.R
import com.labzapp.customer.databinding.FragmentProfileBinding
import com.labzapp.customer.models.ProfileResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.gotoHome
import com.labzapp.customer.utilities.logoutFromDevice
import com.labzapp.customer.utilities.snackze
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val homeF = HomeFragment()
    private val aboutF = AboutFragment()
    private val myprofileF = MyprofileFragment()
    private val editmyprofileF = EditMyProfileFragment()
    private val mybookingsF = MybookingsFragment()
    private val myresultsF = ReportsFragment()
    private val termsF  = TermsFragment()
    private val contactF = ContactFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                gotoHome(requireActivity())
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isAdded) return
        binding.goHome.setOnClickListener { view -> menuClick(view) }
        binding.goAbout.setOnClickListener { view -> menuClick(view) }
        binding.goProfile.setOnClickListener { view -> menuClick(view) }
        binding.goeditProfile.setOnClickListener { view -> menuClick(view) }
        binding.goBookings.setOnClickListener { view -> menuClick(view) }
        binding.goResults.setOnClickListener { view -> menuClick(view) }
        binding.goTerms.setOnClickListener { view -> menuClick(view) }
        binding.goContact.setOnClickListener { view -> menuClick(view) }
        binding.goLogout.setOnClickListener { view -> menuClick(view) }
        fetchProfile()
    }

    private fun menuClick(v: View?) {
        when (v?.id) {
            binding.goHome.id -> { setCurrentFragment(homeF,false) }
            binding.goAbout.id -> { setCurrentFragment(aboutF,true) }
            binding.goProfile.id -> { setCurrentFragment(myprofileF,true) }
            binding.goeditProfile.id -> { setCurrentFragment(editmyprofileF,true) }
            binding.goBookings.id -> { setCurrentFragment(mybookingsF,true) }
            binding.goResults.id -> { setCurrentFragment(myresultsF,true) }
            binding.goTerms.id -> { setCurrentFragment(termsF,true) }
            binding.goContact.id -> { setCurrentFragment(contactF,true) }
            binding.goLogout.id -> { performLogout() }
            else -> {
                //TODO
            }
        }
    }


    private fun setCurrentFragment(openfragmt: Fragment,addtoBackStck:Boolean){

        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, openfragmt)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        if(addtoBackStck){
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    private fun performLogout(){

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Logout")
        builder.setMessage("Are you Sure?")
        builder.setPositiveButton("Yes") { dialog, which ->
            dialog.dismiss()
            logoutFromDevice(requireContext())
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
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

                    view?.let{ snackze(it,resp?.message.toString(),binding.goeditProfile.id) }
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {

                view?.let{ snackze(it,t.message.toString(),binding.goeditProfile.id) }
            }
        })
    }



}
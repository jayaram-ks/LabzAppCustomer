package com.labzapp.customer.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.labzapp.customer.R
import com.labzapp.customer.activities.BookingActivity
import com.labzapp.customer.adapters.BannersAdapter
import com.labzapp.customer.databinding.FragmentHomeBinding
import com.labzapp.customer.models.BannerData
import com.labzapp.customer.models.BannerResponse
import com.labzapp.customer.models.MakeCallResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.snackze
import com.labzapp.customer.utilities.snackzsucc
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentHomeBinding? = null
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchBanners()
        //----BOOK A TEST----
        binding.bookNewTest.setOnClickListener{
            val intent = Intent(requireActivity(), BookingActivity::class.java)
            startActivity(intent)
        }
        //----REQUEST A CALL---
        binding.reqACall.setOnClickListener {
            requestACall()
        }
        //----VIEW LABS----
        binding.viewLabshome.setOnClickListener {
            val succFragment = LabsFragment()
            val trans = parentFragmentManager.beginTransaction()
            trans.replace(R.id.frame_container,succFragment)
            trans.addToBackStack(null)
            trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            trans.commit()
        }
        //----UPLOAD PRESCRIPTION----
        binding.uploadPrescription.setOnClickListener {
            val frUploadtyp = PrescFileTypeDialog()
            val transPresc = childFragmentManager.beginTransaction()
            frUploadtyp.show(transPresc,null)
        }
    }

    private fun fetchBanners() {

        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken

        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getBanners(authTokn, apiTokn)
        requestCall.enqueue(object : Callback<BannerResponse> {
            override fun onResponse(call: Call<BannerResponse>, response: Response<BannerResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    showBanners(resp.banner)
                } else {
                    view?.let{snackze(it,resp?.message.toString(),binding.viewLabshome.id) }
                }
            }

            override fun onFailure(call: Call<BannerResponse>, t: Throwable) {
                view?.let{snackze(it,t.message.toString(),binding.viewLabshome.id) }
            }
        })
    }

    private fun showBanners(bannerlist: List<BannerData>)
    {
        if (!isAdded) return
        val progBar: ProgressBar = binding.progressBar
        progBar.visibility = View.GONE
        binding.viewPager2.adapter = BannersAdapter(requireContext(),bannerlist)

        lifecycleScope.launch {
            while(true){
                for(i in 0..bannerlist.size){
                    delay(3000)
                    if(i==0){
                        binding.viewPager2.setCurrentItem(i,true)
                    }else{
                        binding.viewPager2.setCurrentItem(i,true)
                    }
                }
            }
        }
    }

    private fun requestACall() {

        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.registerCustomerCall(authTokn, apiTokn)
        requestCall.enqueue(object : Callback<MakeCallResponse> {
            override fun onResponse(call: Call<MakeCallResponse>, response: Response<MakeCallResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    view?.let{mss -> snackzsucc(mss,resp?.message.toString(),binding.viewLabshome.id) }
                    binding.reqACall.isClickable = false
                } else {
                    view?.let{snackze(it,resp?.message.toString(),binding.viewLabshome.id) }
                }
            }

            override fun onFailure(call: Call<MakeCallResponse>, t: Throwable) {
                view?.let{snackze(it,t.message.toString(),binding.viewLabshome.id) }
            }
        })
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
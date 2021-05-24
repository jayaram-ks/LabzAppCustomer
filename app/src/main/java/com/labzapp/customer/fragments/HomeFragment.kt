package com.labzapp.customer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.labzapp.customer.adapters.BannersAdapter
import com.labzapp.customer.databinding.FragmentHomeBinding
import com.labzapp.customer.models.BannerData
import com.labzapp.customer.models.BannerResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.toastz
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fetchBanners()




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
                    activity?.let { toastz(it,resp?.message.toString()) }
                }
            }

            override fun onFailure(call: Call<BannerResponse>, t: Throwable) {
                activity?.let { toastz(it,t.message.toString()) }
            }
        })
    }

    private fun showBanners(bannerlist: List<BannerData>)
    {
        if (!isAdded) return
        binding.viewPager2.adapter = BannersAdapter(requireContext(),bannerlist)

        lifecycleScope.launch {
            while(true){
                for(i in 0..bannerlist.size){
                    delay(3500)
                    if(i==0){
                        binding.viewPager2.setCurrentItem(i,true)
                    }else{
                        binding.viewPager2.setCurrentItem(i,true)
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
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
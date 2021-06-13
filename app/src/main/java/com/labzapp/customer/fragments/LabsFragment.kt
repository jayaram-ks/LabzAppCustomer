package com.labzapp.customer.fragments

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.labzapp.customer.adapters.LabsAdapter
import com.labzapp.customer.databinding.FragmentLabsBinding
import com.labzapp.customer.models.LabData
import com.labzapp.customer.models.LabsResponse
import com.labzapp.customer.models.ProfileResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.snackze
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LabsFragment : Fragment() {

    private var _binding: FragmentLabsBinding? = null
    private val binding get() = _binding!!

    private var myLat : Double = 0.00
    private var myLong : Double = 0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLabsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillMyData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchLabs(){
        if (!isAdded) return
        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken

        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.listLabs(authTokn, apiTokn,myLat,myLong)
        requestCall.enqueue(object : Callback<LabsResponse> {
            override fun onResponse(call: Call<LabsResponse>, response: Response<LabsResponse>) {
                val resp = response.body()

                if (resp?.code == 200) {

                    resp.labs?.let{
                        showLabs(it)
                    }

                } else {
                    view?.let{ snackze(it,resp?.message.toString(),binding.progressBar.id) }
                }
            }
            override fun onFailure(call: Call<LabsResponse>, t: Throwable) {
                view?.let{ snackze(it,t.message.toString(),binding.progressBar.id) }
            }
        })
    }

    private fun showLabs(lablist: List<LabData>)
    {
        if (!isAdded) return
        val progBar: ProgressBar = binding.progressBar
        progBar.visibility = View.GONE
        if(lablist.isEmpty()){
            view?.let{er -> snackze(er,"No labs available near your location.Please change your location and try.",binding.progressBar.id) }
            return
        }


        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.labsRecyclerview.layoutManager = layoutManager
        binding.labsRecyclerview.adapter = LabsAdapter(requireContext(),lablist)
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
                    resp.customer.let {
                       myLat = it.latitude!!
                       myLong = it.longitude!!
                        if( (myLat.toString() != "") and (myLong.toString() != "") )
                        {
                            fetchLabs()
                        }
                    }

                } else {
                    view?.let{ snackze(it,resp?.message.toString(),binding.progressBar.id)}
                }
            }
            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                view?.let{ snackze(it,t.message.toString(),binding.progressBar.id)}
            }
        })
    }


}
package com.labzapp.customer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.labzapp.customer.adapters.LabsAdapter
import com.labzapp.customer.databinding.FragmentLabsBinding
import com.labzapp.customer.models.LabData
import com.labzapp.customer.models.LabsResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.toastz
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LabsFragment : Fragment() {

    private var _binding: FragmentLabsBinding? = null
    private val binding get() = _binding!!

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
        fetchLabs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchLabs(){

        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken

        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.listLabs(authTokn, apiTokn,8.8932,76.6141)
        requestCall.enqueue(object : Callback<LabsResponse> {

            override fun onResponse(call: Call<LabsResponse>, response: Response<LabsResponse>) {
                val resp = response.body()

                if (resp?.code == 200) {

                    resp.labs?.let{
                        showLabs(it)
                    }

                } else {
                    activity?.let { toastz(it,resp?.message.toString()) }
                }
            }

            override fun onFailure(call: Call<LabsResponse>, t: Throwable) {
                activity?.let { toastz(it,t.message.toString()) }
            }
        })
    }

    private fun showLabs(lablist: List<LabData>)
    {
        if (!isAdded) return
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.labsRecyclerview.layoutManager = layoutManager
        binding.labsRecyclerview.adapter = LabsAdapter(requireContext(),lablist)
    }
}
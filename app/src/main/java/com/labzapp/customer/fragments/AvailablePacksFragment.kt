package com.labzapp.customer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.labzapp.customer.R
import com.labzapp.customer.adapters.AvailPacksAdapter
import com.labzapp.customer.databinding.FragmentAvailablePacksBinding
import com.labzapp.customer.models.AvailPackResponse
import com.labzapp.customer.models.PackDetails
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.snackze
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val ARG_PARAM1 = "lab_id"
private const val ARG_PARAM2 = "lab_title"
private const val ARG_PARAM3 = "lab_logo"
private const val ARG_PARAM4 = "lab_address"

class AvailablePacksFragment : Fragment() {
    private var _binding: FragmentAvailablePacksBinding? = null
    private val binding get() = _binding!!
    private var labId: Int? = 0
    private var labTit: String? = null
    private var labLogo: String? = null
    private var labAddrs: String? = null
    lateinit var packsadapter: AvailPacksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            labId = it.getInt(ARG_PARAM1)
            labTit = it.getString(ARG_PARAM2)
            labLogo = it.getString(ARG_PARAM3)
            labAddrs = it.getString(ARG_PARAM4)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAvailablePacksBinding.inflate(inflater, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.labHead.text = labTit
        binding.labaddress.text = labAddrs
        if(labLogo != null) {
            Picasso.with(context).load(labLogo).fit().centerCrop()
                .into(binding.lablogo)
        }else
        {
            Picasso.with(context).load(R.drawable.no_lab).fit().centerCrop()
                .into(binding.lablogo)
        }
        fetchPacks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchPacks(){
        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getAvailPacks(authTokn, apiTokn,labId)
        requestCall.enqueue(object : Callback<AvailPackResponse> {
            override fun onResponse(call: Call<AvailPackResponse>, response: Response<AvailPackResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    resp.laballpacks?.let{
                        showPacks(it)
                    }
                } else {

                    view?.let{ snackze(it,resp?.message.toString(),binding.availpacksRecycler.id) }
                }
            }
            override fun onFailure(call: Call<AvailPackResponse>, t: Throwable) {
                view?.let{ snackze(it,t.message.toString(),binding.availpacksRecycler.id) }
            }
        })
    }

    private fun showPacks(packlist: ArrayList<PackDetails>) {
        if (!isAdded) return
        val progBar: ProgressBar = binding.progressBar
        progBar.visibility = View.GONE
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.availpacksRecycler.layoutManager = layoutManager
        packsadapter = AvailPacksAdapter(requireContext(),packlist)
        binding.availpacksRecycler.adapter = packsadapter

        binding.packSearch.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                packsadapter.filter.filter(newText)
                return false
            }
        })
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: Int,param2: String,param3: String,param4: String) =
            AvailablePacksFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                    putString(ARG_PARAM4, param4)
                }
            }
    }
}
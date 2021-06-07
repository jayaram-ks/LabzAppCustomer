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
import com.labzapp.customer.adapters.AvailTestsAdapter
import com.labzapp.customer.databinding.FragmentAvailableTestsBinding
import com.labzapp.customer.models.AvailTestResponse
import com.labzapp.customer.models.Laballtests
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

class AvailableTestsFragment : Fragment() {
    private var _binding: FragmentAvailableTestsBinding? = null
    private val binding get() = _binding!!
    private var labId: Int? = 0
    private var labTit: String? = null
    private var labLogo: String? = null
    private var labAddrs: String? = null
    lateinit var testadapter: AvailTestsAdapter

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
        _binding = FragmentAvailableTestsBinding.inflate(inflater, container, false)
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
        fetchTests()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchTests(){
        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getAvailTests(authTokn, apiTokn,labId)
        requestCall.enqueue(object : Callback<AvailTestResponse> {
            override fun onResponse(call: Call<AvailTestResponse>, response: Response<AvailTestResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    resp.laballtests?.let{
                        showTests(it)
                    }
                } else {

                    view?.let{ snackze(it,resp?.message.toString(),binding.availtestsRecycler.id) }
                }
            }
            override fun onFailure(call: Call<AvailTestResponse>, t: Throwable) {
                view?.let{ snackze(it,t.message.toString(),binding.availtestsRecycler.id) }
            }
        })
    }

    private fun showTests(testlist: ArrayList<Laballtests>) {
        if (!isAdded) return
        val progBar: ProgressBar = binding.progressBar
        progBar.visibility = View.GONE
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.availtestsRecycler.layoutManager = layoutManager
        testadapter = AvailTestsAdapter(requireContext(),testlist)
        binding.availtestsRecycler.adapter = testadapter

        binding.testSearch.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                testadapter.filter.filter(newText)
                return false
            }
        })
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: Int,param2: String,param3: String,param4: String) =
            AvailableTestsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                    putString(ARG_PARAM4, param4)
                }
            }
    }
}
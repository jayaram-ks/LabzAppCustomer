package com.labzapp.customer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.labzapp.customer.adapters.MyReportAdapter
import com.labzapp.customer.databinding.FragmentReportsBinding
import com.labzapp.customer.models.MyReportResponse
import com.labzapp.customer.models.Reports
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.gotoHome
import com.labzapp.customer.utilities.snackze
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

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
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchMyReports()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchMyReports(){
        if (!isAdded) return
        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getMyReports(authTokn, apiTokn)
        requestCall.enqueue(object : Callback<MyReportResponse> {
            override fun onResponse(call: Call<MyReportResponse>, response: Response<MyReportResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    resp.reports?.let{
                        showMyReports(it,resp.report_folder_url)
                    }

                } else {
                    view?.let{ snackze(it,resp?.message.toString(),binding.progressBar.id) }
                }
            }

            override fun onFailure(call: Call<MyReportResponse>, t: Throwable) {
                view?.let{ snackze(it,t.message.toString(),binding.progressBar.id) }
            }
        })
    }

    private fun showMyReports(reportlist: ArrayList<Reports>,repUrl:String?)
    {
        if (!isAdded) return
        val progBar: ProgressBar = binding.progressBar
        progBar.visibility = View.GONE
        if(reportlist.isEmpty()){
            view?.let{er -> snackze(er,"No Reports Available.",binding.progressBar.id) }
            return
        }

        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.myreportRecyclerview.layoutManager = layoutManager
        binding.myreportRecyclerview.adapter = MyReportAdapter(requireContext(),binding,reportlist,repUrl)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReportsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
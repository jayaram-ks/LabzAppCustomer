package com.labzapp.customer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.labzapp.customer.R
import com.labzapp.customer.adapters.LabsAdapter
import com.labzapp.customer.adapters.MyBookAdapter
import com.labzapp.customer.databinding.FragmentLabsBinding
import com.labzapp.customer.databinding.FragmentMybookingsBinding
import com.labzapp.customer.models.Bookings
import com.labzapp.customer.models.LabData
import com.labzapp.customer.models.LabsResponse
import com.labzapp.customer.models.MyBookResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.snackze
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MybookingsFragment : Fragment() {

    private var _binding: FragmentMybookingsBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null

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
        _binding = FragmentMybookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchMyBookings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchMyBookings(){

        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken

        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getMyBook(authTokn, apiTokn)
        requestCall.enqueue(object : Callback<MyBookResponse> {
            override fun onResponse(call: Call<MyBookResponse>, response: Response<MyBookResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    resp.bookings?.let{
                        showMyBookings(it)
                    }

                } else {
                    view?.let{ snackze(it,resp?.message.toString(),binding.progressBar.id) }
                }
            }

            override fun onFailure(call: Call<MyBookResponse>, t: Throwable) {
                view?.let{ snackze(it,t.message.toString(),binding.progressBar.id) }
            }
        })
    }

    private fun showMyBookings(booklist: ArrayList<Bookings>)
    {
        if (!isAdded) return
        val progBar: ProgressBar = binding.progressBar
        progBar.visibility = View.GONE
        if(booklist.isEmpty()){
            view?.let{er -> snackze(er,"No Bookings Available.",binding.progressBar.id) }
            return
        }

        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.mybookRecyclerview.layoutManager = layoutManager
        binding.mybookRecyclerview.adapter = MyBookAdapter(requireContext(),booklist)
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MybookingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
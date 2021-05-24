package com.labzapp.customer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.labzapp.customer.adapters.AvailTestsAdapter
import com.labzapp.customer.adapters.LabsAdapter
import com.labzapp.customer.databinding.AvailTestListItemBinding
import com.labzapp.customer.databinding.FragmentAvailableTestsBinding
import com.labzapp.customer.models.AvailTestResponse
import com.labzapp.customer.models.Laballtests
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.toastz
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val ARG_PARAM1 = "lab_id"


class AvailableTestsFragment : Fragment() {
    private var _binding: FragmentAvailableTestsBinding? = null
    private val binding get() = _binding!!
    private var labId: Int? = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            labId = it.getInt(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentAvailableTestsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
                    activity?.let { toastz(it,resp?.message.toString()) }
                }
            }

            override fun onFailure(call: Call<AvailTestResponse>, t: Throwable) {
                activity?.let { toastz(it,t.message.toString()) }
            }
        })
    }

    private fun showTests(testlist: List<Laballtests>)
    {
        if (!isAdded) return
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.availtestsRecycler.layoutManager = layoutManager
        binding.availtestsRecycler.adapter = AvailTestsAdapter(requireContext(),testlist)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AvailableTestsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int) =
            AvailableTestsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }
}
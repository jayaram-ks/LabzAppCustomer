package com.labzapp.customer.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.labzapp.customer.R
import com.labzapp.customer.activities.BookingActivity
import com.labzapp.customer.databinding.FragmentPackBannerDialogBinding
import com.labzapp.customer.models.SinglePackResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.snackze
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val ARG_PARAM1 = "param1"



class PackBannerDialogFragment : DialogFragment() {

    private var _binding: FragmentPackBannerDialogBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)

        }
    }

    /** The system calls this to get the DialogFragment's layout, regardless
    of whether it's being displayed as a dialog or an embedded fragment. */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPackBannerDialogBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchPackageDetails()
        binding.closedialg.setOnClickListener {
            dialog?.dismiss()
        }
        binding.bookapack.setOnClickListener{
            val intent = Intent(requireActivity(), BookingActivity::class.java)
            intent.putExtra("pack_or_test", "2") //Package booking
            intent.putExtra("pack_id",param1)
            if(param1 != null) {
                startActivity(intent)
            }
        }
    }

    private fun fetchPackageDetails() {

        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getSinglePackDetail(authTokn, apiTokn,param1)
        requestCall.enqueue(object : Callback<SinglePackResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<SinglePackResponse>, response: Response<SinglePackResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    resp.packdetails.let {
                        if (!isAdded) return
                        val rupee = requireContext().getString(R.string.rupee)
                        binding.packTitle.text = it.pack_name
                        binding.packDetails.text = "DESCRIPTION : "+ it.pack_desc
                        binding.packPrecaution.text = "PRECAUTIONS : "+ it.pack_precautions
                        binding.testsDetails.text = it.test_count +" TESTS INCLUDED : "+ it.pack_tests
                        binding.labName.text = "LAB NAME : "+ it.lab_name
                        binding.labAddress.text = "LAB ADDRESS : "+ it.lab_address
                        binding.packRate1.text = "Package Price : " +rupee+" "+it.rate_final
                        binding.packRate2.text =  " Actual Price : " +rupee+" "+it.rate_initial
                        binding.packRate2.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                        if( it.pack_image != "") {
                           Picasso.with(context).load(it.pack_image).fit().into(binding.packImg)
                        }
                        else
                        {
                           binding.packImg.visibility = View.GONE
                        }
                    }



                } else {
                    view?.let{ snackze(it,resp?.message.toString(),binding.packDetails.id) }
                }
            }

            override fun onFailure(call: Call<SinglePackResponse>, t: Throwable) {
                view?.let{ snackze(it,t.message.toString(),binding.packDetails.id) }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            PackBannerDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)


            }
        }
    }
}
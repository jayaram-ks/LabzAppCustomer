package com.labzapp.customer.fragments

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.labzapp.customer.R
import com.labzapp.customer.adapters.TestRateAdapter
import com.labzapp.customer.databinding.FragmentBookingPackPreviewBinding
import com.labzapp.customer.databinding.FragmentBookingPreviewBinding
import com.labzapp.customer.models.*
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.snackze
import com.squareup.picasso.Picasso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BookingPackPreviewFragment : Fragment() {

    private var _binding: FragmentBookingPackPreviewBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null

    private var patbookfor: String? = null
    private var patlat: String? = null
    private var patlong: String? = null
    private var patprefdate: String? = null
    private var packID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        val bookingDataModel: BookingDataPackTransfer = param1?.let { Json.decodeFromString(it) }!!

        patbookfor =  bookingDataModel.patbookfor
        patlat = bookingDataModel.patlatitude
        patlong = bookingDataModel.patlongitude
        patprefdate = bookingDataModel.patprefdate
        packID = bookingDataModel.packid
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookingPackPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchPackageDetails()

        val datFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dat =  LocalDate.parse(patprefdate , datFormat)
        val samplecollDate = dat.dayOfMonth.toString() +" "+dat.month.toString()+" "+dat.year.toString()
        binding.samplePrefDate.text = "Preferred date : "+ samplecollDate

        binding.continuePreviewBtn.setOnClickListener {
            if(patbookfor == "1")   //booking for self
            {
                binding.continuePreviewBtn.setOnClickListener(null)
                binding.continuePreviewBtn.visibility = View.GONE
                fetchAndsubmitMyData()

            } else {   //Booking for Other Show form
                val bundle = Bundle()
                bundle.putString("param1", param1)
                val patdataFragment = PatientDataPackFragment()
                patdataFragment.arguments = bundle
                val transPat = childFragmentManager.beginTransaction()
                patdataFragment.show(transPat,PatientDataPackFragment.TAG)
            }
        }

    }


    private fun fetchAndsubmitMyData(){
        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getProfile(authTokn, apiTokn)
        requestCall.enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    resp.customer.let {
                        val userName = it.name
                        val userAge = it.age.toString()
                        val userPhone = it.phone
                        val userGender = it.gender.toString()
                        val userAddress = it.address
                        val userPincode = it.pincode.toString()
                        val userDistrict = it.district.toString()

                        if ((userName != "") and (userAge != "") and (userPhone != "") and (userGender != "")
                            and (userAddress != "") and (userPincode != "") and (userDistrict != "")) {

                            val apibookService = ServiceBuilder.buildService(ApiService::class.java)
                            val bookrequestCall = apibookService.submitPackBooking(authTokn,apiTokn,userName,userPhone,userAge,userGender,userAddress,
                                userPincode,patlat,patlong,userDistrict,packID,"1",patprefdate)
                            bookrequestCall.enqueue(object : Callback<SubmitBookingResponse> {
                                override fun onResponse(call: Call<SubmitBookingResponse>, response: Response<SubmitBookingResponse>) {
                                    val resp = response.body()
                                    if (resp?.code == 200) {

                                        val bundle = Bundle()
                                        bundle.putString("param1", resp.message.toString())
                                        val fragmentbookFinish = BookFinishDialogFragment()
                                        fragmentbookFinish.arguments = bundle
                                        fragmentbookFinish.isCancelable = false
                                        val transFinish = childFragmentManager.beginTransaction()
                                        fragmentbookFinish.show(transFinish,BookFinishDialogFragment.TAG)

                                    } else {
                                        view?.let{err -> snackze(err,resp?.message.toString(),binding.totalTestCharge.id) }
                                    }
                                }

                                override fun onFailure(call: Call<SubmitBookingResponse>, t: Throwable) {
                                    view?.let{err -> snackze(err,t.message.toString(),binding.totalTestCharge.id) }
                                }
                            })
                        }
                        else
                        {

                            view?.let{err -> snackze(err,"There was an error processing your request",binding.totalTestCharge.id) }
                        }
                    }

                } else {
                    view?.let{errz -> snackze(errz,resp?.message.toString(),binding.totalTestCharge.id) }
                }
            }
            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                view?.let{snackze(it,t.message.toString(),binding.totalTestCharge.id) }
            }
        })
    }

    private fun fetchPackageDetails() {

        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getSinglePackDetail(authTokn, apiTokn,packID)
        requestCall.enqueue(object : Callback<SinglePackResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<SinglePackResponse>, response: Response<SinglePackResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    resp.packdetails.let {
                        if (!isAdded) return
                        val rupee = requireContext().getString(R.string.rupee)
                        binding.packTitle.text ="Selected Package : " +it.pack_name
                        binding.testsDetails.text = it.test_count +" Tests : "+ it.pack_tests
                        binding.labName.text = "Lab Name : "+ it.lab_name
                        binding.labAddress.text = "Lab Address : "+ it.lab_address
                        binding.packRate1.text = "Package Price : " +rupee+" "+it.rate_final
                        binding.packRate2.text =  rupee+" "+it.rate_initial
                        binding.packRate2.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                        val servcharge = it.service_charge?.toFloat()
                        val packtotal = it.rate_final?.toFloat()
                        val grandtot = packtotal?.let { it1 -> servcharge?.plus(it1) }
                        binding.totalTestCharge.text = requireContext().getString(R.string.rupee)+" "+packtotal.toString()
                        binding.labServiceCharge.text = requireContext().getString(R.string.rupee)+" "+servcharge.toString()
                        binding.grandTotal.text = requireContext().getString(R.string.rupee)+" "+grandtot.toString()

                        if( it.pack_image != "") {
                            Picasso.with(context).load(it.pack_image).fit().into(binding.packImg)
                        }
                        else
                        {
                            binding.packImg.visibility = View.GONE
                        }
                    }



                } else {
                    view?.let{ snackze(it,resp?.message.toString(),binding.totalTestCharge.id) }
                }
            }

            override fun onFailure(call: Call<SinglePackResponse>, t: Throwable) {
                view?.let{ snackze(it,t.message.toString(),binding.totalTestCharge.id) }
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BookingPackPreviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "BookingPackPreviewFragment"
    }
}
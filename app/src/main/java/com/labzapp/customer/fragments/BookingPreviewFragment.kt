package com.labzapp.customer.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.labzapp.customer.R
import com.labzapp.customer.adapters.LabsAdapter
import com.labzapp.customer.adapters.TestRateAdapter
import com.labzapp.customer.databinding.FragmentBookingPreviewBinding
import com.labzapp.customer.models.*
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.districtz
import com.labzapp.customer.utilities.genderz
import com.labzapp.customer.utilities.toastz
import com.labzapp.customer.utilities.toastzs
import com.squareup.picasso.Picasso
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class BookingPreviewFragment : Fragment() {

    private var _binding: FragmentBookingPreviewBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var pattests: MutableList<String>
    private lateinit var selectlabdata:Labswithtest
    private var patbookfor: String? = null
    private var patlat: String? = null
    private var patlong: String? = null
    private var patprefdate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        val bookingDataModel: BookingDataTransfer = param1?.let { Json.decodeFromString(it) }!!

        patbookfor =  bookingDataModel.patbookfor
        patlat = bookingDataModel.patlatitude
        patlong = bookingDataModel.patlongitude
        patprefdate = bookingDataModel.patprefdate




        pattests = bookingDataModel.pattests
        val patlab:ArrayList<Labswithtest> = bookingDataModel.patlab
        selectlabdata = patlab[0]

      // Log.d("JKSSS", pattests[0])
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookingPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchTestRates()
        binding.selectLabTitle.text = selectlabdata.name
        binding.selectedLabAddress.text = selectlabdata.address

        if(selectlabdata.thumbnail != null) {
            Picasso.with(context).load(selectlabdata.thumbnail).fit().centerCrop()
                .into(binding.selLabLogo)
        }else
        {
            Picasso.with(context).load(R.drawable.no_lab).fit().centerCrop()
                .into(binding.selLabLogo)
        }

        binding.totalTestCharge.text = requireContext().getString(R.string.rupee)+" "+selectlabdata.test_amount.toString()
        binding.labServiceCharge.text = requireContext().getString(R.string.rupee)+" "+selectlabdata.service_charge.toString()
        binding.grandTotal.text = requireContext().getString(R.string.rupee)+" "+selectlabdata.total_to_pay.toString()

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
                val patdataFragment = PatientDataFragment()
                patdataFragment.arguments = bundle
                val transPat = childFragmentManager.beginTransaction()
                patdataFragment.show(transPat,PatientDataFragment.TAG)
            }
        }

    }
    private fun fetchTestRates(){
        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getTestRatesforLab(authTokn, apiTokn,selectlabdata.lab_id.toString(),pattests)

        requestCall.enqueue(object : Callback<BookingTestRatesResponse> {
            override fun onResponse(call: Call<BookingTestRatesResponse>, response: Response<BookingTestRatesResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    showTestsWithRates(resp.labtestsrates)
                } else {
                    activity?.let { toastz(it,resp?.message.toString()) }
                }
            }
            override fun onFailure(call: Call<BookingTestRatesResponse>, t: Throwable) {
                activity?.let { toastz(it, t.message.toString()) }
            }
        })
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
                                    val bookrequestCall = apibookService.submitBooking(authTokn, apiTokn,userName,userPhone,userAge,userGender,userAddress,
                                        userPincode,patlat,patlong,userDistrict,pattests,selectlabdata.lab_id.toString(),"1",patprefdate)
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
                                                activity?.let {err -> toastz(err,resp?.message.toString()) }
                                            }
                                        }

                                        override fun onFailure(call: Call<SubmitBookingResponse>, t: Throwable) {
                                            activity?.let { err-> toastz(err,t.message.toString()) }
                                        }
                                    })
                        }
                        else
                        {
                            activity?.let { err -> toastz(err,"There was an error processing your request") }
                        }
                    }

                } else {
                    activity?.let { toastz(it,resp?.message.toString()) }
                }
            }
            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                activity?.let { toastz(it,t.message.toString()) }
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun  showTestsWithRates(testrates:ArrayList<Labtestsrates>){
        if (!isAdded) return
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.previewTestsRecycler.layoutManager = layoutManager
        binding.previewTestsRecycler.adapter = TestRateAdapter(requireContext(),testrates)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BookingPreviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "BookingPreviewFragment"
    }
}
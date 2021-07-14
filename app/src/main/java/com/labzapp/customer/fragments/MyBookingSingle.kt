package com.labzapp.customer.fragments

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.labzapp.customer.R
import com.labzapp.customer.adapters.BookTestRateAdapter
import com.labzapp.customer.databinding.FragmentMyBookingSingleBinding
import com.labzapp.customer.models.MyBookSingleResponse
import com.labzapp.customer.models.SinglePackResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.districtz
import com.labzapp.customer.utilities.genderz
import com.labzapp.customer.utilities.snackze
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.floor
import kotlin.math.round


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MyBookingSingle : Fragment() {

    private var _binding: FragmentMyBookingSingleBinding? = null
    private val binding get() = _binding!!
    private var param1: String? = null
    private var param2: String? = null
    private var packOrTest:String? = null
    private var packID:String? = null

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
        _binding = FragmentMyBookingSingleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchBookingDetails()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun fetchBookingDetails(){

        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken

        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getMyBookDetails(authTokn, apiTokn,param1)
        requestCall.enqueue(object : Callback<MyBookSingleResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<MyBookSingleResponse>, response: Response<MyBookSingleResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    if (!isAdded) return
                    val progBar: ProgressBar = binding.progressBar
                    val rupee = context?.getString(R.string.rupee) + " "
                    progBar.visibility = View.GONE

                    resp.booking.let{
                        packOrTest = it.test_or_pack
                        packID = it.package_id
                        binding.patName.text = "Name : "+it.patient_name
                        binding.patAddress.text = "Address : "+it.patient_address
                        binding.patAgeGender.text = "Age : "+it.age +"  Gender : " + genderz[it.gender.toInt()]
                        binding.patPincodeDistrict.text = "Pincode : "+ it.patient_pincode + " District : " + districtz[it.patient_district.toInt()]
                        binding.patPhone.text = "Phone : "+it.mobile

                        val datFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                        val datb =  LocalDate.parse(it.booking_date , datFormat)
                        val bookdate = datb.dayOfMonth.toString() +" "+datb.month.toString()+" "+datb.year.toString()
                        if(it.pref_date != null) {
                            val datp = LocalDate.parse(it.pref_date, datFormat)
                            val prefdate = datp.dayOfMonth.toString() + " " + datp.month.toString() + " " + datp.year.toString()
                            binding.sampleCollDate.text = "Sample Collection Date : $prefdate"
                        }
                        binding.bookId.text = "Booking ID : "+it.id.toString()
                        binding.bookingDate.text = "Booking Date : $bookdate"

                        binding.labAddress.text = "Address : " + it.lab_address
                        binding.labName.text ="Name : " + it.lab_name
                        binding.labPinDistrict.text = "Pincode : "+ it.lab_pincode

                        binding.testTotal.text = rupee +  it.booking_total
                        binding.serviceCharges.text = rupee +  it.service_charge.toFloat().toString()
                        binding.grandTotal.text = rupee + it.grand_total.toFloat().toString()

                    }

                    if(packOrTest == "2"){ //Package Book
                        binding.packDetCard.visibility = View.VISIBLE
                        binding.testDetCard.visibility = View.GONE
                        binding.totalText.text = "Package total"
                        packID?.let { fetchPackageDetails(it) }
                    }
                    else{  //Tests Book
                        binding.packDetCard.visibility = View.GONE
                        binding.testDetCard.visibility = View.VISIBLE
                        binding.totalText.text = "Tests total"
                        resp.tests.let{
                            val layoutManager = LinearLayoutManager(activity)
                            layoutManager.orientation = LinearLayoutManager.VERTICAL
                            binding.testListView.layoutManager = layoutManager
                            binding.testListView.adapter = BookTestRateAdapter(requireContext(),it)
                        }
                    }




                } else {
                    view?.let{ snackze(it,resp?.message.toString(),binding.progressBar.id) }
                }
            }

            override fun onFailure(call: Call<MyBookSingleResponse>, t: Throwable) {
                view?.let{ snackze(it,t.message.toString(),binding.progressBar.id) }
            }
        })
    }

    private fun fetchPackageDetails(packID:String) {

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
                        binding.packTitle.text ="Package : " +it.pack_name
                        binding.testsDetails.text = it.test_count +" Tests : "+ it.pack_tests

                        if( it.pack_image != "") {
                            Picasso.with(context).load(it.pack_image).fit().into(binding.packImg)
                        }
                        else
                        {
                            binding.packImg.visibility = View.GONE
                        }
                    }



                } else {
                    view?.let{ snackze(it,resp?.message.toString(),binding.labName.id) }
                }
            }

            override fun onFailure(call: Call<SinglePackResponse>, t: Throwable) {
                view?.let{ snackze(it,t.message.toString(),binding.labName.id) }
            }
        })
    }



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyBookingSingle().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}




package com.labzapp.customer.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import com.labzapp.customer.R
import com.labzapp.customer.databinding.FragmentPatientDataBinding
import com.labzapp.customer.models.BookingDataTransfer
import com.labzapp.customer.models.Labswithtest
import com.labzapp.customer.models.SubmitBookingResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.districtz
import com.labzapp.customer.utilities.snackze
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PatientDataFragment : DialogFragment(), AdapterView.OnItemSelectedListener {

    private var _binding: FragmentPatientDataBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null

    private var districtid = 0
    private var custGender: Int = 1

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPatientDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val values: ArrayList<String> = ArrayList(districtz.values)
        val adapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner = binding.cdistrict
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
        binding.radio1.setOnClickListener{ view -> onRadioButtonClicked(view) }
        binding.radio2.setOnClickListener { view -> onRadioButtonClicked(view) }
        binding.submitOthrBookingBtn.setOnClickListener {
            binding.submitOthrBookingBtn.isClickable = false
            fetchAndsubmitMyData()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        //  parent.getItemAtPosition(pos)
        val keysz: ArrayList<Int> = ArrayList(districtz.keys)
        districtid =  keysz[pos]

    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    private fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio1 ->
                    if (checked) {
                        custGender = 1
                    }
                R.id.radio2 ->
                    if (checked) {
                        custGender = 2
                    }
            }
        }
    }


    private fun fetchAndsubmitMyData(){

        val userName = binding.patName.text.toString()
        val userAge = binding.patAge.text.toString()
        val userPhone = binding.patMobile.text.toString()
        val userAddress = binding.patAddress.text.toString()
        val userPincode = binding.patPincode.text.toString()
        val userGender = custGender.toString()
        val userDistrict = districtid.toString()


        if ((userName != "") and (userAge != "") and (userPhone != "") and (userGender != "")
            and (userAddress != "") and (userPincode != "") and (userDistrict != "")) {

            val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
            val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken

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

                        view?.let{err -> snackze(err,resp?.message.toString(),binding.patAddress.id) }
                        binding.submitOthrBookingBtn.isClickable = true
                    }
                }

                override fun onFailure(call: Call<SubmitBookingResponse>, t: Throwable) {
                    view?.let{err -> snackze(err,t.message.toString(),binding.patAddress.id) }
                }
            })
        }
        else
        {
            view?.let{err -> snackze(err,"Please fill all Patient Details Fields",binding.patAddress.id) }
            binding.submitOthrBookingBtn.isClickable = true
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PatientDataFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "PatientDataFragment"
    }
}
package com.labzapp.customer.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.labzapp.customer.adapters.BookLabsAdapter
import com.labzapp.customer.adapters.MyLabsLookup
import com.labzapp.customer.databinding.FragmentBookingLabsBinding
import com.labzapp.customer.models.BookingLabsResponse
import com.labzapp.customer.models.Labswithtest
import com.labzapp.customer.models.Tests
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.toastz
import com.labzapp.customer.utilities.toastzs
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"

class BookingLabsFragment : DialogFragment() {

    private var _binding: FragmentBookingLabsBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null
    private var param3: String? = null
    private var param4: String? = null

    private var patientLat : Double = 0.00
    private var patientLong : Double = 0.00
    private var testArr: MutableList<String> = ArrayList()
    private lateinit var labadapter: BookLabsAdapter
    private var ltracker: SelectionTracker<Long>? = null
    val posArr:MutableList<Long> = ArrayList()
    var labmodel:ArrayList<Labswithtest> = ArrayList()
    private lateinit var oldLabspos: ArrayList<Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            param3 = it.getString(ARG_PARAM3)
            param4 = it.getString(ARG_PARAM4)
        }
        testArr = param1?.let { Json.decodeFromString(it) }!!
        patientLat = param2?.toDouble() ?: 0.00
        patientLong = param3?.toDouble() ?:0.00

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookingLabsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** The system calls this only when creating the layout in a dialog. */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchMatchingLabs(testArr)

        binding.labzContinue.setOnClickListener{
            dialog?.dismiss()

            val selPositions = Json.encodeToString(posArr)

           var labarray: ArrayList<Labswithtest> = ArrayList()

            for( (index, row) in labmodel.withIndex()){
                for(t in posArr){
                    if(index.toLong() == t){
                        labarray.add(Labswithtest(row.lab_id,row.name,row.address,row.thumbnail,row.test_amount,row.service_charge,row.total_to_pay))
                    }
                }

            }

            val labString: String = Json.encodeToString(labarray)

            setFragmentResult("labKey", bundleOf("selpos" to selPositions, "sel_labs" to labString ))

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

    private fun fetchMatchingLabs(selectedTests: MutableList<String>){
        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getLabsHavingTests(authTokn, apiTokn,patientLat,patientLong,selectedTests)

        requestCall.enqueue(object : Callback<BookingLabsResponse> {
            override fun onResponse(call: Call<BookingLabsResponse>, response: Response<BookingLabsResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    showLabs(resp.labswithtest)
                    labmodel = resp.labswithtest
                } else {
                    activity?.let { toastz(it,resp?.message.toString()) }
                }
            }
            override fun onFailure(call: Call<BookingLabsResponse>, t: Throwable) {
                activity?.let { toastz(it, t.message.toString()) }
            }
        })
    }

    private fun showLabs(lablist: ArrayList<Labswithtest>) {
        if (!isAdded) return

        if(lablist.size < 1 ){
            context?.let { toastz(it,"No labs available based on your tests selection and location") }
            return
        }

        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.selectLabsRecycler.layoutManager = layoutManager
        labadapter = BookLabsAdapter(requireContext(),lablist)
        binding.selectLabsRecycler.adapter = labadapter

        ltracker = SelectionTracker.Builder<Long>(
            "blabz",
            binding.selectLabsRecycler,
            StableIdKeyProvider( binding.selectLabsRecycler),
            MyLabsLookup( binding.selectLabsRecycler),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectSingleAnything()
        ).build()

        labadapter.setTracker(ltracker)

        ltracker?.addObserver(
            object: SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    val nItems:Int? = ltracker?.selection?.size()

                    Log.d("labs--Tracker---selcted",ltracker?.selection.toString())

                    for( (index, row) in lablist.withIndex()){
                        if(ltracker!!.isSelected(index.toLong())){
                            if(index.toLong() !in posArr) {
                                posArr.add(index.toLong())
                            }
                        } else{
                            posArr.remove(index.toLong())
                        }
                    }

                    if(nItems!=null && nItems > 0) {


                    } else {

                    }
                }
            })

        if(param4 != null) {
            oldLabspos = param4?.let { Json.decodeFromString(it) }!!
            ltracker?.setItemsSelected(oldLabspos,true)
        }

        for( (index, row) in lablist.withIndex()){
            if(ltracker!!.isSelected(index.toLong())){
                if(index.toLong() !in posArr) {
                    posArr.add(index.toLong())
                }
            } else{
                posArr.remove(index.toLong())
            }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String,param3: String,param4: String) =
            BookingLabsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                    putString(ARG_PARAM4, param4)
                }
            }
        const val TAG = "BookingLabsFragment"
    }
}
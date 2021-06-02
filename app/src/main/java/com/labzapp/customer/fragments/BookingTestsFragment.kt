package com.labzapp.customer.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.labzapp.customer.adapters.AllTestsAdapter
import com.labzapp.customer.adapters.MyLookup
import com.labzapp.customer.databinding.FragmentBookingTestsBinding
import com.labzapp.customer.models.BookingTestsResponse
import com.labzapp.customer.models.Tests
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.toastz
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


private const val ARG_PARAM1 = "selcted_tests_pos"

class BookingTestsFragment : DialogFragment() {
    private var _binding: FragmentBookingTestsBinding? = null
    private val binding get() = _binding!!
    var selectedTests:String? = "No tests selected."
    private lateinit var oldTestsPos: ArrayList<Long>
    private lateinit var testadapter: AllTestsAdapter
    private var tracker: SelectionTracker<Long>? = null
    private var selcTsts: String? = null
    val posArr:MutableList<Long> = ArrayList()
    var testmodel:ArrayList<Tests> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selcTsts = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookingTestsBinding.inflate(inflater, container, false)
        return binding.root

    }

    /** The system calls this only when creating the layout in a dialog. */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchTests()
        binding.testContinue.setOnClickListener{
            dialog?.dismiss()
            val selPositions = Json.encodeToString(posArr)

            var idarray: MutableList<String> = ArrayList()
            for( (index, row) in testmodel.withIndex()){
                for(t in posArr){
                    if(index.toLong() == t){
                        idarray.add(row.id.toString())
                    }
                }

            }

            val iDstring: String = Json.encodeToString(idarray)

            setFragmentResult("testKey", bundleOf("sel_pos" to selPositions, "sel_test_ids" to iDstring ))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchTests(){
        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(requireContext()).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(requireContext()).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.getAllTests(authTokn, apiTokn)
        requestCall.enqueue(object : Callback<BookingTestsResponse> {
            override fun onResponse(call: Call<BookingTestsResponse>, response: Response<BookingTestsResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    resp.tests.let{
                        showTests(it)
                        val testsToSrch = it
                        testmodel = it
                        binding.srchTxt.addTextChangedListener{
                            val positn = performFiltering(binding.srchTxt.text,testsToSrch)
                            binding.alltestsRecycler.scrollToPosition(positn)
                        }
                    }
                } else {
                    activity?.let { toastz(it,resp?.message.toString()) }
                }
            }
            override fun onFailure(call: Call<BookingTestsResponse>, t: Throwable) {
                activity?.let { toastz(it,t.message.toString()) }
            }
        })
    }


    private fun showTests(testlist: ArrayList<Tests>) {
        if (!isAdded) return
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.alltestsRecycler.layoutManager = layoutManager
        testadapter = AllTestsAdapter(requireContext(),testlist)
        binding.alltestsRecycler.adapter = testadapter

        tracker = SelectionTracker.Builder<Long>(
            "btestz",
            binding.alltestsRecycler,
            StableIdKeyProvider( binding.alltestsRecycler),
            MyLookup( binding.alltestsRecycler),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        testadapter.setTracker(tracker)

        tracker?.addObserver(
            object: SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    val nItems:Int? = tracker?.selection?.size()


Log.d("Tracker--------POST",tracker?.selection.toString())

                    for( (index, row) in testlist.withIndex()){
                        if(tracker!!.isSelected(index.toLong())){
                            if(index.toLong() !in posArr) {
                                posArr.add(index.toLong())
                            }
                        } else{
                            posArr.remove(index.toLong())
                        }
                    }

                    if(nItems!=null && nItems > 0) {
                        selectedTests = "$nItems tests selected"
                       binding.testNum.text = selectedTests

                    } else {
                        selectedTests = "No tests selected."
                        binding.testNum.text = selectedTests
                    }
                }
            })



            if(selcTsts != null) {
                oldTestsPos = selcTsts?.let { Json.decodeFromString(it) }!!
                tracker?.setItemsSelected(oldTestsPos,true)
            }

        for( (index, row) in testlist.withIndex()){
            if(tracker!!.isSelected(index.toLong())){
                if(index.toLong() !in posArr) {
                    posArr.add(index.toLong())
                }
            } else{
                posArr.remove(index.toLong())
            }
        }



    }

    fun performFiltering(constraint: CharSequence?,tests: ArrayList<Tests>):Int{
        var requiredIndex = 0
        val charSearch = constraint.toString()
        if (charSearch.isEmpty()) {
        } else {
            for( (index, row) in tests.withIndex()){
                // Log.d("--jk----",row.test_name)
                if (row.test_name?.toLowerCase(Locale.ROOT)?.contains(charSearch.toLowerCase(Locale.ROOT)) == true) {
                    requiredIndex = index
                }
            }
        }
        return requiredIndex

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1:String) =
            BookingTestsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
        const val TAG = "BookingTestsFragment"
    }

}
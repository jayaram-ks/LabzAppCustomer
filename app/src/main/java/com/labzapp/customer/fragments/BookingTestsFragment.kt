package com.labzapp.customer.fragments

import android.app.Dialog
import android.os.Bundle
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class BookingTestsFragment : DialogFragment() {
    private var _binding: FragmentBookingTestsBinding? = null
    private val binding get() = _binding!!
    var selectedTests:String? = "No tests selected."
    lateinit var testadapter: AllTestsAdapter
    private var tracker: SelectionTracker<Long>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

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
            var selIDS = Json.encodeToString(testadapter.selctedIdList)
            setFragmentResult("testKey", bundleOf("sel_tests" to selectedTests,"sel_ids" to selIDS ))
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
                    resp.tests?.let{
                        showTests(it)
                        val testsToSrch = it
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

                    if(nItems!=null && nItems > 0) {
                        selectedTests = "$nItems tests selected"
                       binding.testNum.text = selectedTests

                    } else {
                        selectedTests = "No tests selected."
                        binding.testNum.text = selectedTests
                    }
                }
            })

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
        fun newInstance() =
            BookingTestsFragment().apply {
                arguments = Bundle().apply {
                }
            }
        const val TAG = "BookingTestsFragment"
    }
}
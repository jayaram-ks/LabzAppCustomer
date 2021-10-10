package com.labzapp.customer.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.labzapp.customer.R
import com.labzapp.customer.activities.BookingActivity
import com.labzapp.customer.adapters.AvailTestsAdapter
import com.labzapp.customer.adapters.MyLookupLabTest
import com.labzapp.customer.databinding.FragmentAvailableTestsBinding
import com.labzapp.customer.models.AvailTestResponse
import com.labzapp.customer.models.Laballtests
import com.labzapp.customer.models.Labswithtest
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.snackze
import com.squareup.picasso.Picasso
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


private const val ARG_PARAM1 = "lab_id"
private const val ARG_PARAM2 = "lab_title"
private const val ARG_PARAM3 = "lab_logo"
private const val ARG_PARAM4 = "lab_address"
private const val ARG_PARAM5 = "serv_charg"

class AvailableTestsFragment : Fragment() {
    private var _binding: FragmentAvailableTestsBinding? = null
    private val binding get() = _binding!!
    private var labId: Int? = 0
    private var labTit: String? = null
    private var labLogo: String? = null
    private var labAddrs: String? = null
    private var servChg: String? = null
    lateinit var testadapter: AvailTestsAdapter

    private var tracker: SelectionTracker<Long>? = null
    val posArr:MutableList<Long> = ArrayList()
    var selectedTests:String? = "No tests selected."
    var testmodel:ArrayList<Laballtests> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            labId = it.getInt(ARG_PARAM1)
            labTit = it.getString(ARG_PARAM2)
            labLogo = it.getString(ARG_PARAM3)
            labAddrs = it.getString(ARG_PARAM4)
            servChg = it.getString(ARG_PARAM5)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAvailableTestsBinding.inflate(inflater, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.labHead.text = labTit
        binding.labaddress.text = labAddrs
        if(labLogo != null) {
            Picasso.with(context).load(labLogo).fit().centerCrop()
                .into(binding.lablogo)
        }else
        {
            Picasso.with(context).load(R.drawable.no_lab).fit().centerCrop()
                .into(binding.lablogo)
        }
        fetchTests()

        binding.testLabSubmit.setOnClickListener{

            var idarray: MutableList<String> = ArrayList()
            for( (index, row) in testmodel.withIndex()){
                for(t in posArr){
                    if(index.toLong() == t){
                        idarray.add(row.testid.toString())
                    }
                }

            }

            val iDstring: String = Json.encodeToString(idarray)

            var labarray: ArrayList<Labswithtest> = ArrayList()
            val selabID :Long = labId?.toLong() ?: 0
            labarray.add(Labswithtest(selabID,labTit.toString(),labAddrs.toString(),labLogo,"0",servChg.toString(),"0"))
            val labString: String = Json.encodeToString(labarray)

            val intent = Intent(requireActivity(), BookingActivity::class.java)
            intent.putExtra("testidsFromLab", iDstring) //Test booking from labpage
            intent.putExtra("labDataString",labString)
            intent.putExtra("isBookFromLab","yes")
            startActivity(intent)

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
        val requestCall = apiService.getAvailTests(authTokn, apiTokn,labId)
        requestCall.enqueue(object : Callback<AvailTestResponse> {
            override fun onResponse(call: Call<AvailTestResponse>, response: Response<AvailTestResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                    resp.laballtests?.let{
                        showTests(it)
                        val testsToSrch = it
                        testmodel = it
                        binding.srchTxt.addTextChangedListener{
                            val positn = performFiltering(binding.srchTxt.text,testsToSrch)
                            binding.availtestsRecycler.scrollToPosition(positn)
                        }
                    }
                } else {

                    view?.let{ snackze(it,resp?.message.toString(),binding.availtestsRecycler.id) }
                }
            }
            override fun onFailure(call: Call<AvailTestResponse>, t: Throwable) {
                view?.let{ snackze(it,t.message.toString(),binding.availtestsRecycler.id) }
            }
        })
    }

    private fun showTests(testlist: ArrayList<Laballtests>) {
        if (!isAdded) return
        val progBar: ProgressBar = binding.progressBar
        progBar.visibility = View.GONE
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.availtestsRecycler.layoutManager = layoutManager
        testadapter = AvailTestsAdapter(requireContext(),testlist)
        binding.availtestsRecycler.adapter = testadapter


        tracker = SelectionTracker.Builder<Long>(
            "btestz",
            binding.availtestsRecycler,
            StableIdKeyProvider( binding.availtestsRecycler),
            MyLookupLabTest( binding.availtestsRecycler),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        testadapter.setTracker(tracker)


        tracker?.addObserver(
            object: SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    val nItems:Int? = tracker?.selection?.size()


//Log.d("Tracker--------POST",tracker?.selection.toString())

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
                        binding.testLabSubmit.visibility = View.VISIBLE

                    } else {
                        selectedTests = "Select Tests"
                        binding.testNum.text = selectedTests
                        binding.testLabSubmit.visibility = View.GONE
                    }
                }
            })

    }

    fun performFiltering(constraint: CharSequence?,tests: ArrayList<Laballtests>):Int{
        var requiredIndex = 0
        val charSearch = constraint.toString()
        if (charSearch.isEmpty()) {
        } else {
            for( (index, row) in tests.withIndex()){
                // Log.d("--jk----",row.test_name)
                if (row.test_name?.lowercase(Locale.ROOT)?.contains(charSearch.lowercase(Locale.ROOT)) == true) {
                    requiredIndex = index
                }
            }
        }
        return requiredIndex

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: Int,param2: String,param3: String,param4: String,param5:String) =
            AvailableTestsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                    putString(ARG_PARAM4, param4)
                    putString(ARG_PARAM4, param5)
                }
            }
    }
}
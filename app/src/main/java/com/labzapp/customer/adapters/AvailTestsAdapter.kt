package com.labzapp.customer.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.labzapp.customer.R
import com.labzapp.customer.databinding.AvailTestListItemBinding
import com.labzapp.customer.fragments.TestDialogFragment
import com.labzapp.customer.models.Laballtests
import java.util.*
import kotlin.collections.ArrayList

class AvailTestsAdapter( val context: Context,private val tests: ArrayList<Laballtests>) : RecyclerView.Adapter<AvailTestsAdapter.TestsViewHolder>() ,Filterable{

    var testFilterList = ArrayList<Laballtests>()

    init {
        testFilterList = tests
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestsViewHolder {
        val binding = AvailTestListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TestsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return testFilterList.size
    }

    override fun onBindViewHolder(holder: TestsViewHolder, position: Int) {
        val testp = testFilterList[position]
        holder.setData(testp, position)
    }

    inner class TestsViewHolder(private val binding: AvailTestListItemBinding) : RecyclerView.ViewHolder(binding.root){

        var currenTest: Laballtests? = null
        var currentPosition: Int = 0

        init {
            itemView.setOnClickListener {
                currenTest?.let {
                    val bundle = Bundle()
                    bundle.putString("test_title", it.test_name.toString())
                    bundle.putString("test_description", it.test_description.toString())
                    bundle.putString("test_recomm", it.test_recommendation.toString() +"\n"+ it.test_recommendation2.toString())
                    val appCompatActivity = context as AppCompatActivity
                    val fragmentManager =  appCompatActivity.supportFragmentManager
                    val dialgFragment = TestDialogFragment()
                    dialgFragment.arguments = bundle
                    dialgFragment.show(fragmentManager,null)
                }
            }

        }

        fun setData(test: Laballtests?, pos: Int) {
            test?.let {
                binding.tstTitle.text = it.test_name.toString()
                binding.tstDetails.text = it.test_description.toString()
                binding.tstFooter.text = context.getString(R.string.rupee)+" "+it.lab_test_rate.toString()
              //  binding.labFooter.text = "District: " + districtz[it.district_id].toString() +", Pincode: "+ it.pincode.toString()
            }
            this.currenTest = test
            this.currentPosition = pos
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    testFilterList = tests
                } else {
                    val resultList = ArrayList<Laballtests>()
                    for (row in tests) {
                       // Log.d("--jk----",row.test_name)
                        if (row.test_name?.toLowerCase(Locale.ROOT)?.contains(charSearch.toLowerCase(Locale.ROOT)) == true) {
                            resultList.add(row)
                        }
                    }
                    testFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = testFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                testFilterList = results?.values as ArrayList<Laballtests>
                notifyDataSetChanged()
            }

        }
    }

}

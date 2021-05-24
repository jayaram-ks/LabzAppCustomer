package com.labzapp.customer.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.labzapp.customer.R
import com.labzapp.customer.databinding.AvailTestListItemBinding
import com.labzapp.customer.fragments.AvailableTestsFragment
import com.labzapp.customer.fragments.TestDialogFragment
import com.labzapp.customer.models.Laballtests
import com.labzapp.customer.utilities.toastz

class AvailTestsAdapter( val context: Context,private val tests: List<Laballtests>) : RecyclerView.Adapter<AvailTestsAdapter.TestsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestsViewHolder {
        val binding = AvailTestListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TestsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return tests.size
    }

    override fun onBindViewHolder(holder: TestsViewHolder, position: Int) {
        val testp = tests[position]
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
}

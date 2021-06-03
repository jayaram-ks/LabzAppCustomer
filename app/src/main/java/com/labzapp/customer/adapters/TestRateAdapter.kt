package com.labzapp.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.labzapp.customer.R
import com.labzapp.customer.databinding.BookTestrateListItemBinding
import com.labzapp.customer.models.Labtestsrates

class TestRateAdapter( val context: Context,private val testrates: ArrayList<Labtestsrates>) : RecyclerView.Adapter<TestRateAdapter.TestRateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestRateViewHolder {
        val binding = BookTestrateListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TestRateViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return testrates.size
    }

    override fun onBindViewHolder(holder: TestRateViewHolder, position: Int) {
        val testratep = testrates[position]
        holder.setData(testratep, position)
    }

    inner class TestRateViewHolder(private val binding: BookTestrateListItemBinding) : RecyclerView.ViewHolder(binding.root){

        var currentTestRate: Labtestsrates? = null
        var currentPosition: Int = 0

        init {
            itemView.setOnClickListener {

            }
        }

        fun setData(testrate: Labtestsrates?, pos: Int) {
            testrate?.let {
                binding.testzTitle.text = it.test_name
                binding.testzRate.text = context.getString(R.string.rupee)+" "+it.lab_test_rate.toString()
            }
            this.currentTestRate = testrate
            this.currentPosition = pos
        }
    }
}

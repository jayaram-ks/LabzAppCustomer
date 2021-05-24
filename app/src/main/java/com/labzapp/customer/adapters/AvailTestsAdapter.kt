package com.labzapp.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.labzapp.customer.databinding.AvailTestListItemBinding
import com.labzapp.customer.models.Laballtests

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
                // currenTest?.let {

                // }
            }

           /* binding.avlTests.setOnClickListener {
                currenTest?.let {
                    val bundle = Bundle()
                    bundle.putInt("lab_id", it.lab_id)
                    val appCompatActivity = context as AppCompatActivity
                    val transaction = appCompatActivity.supportFragmentManager.beginTransaction()
                    val openfragmt = AvailableTestsFragment()
                    openfragmt.arguments = bundle
                    transaction.replace(R.id.frame_container, openfragmt)
                    transaction.addToBackStack(null)
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    transaction.commit()

                }
            }*/
        }

        fun setData(test: Laballtests?, pos: Int) {
            test?.let {
                binding.tstTitle.text = it.test_name.toString()
                binding.tstDetails.text = it.test_description.toString()
              //  binding.labFooter.text = "Dist: " + districtz[it.district_id].toString() +", Pincode: "+ it.pincode.toString()
            }
            this.currenTest = test
            this.currentPosition = pos
        }
    }
}

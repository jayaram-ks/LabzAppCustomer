package com.labzapp.customer.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.labzapp.customer.R
import com.labzapp.customer.databinding.AvailTestListItemBinding
import com.labzapp.customer.models.Laballtests
import java.util.*
import kotlin.collections.ArrayList

class AvailTestsAdapter( val context: Context,private val tests: ArrayList<Laballtests>) : RecyclerView.Adapter<AvailTestsAdapter.TestsViewHolder>() {

    var testFilterList = ArrayList<Laballtests>()
    private var tracker: SelectionTracker<Long>? = null

    init {
        testFilterList = tests
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestsViewHolder {
        val binding = AvailTestListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TestsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return testFilterList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: TestsViewHolder, position: Int) {
        val testp = testFilterList[position]
        holder.setData(testp, position)
    }

    inner class TestsViewHolder(private val binding: AvailTestListItemBinding) : RecyclerView.ViewHolder(binding.root){

        var currenTest: Laballtests? = null
        var currentPosition: Int = 0

        init {


            // Removed for now
            /*itemView.setOnClickListener {
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
            } */

        }

        @SuppressLint("SetTextI18n")
        fun setData(test: Laballtests?, pos: Int) {
            test?.let {
                binding.tstTitle.text = it.test_name.toString()
                binding.tstDetails.text = it.test_description.toString()
                binding.tstRecommtxt.text =it.test_recommendation.toString() +"\n"+ it.test_recommendation2.toString()
                binding.tstFooter.text = context.getString(R.string.rupee)+" "+it.lab_test_rate.toString()
              //  binding.labFooter.text = "District: " + districtz[it.district_id].toString() +", Pincode: "+ it.pincode.toString()
            }
            this.currenTest = test
            this.currentPosition = pos

            if(tracker!!.isSelected(pos.toLong())) {
                itemView.setBackgroundColor( ContextCompat.getColor(context,R.color.green))
            } else {
                itemView.setBackgroundColor( ContextCompat.getColor(context,R.color.white))
            }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object: ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = absoluteAdapterPosition
                override fun getSelectionKey(): Long? = itemId

                override fun inSelectionHotspot(e: MotionEvent): Boolean {
                    return true
                }
            }

    }

    fun setTracker(tracker: SelectionTracker<Long>?) {
        this.tracker = tracker
    }



}

class MyLookupLabTest(private val rv: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long> {
        val view = rv.findChildViewUnder(event.x, event.y)
        return (view?.let { rv.getChildViewHolder(it) } as AvailTestsAdapter.TestsViewHolder).getItemDetails()
    }
}

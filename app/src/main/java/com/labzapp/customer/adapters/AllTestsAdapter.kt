package com.labzapp.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.labzapp.customer.R
import com.labzapp.customer.databinding.AllTestsListItemBinding
import com.labzapp.customer.models.Tests

class AllTestsAdapter( val context: Context,private val tests: ArrayList<Tests>) : RecyclerView.Adapter<AllTestsAdapter.TestsViewHolder>() {

    var testFilterList = ArrayList<Tests>()
    private var tracker: SelectionTracker<Long>? = null
    var selectedTestsPos: ArrayList<Long> = ArrayList()
    init {
        testFilterList = tests
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestsViewHolder {
        val binding = AllTestsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class TestsViewHolder(private val binding: AllTestsListItemBinding) : RecyclerView.ViewHolder(binding.root){

        var currenTest: Tests? = null
        var currentPosition: Int = 0
        init {

        }
        fun setData(test: Tests?, pos: Int) {
            test?.let {
                binding.tstId.text = it.id.toString()
                binding.tstTitle.text = it.test_name.toString()
                binding.tstDetails.text = it.test_description.toString()
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

class MyLookup(private val rv: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long> {
        val view = rv.findChildViewUnder(event.x, event.y)
        return (view?.let { rv.getChildViewHolder(it) } as AllTestsAdapter.TestsViewHolder).getItemDetails()
    }
}

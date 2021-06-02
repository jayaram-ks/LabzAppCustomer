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
import com.labzapp.customer.databinding.BookingLabListItemBinding
import com.labzapp.customer.models.Labswithtest
import com.squareup.picasso.Picasso

class BookLabsAdapter( val context: Context,private val labs: ArrayList<Labswithtest>) : RecyclerView.Adapter<BookLabsAdapter.LabViewHolder>() {

    private var ltracker: SelectionTracker<Long>? = null
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabViewHolder {
        val binding = BookingLabListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LabViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return labs.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: LabViewHolder, position: Int) {
        val labp = labs[position]
        holder.setData(labp, position)
    }

    inner class LabViewHolder(private val binding: BookingLabListItemBinding) : RecyclerView.ViewHolder(binding.root){

        var currentLab: Labswithtest? = null
        var currentPosition: Int = 0
        init {

        }
        fun setData(lab: Labswithtest?, pos: Int) {
            lab?.let {
                binding.bookLabTitle.text = it.name
                binding.bookLabAddress.text = it.address
                binding.labId.text = it.lab_id.toString()
                if(it.thumbnail != null) {
                    Picasso.with(context).load(it.thumbnail).fit().centerCrop()
                        .into(binding.labzLogo)
                }else {
                    Picasso.with(context).load(R.drawable.squarelogo).fit().centerCrop()
                        .into(binding.labzLogo)
                }

            }
            this.currentLab = lab
            this.currentPosition = pos

            if(ltracker!!.isSelected(pos.toLong())) {
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
        this.ltracker = tracker
    }
}

class MyLabsLookup(private val lrv: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long> {
        val view = lrv.findChildViewUnder(event.x, event.y)
        return (view?.let { lrv.getChildViewHolder(it) } as BookLabsAdapter.LabViewHolder).getItemDetails()
    }
}

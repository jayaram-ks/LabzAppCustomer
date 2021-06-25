package com.labzapp.customer.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.labzapp.customer.R
import com.labzapp.customer.databinding.LabListItemBinding
import com.labzapp.customer.fragments.AvailableTestsFragment
import com.labzapp.customer.models.LabData
import com.labzapp.customer.utilities.districtz
import com.squareup.picasso.Picasso

class LabsAdapter( val context: Context,private val labs: List<LabData>) : RecyclerView.Adapter<LabsAdapter.LabViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabViewHolder {
        val binding = LabListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LabViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return labs.size
    }

    override fun onBindViewHolder(holder: LabViewHolder, position: Int) {
        val labp = labs[position]
        holder.setData(labp, position)
    }

    inner class LabViewHolder(private val binding: LabListItemBinding) : RecyclerView.ViewHolder(binding.root){

        var currentLab: LabData? = null
        var currentPosition: Int = 0

        init {
            itemView.setOnClickListener {
              // currentLab?.let {

               // }
            }

            binding.avlTests.setOnClickListener {
               currentLab?.let {
                   val bundle = Bundle()
                   bundle.putInt("lab_id", it.lab_id)
                   bundle.putString("lab_title", it.name)
                   bundle.putString("lab_logo", it.thumbnail)
                   bundle.putString("lab_address", it.address)
                   val appCompatActivity = context as AppCompatActivity
                   val transaction = appCompatActivity.supportFragmentManager.beginTransaction()
                   val openfragmt = AvailableTestsFragment()
                   openfragmt.arguments = bundle
                   transaction.replace(R.id.frame_container, openfragmt)
                   transaction.addToBackStack(null)
                   transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                   transaction.commit()

                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun setData(lab: LabData?, pos: Int) {
            lab?.let {
                binding.labTitle.text = it.name.toString()
                binding.labDetails.text = it.address.toString()
                binding.labFooter.text = "Dist: " + districtz[it.district_id].toString() +", Pincode: "+ it.pincode.toString()
                if(it.thumbnail != null) {
                    Picasso.with(context).load(it.thumbnail).fit().centerCrop()
                        .into(binding.labLogo)
                }else
                {
                    Picasso.with(context).load(R.drawable.no_lab).fit().centerCrop()
                        .into(binding.labLogo)
                }

            }
            this.currentLab = lab
            this.currentPosition = pos
        }
    }
}

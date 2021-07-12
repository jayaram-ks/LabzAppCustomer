package com.labzapp.customer.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.labzapp.customer.R
import com.labzapp.customer.databinding.PackBannerListItemBinding
import com.labzapp.customer.fragments.AvailableTestsFragment
import com.labzapp.customer.fragments.LabsFragment
import com.labzapp.customer.fragments.PackBannerDialogFragment
import com.labzapp.customer.fragments.PackDialogFragment
import com.labzapp.customer.models.PackBannerData
import com.squareup.picasso.Picasso

class PackBannerAdapter(val context: Context, private val pbanners: List<PackBannerData>) : RecyclerView.Adapter<PackBannerAdapter.PackBannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackBannerViewHolder {
        val binding = PackBannerListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PackBannerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return pbanners.size
    }

    override fun onBindViewHolder(holder: PackBannerViewHolder, position: Int) {
        val pbannerp = pbanners[position]
        holder.setData(pbannerp, position)
    }

    inner class PackBannerViewHolder(private val binding: PackBannerListItemBinding) : RecyclerView.ViewHolder(binding.root){
        var currentPBanner: PackBannerData? = null
        var currentBPosition: Int = 0

        init {

            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("param1", currentPBanner?.packid.toString())
                val appCompatActivity = context as AppCompatActivity
                val fragmentManager =  appCompatActivity.supportFragmentManager
                val dialgFragment = PackBannerDialogFragment()
                dialgFragment.arguments = bundle
                dialgFragment.show(fragmentManager,null)

            }
        }

        fun setData(pbanner: PackBannerData?, ppos: Int) {
            pbanner?.let {
                Picasso.with(context).load(pbanner.pack_image).fit().into(binding.packImage)
            }

            this.currentPBanner = pbanner
            this.currentBPosition = ppos
        }

    }

}

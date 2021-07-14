package com.labzapp.customer.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.labzapp.customer.R
import com.labzapp.customer.databinding.AvailPackListItemBinding
import com.labzapp.customer.fragments.PackDialogFragment
import com.labzapp.customer.models.PackDetails
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class AvailPacksAdapter( val context: Context,private val packs: ArrayList<PackDetails>) : RecyclerView.Adapter<AvailPacksAdapter.PacksViewHolder>() ,Filterable{

    var packFilterList = ArrayList<PackDetails>()

    init {
        packFilterList = packs
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PacksViewHolder {
        val binding = AvailPackListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PacksViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return packFilterList.size
    }

    override fun onBindViewHolder(holder: PacksViewHolder, position: Int) {
        val packp = packFilterList[position]
        holder.setData(packp, position)
    }

    inner class PacksViewHolder(private val binding: AvailPackListItemBinding) : RecyclerView.ViewHolder(binding.root){

        var currenPack: PackDetails? = null
        var currentPosition: Int = 0

        init {
            itemView.setOnClickListener {
                currenPack?.let {
                    val bundle = Bundle()
                    bundle.putString("param1", it.pack_name)
                    bundle.putString("param2", it.pack_desc.toString())
                    bundle.putString("param3", it.pack_precautions.toString())
                    bundle.putString("param4", it.pack_image.toString())
                    bundle.putString("param5", it.pack_has_image.toString())
                    bundle.putString("param6", it.pack_id)

                    val appCompatActivity = context as AppCompatActivity
                    val fragmentManager =  appCompatActivity.supportFragmentManager
                    val dialgFragment = PackDialogFragment()
                    dialgFragment.arguments = bundle
                    dialgFragment.show(fragmentManager,null)
                }
            }

        }

        @SuppressLint("SetTextI18n")
        fun setData(pack: PackDetails?, pos: Int) {
            pack?.let {

                if( it.pack_has_image != "") {
                    Picasso.with(context).load(it.pack_image).fit().into(binding.listPackImg)
                    binding.listPackImg.visibility = View.VISIBLE
                }
                else
                {
                    binding.listPackImg.visibility = View.GONE
                }

                binding.packTitle.text = it.pack_name
                binding.packRate1.text = "Package Price : " +context.getString(R.string.rupee)+" "+it.rate_final
                binding.packRate2.text =  " Actual Price : " +context.getString(R.string.rupee)+" "+it.rate_initial
                binding.packDetails.text = "Tests Included : "+ it.pack_tests
                binding.packFooter.text = it.test_count+" Tests in this Package"
                binding.packRate2.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            }
            this.currenPack = pack
            this.currentPosition = pos
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    packFilterList = packs
                } else {
                    val resultList = ArrayList<PackDetails>()
                    for (row in packs) {
                        // Log.d("--jk----",row.test_name)
                        if (row.pack_name?.toLowerCase(Locale.ROOT)?.contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    packFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = packFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                packFilterList = results?.values as ArrayList<PackDetails>
                notifyDataSetChanged()
            }

        }
    }

}

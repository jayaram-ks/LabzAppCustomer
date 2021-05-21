package com.labzapp.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.labzapp.customer.databinding.LabListItemBinding
import com.labzapp.customer.models.LabData

class LabsAdapter(private val labs: List<LabData>) : RecyclerView.Adapter<LabsAdapter.LabViewHolder>() {

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
                currentLab?.let {
                    //context.showToast(currentLab!!.name + " Clicked !")
                }
            }

            //itemView.imgShare.setOnClickListener {

              //  currentLab?.let {
                //    val message: String = "My hobby is: " + currentLab!!.name

                //    val intent = Intent()
                 //   intent.action = Intent.ACTION_SEND
                 //   intent.putExtra(Intent.EXTRA_TEXT, message)
                  //  intent.type = "text/plain"

                   // context.startActivity(Intent.createChooser(intent, "Please select app: "))
               // }
           // }
        }

        fun setData(lab: LabData?, pos: Int) {
            lab?.let {
                binding.labTitle.text = lab.name.toString()
            }
            this.currentLab = lab
            this.currentPosition = pos
        }
    }
}

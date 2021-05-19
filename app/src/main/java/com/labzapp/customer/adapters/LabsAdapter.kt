package com.labzapp.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.labzapp.customer.databinding.LabListItemBinding
import com.labzapp.customer.models.LabData

class LabsAdapter(val context: Context, private val labs: List<LabData>) : RecyclerView.Adapter<LabsAdapter.LabViewHolder>() {

    companion object {
        val TAG: String = LabsAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabViewHolder {
        //val view = LayoutInflater.from(context).inflate(R.layout.lab_list_item, parent, false)
        //return MyViewHolder(view)

        val binding = LabListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return LabViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return labs.size
    }

    override fun onBindViewHolder(holder: LabViewHolder, position: Int) {
        val hobby = labs[position]
        holder.setData(hobby, position)
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

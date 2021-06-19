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
import com.labzapp.customer.databinding.MybookingListItemBinding
import com.labzapp.customer.fragments.MyBookingSingle
import com.labzapp.customer.models.Bookings
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MyBookAdapter( val context: Context,private val bookings: ArrayList<Bookings>) : RecyclerView.Adapter<MyBookAdapter.BookingViewHolder>() {
    val rupee = context.getString(R.string.rupee)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = MybookingListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return bookings.size
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val bookingp = bookings[position]
        holder.setData(bookingp, position)
    }

    inner class BookingViewHolder(private val binding: MybookingListItemBinding) : RecyclerView.ViewHolder(binding.root){

        var currentBook: Bookings? = null
        var currentPosition: Int = 0

        init {
            itemView.setOnClickListener {
                // currentBook?.let {
                // }
            }
            binding.viewDetails.setOnClickListener {
                currentBook?.let {
                    val bundle = Bundle()
                    bundle.putString("param1",it.id.toString())
                    val appCompatActivity = context as AppCompatActivity
                    val transaction = appCompatActivity.supportFragmentManager.beginTransaction()
                    val openfragmt = MyBookingSingle()
                    openfragmt.arguments = bundle
                    transaction.replace(R.id.frame_container, openfragmt)
                    transaction.addToBackStack(null)
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    transaction.commit()
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun setData(booking: Bookings?, pos: Int) {
            booking?.let {
                binding.patientName.text = "Patient : "+ it.patient_name
                binding.bookLabName.text = "Lab : "+ it.lab_name
                binding.bookTotal.text = "Booking ID : "+it.id +"   Grand Total : "+rupee+ it.total_to_pay.toString()

                val datFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val dat =  LocalDate.parse(it.booking_date , datFormat)
                val bookdate = dat.dayOfMonth.toString() +" "+dat.month.toString()+" "+dat.year.toString()
                binding.bookDate.text = "Booking Date : " +bookdate
                var prefdate = ""
                if(it.pref_date != null) {
                    val datp = LocalDate.parse(it.pref_date, datFormat)
                    prefdate = datp.dayOfMonth.toString() + " " + datp.month.toString() + " " + datp.year.toString()
                }

                if(it.report_file == null){
                    binding.downReport.text = "Pref Date : $prefdate\nTest Report : Not uploaded"
                }else{
                    binding.downReport.text = "Pref Date : $prefdate\nTest Report : Uploaded"
                }
            }
            this.currentBook = booking
            this.currentPosition = pos
        }
    }
}

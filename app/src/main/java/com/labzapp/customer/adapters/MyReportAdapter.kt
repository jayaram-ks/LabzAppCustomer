package com.labzapp.customer.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.labzapp.customer.R
import com.labzapp.customer.databinding.MyreportListItemBinding
import com.labzapp.customer.models.Reports
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MyReportAdapter( val context: Context,private val reports: ArrayList<Reports>) : RecyclerView.Adapter<MyReportAdapter.ReportViewHolder>() {
    val rupee = context.getString(R.string.rupee)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = MyreportListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return reports.size
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val reportp = reports[position]
        holder.setData(reportp, position)
    }

    inner class ReportViewHolder(private val binding: MyreportListItemBinding) : RecyclerView.ViewHolder(binding.root){

        var currentReport: Reports? = null
        var currentPosition: Int = 0

        init {
            itemView.setOnClickListener {
                // currentReport?.let {
                // }
            }

        }

        @SuppressLint("SetTextI18n")
        fun setData(report: Reports?, pos: Int) {
            report?.let {
                binding.patientName.text = "Patient : "+ it.patient_name
                binding.bookLabName.text = "Lab : "+ it.lab_name
                if(it.report_file == null){
                    binding.downReport.text = "Report not uploaded"
                }else{
                    binding.downReport.text = "Download Report"
                }
                val datFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val dat =  LocalDate.parse(it.booking_date , datFormat)
                val bookdate = dat.dayOfMonth.toString() +"-"+dat.monthValue.toString()+"-"+dat.year.toString()
                binding.bookTotal.text = "Grand Total : "+rupee+ it.total_to_pay.toString()
                binding.bkId.text = "Booking ID : "+it.id + "\nBooked On : $bookdate"

            }
            this.currentReport = report
            this.currentPosition = pos
        }
    }
}

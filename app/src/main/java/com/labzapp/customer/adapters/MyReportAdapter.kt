package com.labzapp.customer.adapters

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.Intent.CATEGORY_OPENABLE
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.labzapp.customer.R
import com.labzapp.customer.databinding.FragmentReportsBinding
import com.labzapp.customer.databinding.MyreportListItemBinding
import com.labzapp.customer.fragments.ReportWebview
import com.labzapp.customer.models.Reports
import com.labzapp.customer.utilities.snackzcolor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MyReportAdapter( val context: Context,rbinding:FragmentReportsBinding,private val reports: ArrayList<Reports>,reportUrl:String?) : RecyclerView.Adapter<MyReportAdapter.ReportViewHolder>() {
    val rupee = context.getString(R.string.rupee)
    val repUrl = reportUrl.toString()
    val parentbind:FragmentReportsBinding = rbinding

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
            binding.downReport.setOnClickListener {
                val labreport = repUrl +"/"+ (currentReport?.report_file)
                loadpdf(labreport,binding)
            }

            binding.viewReport.setOnClickListener {
                val labreporturl = repUrl +"/"+ (currentReport?.report_file)

                val bundle = Bundle()
                bundle.putString("param1", labreporturl)
                val appCompatActivity = context as AppCompatActivity
                val transaction = appCompatActivity.supportFragmentManager.beginTransaction()
                val openfragmt = ReportWebview()
                openfragmt.arguments = bundle
                transaction.replace(R.id.frame_container, openfragmt)
                transaction.addToBackStack(null)
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                transaction.commit()

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
                binding.bkId.text = "Booking ID : "+it.id + "   Booked On : $bookdate"

            }
            this.currentReport = report
            this.currentPosition = pos
        }
    }


    private var msg: String? = ""
    private var lastMsg = ""

    private fun loadpdf(url: String,binding: MyreportListItemBinding) {
        val directory = File(Environment.DIRECTORY_DOWNLOADS)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(url.substring(url.lastIndexOf("/") + 1))
                .setDescription("")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(directory.toString(),
                    url.substring(url.lastIndexOf("/") + 1)
                )
        }

        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)
        CoroutineScope(Dispatchers.IO).launch{
            var downloading = true
            while (downloading) {
                val cursor: Cursor = downloadManager.query(query)
                cursor.moveToFirst()
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                msg = statusMessage(url, directory, status)
                if (msg != lastMsg) {
                    CoroutineScope(Dispatchers.Main).launch {


                        if(status == DownloadManager.STATUS_SUCCESSFUL  ){
                            snackzcolor(parentbind.root,msg.toString(),parentbind.progressBar.id,"#FF6600",7000)
                        }else if(status == DownloadManager.STATUS_FAILED ){
                            snackzcolor(parentbind.root,msg.toString(),parentbind.progressBar.id,"#FF3333",3000)
                        }else if(status == DownloadManager.STATUS_PENDING ){
                            snackzcolor(parentbind.root,msg.toString(),parentbind.progressBar.id,"#FF3333",3000)
                        }else if(status == DownloadManager.STATUS_PAUSED ){
                            snackzcolor(parentbind.root,msg.toString(),parentbind.progressBar.id,"#FF3333",3000)
                        }else if(status == DownloadManager.STATUS_RUNNING ){
                            snackzcolor(parentbind.root,msg.toString(),parentbind.progressBar.id,"#000080",180000)
                        }else {
                            snackzcolor(parentbind.root,msg.toString(),parentbind.progressBar.id,"#FF3333",2000)
                        }

                    }
                    lastMsg = msg ?: ""
                }
                cursor.close()
                if(status == DownloadManager.STATUS_SUCCESSFUL  ){
                    //openFile()
                }
            }
        }
    }


    private fun statusMessage(url: String, directory: File, status: Int): String? {
        var msg = ""
        msg = when (status) {
            DownloadManager.STATUS_FAILED -> "Download has been failed, please try again"
            DownloadManager.STATUS_PAUSED -> "Paused"
            DownloadManager.STATUS_PENDING -> "Pending"
            DownloadManager.STATUS_RUNNING -> "Downloading..."
            DownloadManager.STATUS_SUCCESSFUL -> "Report downloaded successfully in $directory" + File.separator + url.substring(
                url.lastIndexOf("/") + 1
            )
            else -> "There's nothing to download"
        }
        return msg
    }

    fun openFile() {
        CoroutineScope(Dispatchers.Main).launch {
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path
            var fileopen = Uri.parse("$storageDir/")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.setDataAndTypeAndNormalize(fileopen, "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            intent.addCategory(CATEGORY_OPENABLE)
            startActivity(context,intent,null)
        }
    }
}

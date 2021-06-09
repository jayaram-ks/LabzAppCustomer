package com.labzapp.customer.models


data class MyReportResponse(val code : Int, val status : String, val message : String, val reports : ArrayList<Reports>)
data class Reports (val id : Int, val patient_name : String, val total_to_pay : Double, val booking_date : String, val lab_name : String, val report_file : String
)


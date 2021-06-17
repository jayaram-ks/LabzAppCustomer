package com.labzapp.customer.models

data class MyBookResponse(val code : Int, val status : String, val message : String, val bookings : ArrayList<Bookings>)
data class Bookings (val id : Int, val patient_name : String, val total_to_pay : Double, val booking_date : String,val pref_date:String, val lab_name : String, val report_file : String
)
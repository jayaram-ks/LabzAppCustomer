package com.labzapp.customer.models

data class MyBookSingleResponse(val code : Int, val status : String, val message : String, val booking : BookingSingle, val tests : ArrayList<BookedTests>)

data class BookingSingle (val id : Int, val patient_name : String, val patient_address : String,val age:String,val gender:String,
                          val mobile : String, val patient_pincode : String, val patient_district: String,
                          val service_charge : String, val booking_total : String, val paper_bill_charge : String?,
                          val paper_bill_status : String?, val paper_bill : String?, val booking_date : String, val pref_date : String?,
                          val report_file : String?, val lab_name : String, val lab_address : String?, val lab_pincode : String, val grand_total : String)

data class BookedTests (val booking_test_rate : String, val test_name : String)

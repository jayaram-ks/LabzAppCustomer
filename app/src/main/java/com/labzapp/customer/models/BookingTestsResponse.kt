package com.labzapp.customer.models

data class BookingTestsResponse(val code : Int, val status : String, val message : String, val tests : ArrayList<Tests>)

data class Tests(val id : Long, val test_name : String?, val test_recommendation : String?, val test_recommendation2 : String?, val test_description : String?)
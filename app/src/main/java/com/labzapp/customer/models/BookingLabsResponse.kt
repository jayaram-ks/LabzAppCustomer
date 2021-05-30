package com.labzapp.customer.models

data class BookingLabsResponse ( val code : Int,
    val status : String,
    val message : String,
    val labswithtest : List<Labswithtest>
)

data class Labswithtest( val lab_id : Int, val name : String, val address : String, val test_amount : Int, val service_charge : Int, val total_to_pay : Int )
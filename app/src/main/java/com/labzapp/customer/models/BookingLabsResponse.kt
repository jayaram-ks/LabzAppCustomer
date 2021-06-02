package com.labzapp.customer.models
import kotlinx.serialization.Serializable
data class BookingLabsResponse ( val code : Int, val status : String, val message : String, val labswithtest : ArrayList<Labswithtest>)
@Serializable
data class Labswithtest( val lab_id : Long, val name : String, val address : String, val thumbnail: String?, val test_amount : Int, val service_charge : Int, val total_to_pay : Int )
package com.labzapp.customer.models

data class BookingTestRatesResponse (
    val code : Int,
    val status : String,
    val message : String,
    val labtestsrates : ArrayList<Labtestsrates>
)

data class Labtestsrates (
    val testid : Int,
    val test_name : String,
    val lab_test_rate : Int
)
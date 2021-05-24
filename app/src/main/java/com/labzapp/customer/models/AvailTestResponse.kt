package com.labzapp.customer.models

data class AvailTestResponse(val code : Int, val status : String, val message : String, val laballtests : ArrayList<Laballtests>)

data class Laballtests(val testid : Int, val test_name : String?, val test_description : String?, val test_recommendation : String?, val test_recommendation2 : String?, val lab_test_rate : Double? )
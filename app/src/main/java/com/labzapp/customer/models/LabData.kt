package com.labzapp.customer.models

data class LabData ( val lab_id : Int,
    val name : String?,
    val address : String?,
    val service_charge : Int?,
    val description : String?,
    val pincode : String?,
    val phone : String?,
    val district_id : Int?
)
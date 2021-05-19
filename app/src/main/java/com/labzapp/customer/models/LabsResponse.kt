package com.labzapp.customer.models

data class LabsResponse (

    val code : Int,
    val status : String,
    val message : String,
    val labs : List<LabData>
)
package com.labzapp.customer.models

data class LabsResponse (val code : Int, val status : String, val message : String, val labs : List<LabData>)

data class LabData ( val lab_id : Int, val name : String?, val address : String?, val service_charge : Int?, val description : String?,
                     val pincode : String?, val phone : String?, val district_id : Int?, val thumbnail: String?)
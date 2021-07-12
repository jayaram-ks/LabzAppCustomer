package com.labzapp.customer.models

data class SinglePackResponse(val code : Int,
                              val status : String,
                              val message : String,
                              val packdetails : SinglePackDetails)

data class SinglePackDetails (
    val pack_id : String?,
    val pack_name : String?,
    val pack_desc : String?,
    val pack_tests : String?,
    val pack_precautions : String?,
    val pack_image : String?,
    val rate_initial : String?,
    val rate_final : String?,
    val test_count : String?,
    val lab_name : String?,
    val lab_address : String?
)

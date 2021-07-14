package com.labzapp.customer.models

data class AvailPackResponse(val code : Int,
                            val status : String,
                            val message : String,
                            val laballpacks : ArrayList<PackDetails>)

data class PackDetails (
    val pack_id : String,
    val pack_name : String,
    val pack_desc : String?,
    val pack_tests : String?,
    val pack_precautions : String?,
    val pack_image : String?,
    val pack_has_image : String?,
    val rate_initial : String?,
    val rate_final : String?,
    val test_count: String?
)

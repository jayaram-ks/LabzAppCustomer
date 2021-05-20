package com.labzapp.customer.models

data class ProfileResponse(val code : Int, val status : String, val message : Any?,val customer : ProfileData)

data class ProfileData (val name : String?, val address : String?, val phone : String, val pincode : Int?, val latitude : String?, val longitude : String?,
                        val district : Int?, val age : Int?, val gender : String?

)

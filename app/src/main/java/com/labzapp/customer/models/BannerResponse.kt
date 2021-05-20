package com.labzapp.customer.models

data class BannerResponse(val code : Int, val status : String, val message : String, val banner : List<BannerData>)

data class BannerData(val id : Int, val title : String?, val description : String?, val thumbnail : String?)

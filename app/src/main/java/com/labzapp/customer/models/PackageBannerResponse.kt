package com.labzapp.customer.models

data class PackageBannerResponse(val code : Int,
                                 val status : String,
                                 val message : String,
                                 val packbanner : List<PackBannerData>)

data class PackBannerData (val packid : Int, val pack_image : String?)
package com.labzapp.customer.models

data class OtpResponse(val code : Int, val status : String, val message : Any?, val api_token: String?, val auth_key: String?)

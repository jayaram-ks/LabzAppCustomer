package com.labzapp.customer.models

data class RegisterResponse(val code : Int, val status : String, val message : Any?)

data class SaveCustomerResponse(val code : Int, val status : String, val message : Any?)

data class SubmitBookingResponse(val code : Int, val status : String, val message : Any?)

data class MakeCallResponse(val code : Int, val status : String, val message : Any?)

data class EditProfileResponse(val code : Int, val status : String, val message : Any?)

data class UploadPresResponse(val code : Int, val status : String, val message : Any?)


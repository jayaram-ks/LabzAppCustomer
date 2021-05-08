package com.labzapp.customer.services

import com.labzapp.customer.models.OtpResponse
import com.labzapp.customer.models.RegisterResponse
import com.labzapp.customer.utilities.REG_USER
import com.labzapp.customer.utilities.USER_OTP_VERIFY
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST(REG_USER)
    fun registerUser(
        @Field("customer_phone") mobile: String,
        @Field("customer_name") fullname: String,
        @Field("customer_pincode") pincode: String
    ):Call<RegisterResponse>

    @FormUrlEncoded
    @POST(USER_OTP_VERIFY)
    fun otpValidate(
        @Field("customer_phone") mobile : String,
        @Field("customer_mobile_otp") otp : String
    ): Call<OtpResponse>
}
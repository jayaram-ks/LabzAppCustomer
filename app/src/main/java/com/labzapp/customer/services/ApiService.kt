package com.labzapp.customer.services

import com.labzapp.customer.models.OtpResponse
import com.labzapp.customer.models.ProfileResponse
import com.labzapp.customer.models.RegisterResponse
import com.labzapp.customer.models.SaveCustomerResponse
import com.labzapp.customer.utilities.REG_AFTER_OTP
import com.labzapp.customer.utilities.REG_USER
import com.labzapp.customer.utilities.USER_OTP_VERIFY
import com.labzapp.customer.utilities.USER_PROFILE
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST(REG_USER)
    fun registerUser(
        @Field("customer_phone") mobile: String,
        @Field("customer_name") fullname: String,
        @Field("customer_pincode") pincode: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST(USER_OTP_VERIFY)
    fun otpValidate(
        @Field("customer_phone") mobile : String,
        @Field("customer_mobile_otp") otp : String
    ): Call<OtpResponse>


    @GET(USER_PROFILE)
    fun getProfile(
        @Header("Authorization") authtoken: String?,
        @Query("api_token") apitoken: String?
    ): Call <ProfileResponse>

    @FormUrlEncoded
    @POST(REG_AFTER_OTP)
    fun saveCustomer(
        @Header("Authorization") authtoken: String?,
        @Field("api_token") apitoken: String?,
        @Field("customer_latitude") custLatitude: String?,
        @Field("customer_longitude") custLongitude: String?,
        @Field("age") custAge: String?,
        @Field("gender") custGender: String?,
        @Field("customer_address") custAddrs: String?,
        @Field("customer_district") custDistrict: String?,
    ): Call<SaveCustomerResponse>
}

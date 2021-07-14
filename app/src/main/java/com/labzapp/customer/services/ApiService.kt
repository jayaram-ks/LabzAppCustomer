package com.labzapp.customer.services

import com.labzapp.customer.models.*
import com.labzapp.customer.utilities.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    @GET(LIST_LABS)
    fun listLabs(
        @Header("Authorization") authtoken: String?,
        @Query("api_token") apitoken: String?,
        @Query("latitude") lat: Double?,
        @Query("longitude") lng: Double?
    ): Call<LabsResponse>

    @GET(GET_BANNERS)
    fun getBanners(
        @Header("Authorization") authtoken: String?,
        @Query("api_token") apitoken: String?,
    ): Call<BannerResponse>

    @GET(GET_TESTS_AVAILABLE)
    fun getAvailTests(
        @Header("Authorization") authtoken: String?,
        @Query("api_token") apitoken: String?,
        @Query("lab_id") labid: Int?,
    ): Call<AvailTestResponse>

    @GET(GET_ALL_TESTS)
    fun getAllTests(
        @Header("Authorization") authtoken: String?,
        @Query("api_token") apitoken: String?,
    ): Call<BookingTestsResponse>

    @GET(GET_LABS_HAVING_TESTS)
    fun getLabsHavingTests(
        @Header("Authorization") authtoken: String?,
        @Query("api_token") apitoken: String?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
        @Query("test_ids[]") items:MutableList<String>,
    ):Call<BookingLabsResponse>

    @GET(GET_LABTEST_RATE)
    fun getTestRatesforLab(
        @Header("Authorization") authtoken: String?,
        @Query("api_token") apitoken: String?,
        @Query("lab_id") labid: String?,
        @Query("test_ids[]") items:MutableList<String>,
    ):Call<BookingTestRatesResponse>

    @FormUrlEncoded
    @POST(SUBMIT_BOOKING)
    fun submitBooking(
        @Header("Authorization") authtoken: String?,
        @Field("api_token") apitoken: String?,
        @Field("patient_name") pname: String?,
        @Field("patient_mobile") pmobile: String?,
        @Field("age") patntage: String?,
        @Field("gender") pgender: String?,
        @Field("patient_address") paddress: String?,
        @Field("patient_pincode") pincode: String?,
        @Field("patient_latitude") plat: String?,
        @Field("patient_longitude") plong: String?,
        @Field("patient_district") pdist: String?,
        @Field("test_ids[]") testids:MutableList<String>,
        @Field("lab_id") labid: String?,
        @Field("paper_bill_needed") paperbill: String?,
        @Field("pref_date") prefdate: String?,
        ): Call<SubmitBookingResponse>

    @FormUrlEncoded
    @POST(REGISTER_CALL)
    fun registerCustomerCall(
        @Header("Authorization") authtoken: String?,
        @Field("api_token") apitoken: String?,
    ):Call<MakeCallResponse>

    @FormUrlEncoded
    @POST(EDIT_LOC_PROFILE)
    fun updateProfile(
        @Header("Authorization") authtoken: String?,
        @Field("api_token") apitoken: String?,
        @Field("customer_name") cname: String?,
        @Field("age") custage: String?,
        @Field("gender") cgender: String?,
        @Field("customer_address") caddress: String?,
        @Field("customer_pincode") cpincode: String?,
        @Field("customer_district") cdist: String?,
        @Field("customer_latitude") clat: String?,
        @Field("customer_longitude") clong: String?,
    ): Call<EditProfileResponse>

    @GET(GET_MYBOOKINGS)
    fun getMyBook(
        @Header("Authorization") authtoken: String?,
        @Query("api_token") apitoken: String?,
    ):Call<MyBookResponse>

    @GET(BOOK_SINGLE_DETAILS)
    fun getMyBookDetails(
        @Header("Authorization") authtoken: String?,
        @Query("api_token") apitoken: String?,
        @Query("booking_id") bookingid: String?,
    ):Call<MyBookSingleResponse>

    @GET(GET_MYREPORTS)
    fun getMyReports(
        @Header("Authorization") authtoken: String?,
        @Query("api_token") apitoken: String?,
    ):Call<MyReportResponse>

    @Multipart
    @POST(PRESC_FILE_UPLOAD)
    fun uploadPrescription(
        @Header("Authorization") authtoken : String?,
        @Part("api_token") apitoken : RequestBody,
        @Part prescrImage :MultipartBody.Part,
    ):Call<UploadPresResponse>

    @FormUrlEncoded
    @POST(UPDATE_DEVICE_ID)
    fun updateFBDeviceID(
        @Header("Authorization") authtoken: String?,
        @Field("api_token") apitoken: String?,
        @Field("device_token") fbtoken: String?,
    ): Call<FbIdUpdateResponse>

    @GET(GET_PACKS_AVAILABLE)
    fun getAvailPacks(
        @Header("Authorization") authtoken: String?,
        @Query("api_token") apitoken: String?,
        @Query("lab_id") labid: Int?,
    ): Call<AvailPackResponse>

    @GET(GET_PACKS_BANNER)
    fun getPackagesBanner(
        @Header("Authorization") authtoken: String?,
        @Query("api_token") apitoken: String?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
    ):Call<PackageBannerResponse>

    @GET(GET_SINGLE_PACK_DET)
    fun getSinglePackDetail(
        @Header("Authorization") authtoken: String?,
        @Query("api_token") apitoken: String?,
        @Query("pack_id") packid: String?,
    ):Call<SinglePackResponse>

    @FormUrlEncoded
    @POST(SUBMIT_PACKAGE_BOOKING)
    fun submitPackBooking(
        @Header("Authorization") authtoken: String?,
        @Field("api_token") apitoken: String?,
        @Field("patient_name") pname: String?,
        @Field("patient_mobile") pmobile: String?,
        @Field("age") patntage: String?,
        @Field("gender") pgender: String?,
        @Field("patient_address") paddress: String?,
        @Field("patient_pincode") pincode: String?,
        @Field("patient_latitude") plat: String?,
        @Field("patient_longitude") plong: String?,
        @Field("patient_district") pdist: String?,
        @Field("pack_id") packid: String?,
        @Field("paper_bill_needed") paperbill: String?,
        @Field("pref_date") prefdate: String?,
    ): Call<SubmitBookingResponse>

}

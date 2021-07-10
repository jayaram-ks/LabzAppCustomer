package com.labzapp.customer.utilities

const val API_BASEURL = "http://labzapp.in/api/v1/customer/"
const val REG_USER = "register"
const val USER_OTP_VERIFY = "verifyotp"
const val USER_PROFILE = "profile"
const val REG_AFTER_OTP ="savecustomerdetails"
const val LIST_LABS = "listlabs"
const val GET_BANNERS = "banners"
const val GET_TESTS_AVAILABLE = "labstestdetails"
const val GET_ALL_TESTS = "tests"
const val GET_LABS_HAVING_TESTS = "labswithtest"
const val GET_LABTEST_RATE = "labtestswithrates"
const val SUBMIT_BOOKING = "submitbooking"
const val REGISTER_CALL = "callregistration"
const val EDIT_LOC_PROFILE = "editcustomerprofile"
const val GET_MYBOOKINGS = "mybookings"
const val BOOK_SINGLE_DETAILS = "bookingdetails"
const val GET_MYREPORTS = "getmyreports"
const val PRESC_FILE_UPLOAD = "uploadprescription"
const val UPDATE_DEVICE_ID = "updatefbdeviceid"
const val GET_PACKS_AVAILABLE = "labspackagedetails"


val districtz = linkedMapOf(0 to "--Select District--", 1 to "Thiruvananthapuram", 2 to "Kollam", 3 to "Pathanamthitta", 4 to "Alappuzha", 5 to "Kottayam", 6 to "Idukki",
    7 to "Ernakulam", 8 to "Thrissur", 9 to "Palakkad", 10 to "Malappuram", 11 to "Kozhikode", 12 to "Wayanad", 13 to "Kannur", 14 to "Kasaragod")
val genderz = linkedMapOf(1 to "Male", 2 to "Female")
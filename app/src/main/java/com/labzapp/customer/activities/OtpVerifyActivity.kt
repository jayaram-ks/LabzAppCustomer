package com.labzapp.customer.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.labzapp.customer.R
import com.labzapp.customer.databinding.ActivityOtpVerifyBinding
import com.labzapp.customer.models.OtpResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.toastz
import com.poovam.pinedittextfield.PinField
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpVerifyActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityOtpVerifyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerifyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val otpmessage:String = intent.getStringExtra("otp_message").toString()
        val custmobile:String = intent.getStringExtra("customermobile").toString()
        binding.otpMessg.text = otpmessage
        val squareField = findViewById<PinField>(R.id.squareField1)
        val listener = object : PinField.OnTextCompleteListener{
            override fun onTextComplete(enteredText: String): Boolean {
                verifyOtp(custmobile,enteredText)
                return@onTextComplete true
            }
        }
        squareField.onTextCompleteListener = listener
        binding.cancelOtp.setOnClickListener(){
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun verifyOtp(cmobile: String,otp: String){
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.otpValidate(cmobile,otp)
        requestCall.enqueue(object : Callback<OtpResponse> {

            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {

                    toastz(this@OtpVerifyActivity,resp?.message.toString())
                    val api_tokn = resp.api_token.toString()
                    val auth_ky  = resp.auth_key.toString()
                    SharedPrefManager.getInstance(applicationContext).saveCredentials(api_tokn,auth_ky)
                    val intent = Intent(this@OtpVerifyActivity, ProfileUpdateActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                } else {
                    toastz(this@OtpVerifyActivity,resp?.message.toString())
                }
            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                toastz(this@OtpVerifyActivity,t.message.toString())
            }

        })
    }
}
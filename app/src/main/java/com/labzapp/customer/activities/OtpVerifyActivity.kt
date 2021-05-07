package com.labzapp.customer.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.labzapp.customer.databinding.ActivityOtpVerifyBinding

class OtpVerifyActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityOtpVerifyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerifyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val otpmessage:String = intent.getStringExtra("otp_message").toString()
        binding.otpMessg.text = otpmessage


    }
}
package com.labzapp.customer.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.labzapp.customer.databinding.ActivityRegisterBinding
import com.labzapp.customer.models.RegisterResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.network.ConnectionType
import com.labzapp.customer.utilities.network.NetworkMonitorUtil
import com.labzapp.customer.utilities.snackze
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegisterActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityRegisterBinding
    private val networkMonitor = NetworkMonitorUtil(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        networkMonitor.result = { isAvailable, type ->
            runOnUiThread {
                when (isAvailable) {
                    true -> {
                        when (type) {
                            ConnectionType.Wifi -> {
                                //internet_status.text = "Wifi Connection"
                            }
                            ConnectionType.Cellular -> {
                               // internet_status.text = "Cellular Connection"
                            }
                            else -> { }
                        }
                    }
                    false -> {
                        snackze(view, "No Network Connection", binding.fullName.id)
                    }
                }
            }
        }

        binding.getOtp.setOnClickListener {
            val mobile = binding.mobileNumber.editText?.text.toString()
            val fullname = binding.fullName.editText?.text.toString()
            val pincode = binding.pincode.editText?.text.toString()
            getOTP(mobile, fullname, pincode)
        }
    }

    private fun getOTP(mobile: String, fullname: String, pincode: String) {
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.registerUser(mobile, fullname, pincode)
        requestCall.enqueue(object : Callback<RegisterResponse> {

            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {

                    val intent = Intent(this@RegisterActivity, OtpVerifyActivity::class.java)
                    intent.putExtra("otp_message",resp?.message.toString())
                    intent.putExtra("customermobile",mobile)
                    startActivity(intent)

                } else {
                    snackze(binding.root, resp?.message.toString(), binding.fullName.id)
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                snackze(binding.root, t.message.toString(), binding.fullName.id)
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onStart() {
        super.onStart()

        if(SharedPrefManager.getInstance(this).isLoggedIn){
            val intent = Intent(applicationContext, ProfileUpdateActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        networkMonitor.register()
    }


    override fun onStop() {
        super.onStop()
        networkMonitor.unregister()
    }
}

package com.labzapp.customer.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.labzapp.customer.databinding.ActivityNoNetworkBinding

class NoNetworkActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoNetworkBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoNetworkBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.reloadWifi.setOnClickListener {
            finish()
        }
    }
}
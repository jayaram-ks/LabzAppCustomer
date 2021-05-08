package com.labzapp.customer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.labzapp.customer.databinding.ActivityProfileUpdateBinding

class ProfileUpdateActivity : AppCompatActivity() {
    private  lateinit var  binding:ActivityProfileUpdateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileUpdateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}
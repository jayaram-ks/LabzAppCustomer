package com.labzapp.customer.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.labzapp.customer.R
import com.labzapp.customer.databinding.ActivityBookingBinding
import com.labzapp.customer.fragments.BookingHomeFragment
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.network.ConnectionType
import com.labzapp.customer.utilities.network.NetworkMonitorUtil

class BookingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookingBinding
    private val networkMonitor = NetworkMonitorUtil(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
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
                        val intent = Intent(this, NoNetworkActivity ::class.java)
                        startActivity(intent)
                    }
                }
            }
        }


        val homeFragment = BookingHomeFragment()
        setCurrentFragment(homeFragment)
    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.booking_container,fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            commit()
        }

    override fun onStart() {
        super.onStart()
        if(!SharedPrefManager.getInstance(this).isLoggedIn){
            val intent = Intent(applicationContext, RegisterActivity::class.java)
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
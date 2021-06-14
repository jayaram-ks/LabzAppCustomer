package com.labzapp.customer.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.labzapp.customer.R
import com.labzapp.customer.databinding.ActivityHomeBinding
import com.labzapp.customer.fragments.HomeFragment
import com.labzapp.customer.fragments.LabsFragment
import com.labzapp.customer.fragments.ProfileFragment
import com.labzapp.customer.fragments.ReportsFragment
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.network.ConnectionType
import com.labzapp.customer.utilities.network.NetworkMonitorUtil

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val networkMonitor = NetworkMonitorUtil(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
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

        val homeFragment = HomeFragment()
        setCurrentFragment(homeFragment)

        val labsFragment = LabsFragment()
        val reportsFragment = ReportsFragment()
        val profileFragment = ProfileFragment()
        
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navhome->setCurrentFragment(homeFragment)
                R.id.navlabs->setCurrentFragment(labsFragment)
                R.id.navsearch->goToBooking()
                R.id.navreports->setCurrentFragment(reportsFragment)
                R.id.navprofile->setCurrentFragment(profileFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_container,fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            commit()
        }

    private fun goToBooking(){
        val intent = Intent(this, BookingActivity::class.java)
        startActivity(intent)
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
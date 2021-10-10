package com.labzapp.customer.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.labzapp.customer.R
import com.labzapp.customer.databinding.ActivityBookingBinding
import com.labzapp.customer.fragments.BookFromLabFragment
import com.labzapp.customer.fragments.BookingHomeFragment
import com.labzapp.customer.fragments.BookingPackHomeFragment
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.network.ConnectionType
import com.labzapp.customer.utilities.network.NetworkMonitorUtil

class BookingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookingBinding
    private val networkMonitor = NetworkMonitorUtil(this)
    private var packOrTest : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        var packageID:String? = null
        packOrTest = intent?.getStringExtra("pack_or_test")
        packageID = intent?.getStringExtra("pack_id")

        //booking From labs page
        val isBookFromLab:String? = intent?.getStringExtra("isBookFromLab")
        val tstIDstring: String? = intent?.getStringExtra("testidsFromLab")
        val tstlabString: String? = intent?.getStringExtra("labDataString")

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

       if(packOrTest == "2"){  //is package booking
           if(packageID != ""){
               val bundle = Bundle()
               bundle.putString("param1", packageID)
               val packFragment = BookingPackHomeFragment()
               packFragment.arguments = bundle
               val transaction = supportFragmentManager.beginTransaction()
               transaction.replace(R.id.booking_container, packFragment)
               transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
               transaction.commit()
           }


        }else{

            if(isBookFromLab == "yes"){ //Testbook from labslist
                val bundle = Bundle()
                bundle.putString("param1", tstIDstring)
                bundle.putString("param2", tstlabString)

                val bfFragment = BookFromLabFragment()
                bfFragment.arguments = bundle
                val transPrev = supportFragmentManager.beginTransaction()
                transPrev.replace(R.id.booking_container,bfFragment)
                transPrev.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                transPrev.commit()

            }
            else{  // Normal Tests  booking
                val homeFragment = BookingHomeFragment()
                setCurrentFragment(homeFragment)

            }

        }

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
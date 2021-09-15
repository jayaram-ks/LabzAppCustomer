package com.labzapp.customer.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.messaging.FirebaseMessaging
import com.labzapp.customer.R
import com.labzapp.customer.databinding.ActivityHomeBinding
import com.labzapp.customer.fragments.HomeFragment
import com.labzapp.customer.fragments.LabsFragment
import com.labzapp.customer.fragments.ProfileFragment
import com.labzapp.customer.fragments.ReportsFragment
import com.labzapp.customer.models.FbIdUpdateResponse
import com.labzapp.customer.services.ApiService
import com.labzapp.customer.services.ServiceBuilder
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.network.ConnectionType
import com.labzapp.customer.utilities.network.NetworkMonitorUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val networkMonitor = NetworkMonitorUtil(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //GET FireBase TOKEN
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if(it.isComplete){
                val fbToken = it.result.toString()
               // Log.d("token in Home is-----",fbToken)
                updateFbToken(fbToken)
            }
            else{
               // Log.d("FB_API_ER","Firebase token fail")
            }
        }

        //creating notification channel if android version is greater than or equals to oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = CHANNEL_DESC
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

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
        setCurrentFragment(homeFragment,false)

        val labsFragment = LabsFragment()
        val reportsFragment = ReportsFragment()
        val profileFragment = ProfileFragment()
        
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navhome->setCurrentFragment(homeFragment,false)
                R.id.navlabs->setCurrentFragment(labsFragment,false)
                R.id.navsearch->goToBooking()
                R.id.navreports->setCurrentFragment(reportsFragment,false)
                R.id.navprofile->setCurrentFragment(profileFragment,false)
            }
            true
        }
    }


    private fun setCurrentFragment(fragment: Fragment,addtoBackStck:Boolean)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_container,fragment)
            if(addtoBackStck){
                addToBackStack(null)
            }
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            commit()
        }

    private fun updateFbToken(fbtokn:String){
        val authTokn: String? = "Bearer "+ SharedPrefManager.getInstance(this).authKey
        val apiTokn: String? = SharedPrefManager.getInstance(this).apiToken
        val apiService = ServiceBuilder.buildService(ApiService::class.java)
        val requestCall = apiService.updateFBDeviceID(authTokn, apiTokn,fbtokn)
        requestCall.enqueue(object : Callback<FbIdUpdateResponse> {
            override fun onResponse(call: Call<FbIdUpdateResponse>, response: Response<FbIdUpdateResponse>) {
                val resp = response.body()
                if (resp?.code == 200) {
                 // Log.d("FBTOKEN",resp.message.toString())
                } else {
                    if (resp != null) {
                       // Log.d("FBTOKEN_ER",resp.message.toString())
                    }
                }
            }
            override fun onFailure(call: Call<FbIdUpdateResponse>, t: Throwable) {
               // Log.d("FB_API_ER",t.message.toString())
            }
        })
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

    companion object {
        const val CHANNEL_ID = "labzapp_customer_channel_id"
        private const val CHANNEL_NAME= "LabzApp"
        private const val CHANNEL_DESC = "LabzApp Notifications"
    }
}
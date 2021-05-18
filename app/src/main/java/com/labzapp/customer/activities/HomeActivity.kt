package com.labzapp.customer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.labzapp.customer.R
import com.labzapp.customer.databinding.ActivityHomeBinding
import com.labzapp.customer.fragments.HomeFragment
import com.labzapp.customer.fragments.LabsFragment
import com.labzapp.customer.fragments.ProfileFragment
import com.labzapp.customer.fragments.ReportsFragment

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val homeFragment = HomeFragment()
        val labsFragment = LabsFragment()
        val reportsFragment = ReportsFragment()
        val profileFragment = ProfileFragment()

        setCurrentFragment(homeFragment)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navhome->setCurrentFragment(homeFragment)
                R.id.navlabs->setCurrentFragment(labsFragment)
                R.id.navreports->setCurrentFragment(reportsFragment)
                R.id.navprofile->setCurrentFragment(profileFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_container,fragment)
            commit()
        }
}
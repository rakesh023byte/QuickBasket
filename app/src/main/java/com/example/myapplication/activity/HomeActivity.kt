package com.example.myapplication.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityHomeBinding
import com.example.myapplication.fragments.CartFragment
import com.example.myapplication.fragments.HomeFragment
import com.example.myapplication.fragments.OrderHistoryFragment
import com.example.myapplication.fragments.ProfileFragment
import com.example.myapplication.fragments.SearchFragment
import com.google.android.material.navigation.NavigationBarView

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment(HomeFragment())

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_search -> loadFragment(SearchFragment())
                R.id.nav_cart -> loadFragment(CartFragment())
                R.id.nav_order -> loadFragment(OrderHistoryFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }
        binding.bottomNavigation.labelVisibilityMode =
            NavigationBarView.LABEL_VISIBILITY_UNLABELED
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

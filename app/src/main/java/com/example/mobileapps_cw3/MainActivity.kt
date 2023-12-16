package com.example.mobileapps_cw3

import HomeFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mobileapps_cw3.databinding.MainMainpageBinding
import com.example.mobileapps_cw3.fragments.ProfileFragment
import com.example.mobileapps_cw3.fragments.SettingsFragment
import com.example.mobileapps_cw3.structures.Profile
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainMainpageBinding
    private lateinit var navigation: BottomNavigationView
    private lateinit var user: Profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainMainpageBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(R.layout.main_mainpage)

        user = intent.getSerializableExtra("user") as? Profile
            ?: Profile(-1, "", "", emptyList(), emptyList(), "",
                emptyList(), emptyList(), false, emptyList(), emptyList(), 0)

        replaceFragment(HomeFragment(), user)

        navigation = findViewById(R.id.bottomNavigation)
        navigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(HomeFragment(), user)
                R.id.profile -> replaceFragment(ProfileFragment(), user)
                R.id.settings -> replaceFragment(SettingsFragment())

                else -> {

                }
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment, user: Profile? = null) {
        val newFragment = fragment.apply {
            arguments = Bundle().apply {
                putSerializable("user", user)
            }
        }
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout, newFragment).commit()
    }

}

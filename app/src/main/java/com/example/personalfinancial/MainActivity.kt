package com.example.personalfinancial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.threetenabp.AndroidThreeTen

class MainActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val buttomNavigationView = findViewById<BottomNavigationView>(R.id.navigationBar)
        setupWithNavController(buttomNavigationView, navController)

//        binding = ActivityHomeBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Initialize NavController
//        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
//
//        // Set up navigation item click listener
//        binding.navigationBar.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.home -> {
//                    navController.navigate(R.id.homeFragment)
//                    true
//                }
//                R.id.planning -> {
//                    navController.navigate(R.id.planningFragment)
//                    true
//                }
//                R.id.graph -> {
//                    navController.navigate(R.id.graphFragment)
//                    true
//                }
//                R.id.history -> {
//                    navController.navigate(R.id.historyFragment)
//                    true
//                }
//                R.id.setting -> {
//                    navController.navigate(R.id.settingFragment)
//                    true
//                }
//                else -> false
//            }
//        }

    }
}

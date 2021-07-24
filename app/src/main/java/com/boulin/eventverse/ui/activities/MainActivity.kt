package com.boulin.eventverse.ui.activities

import android.os.Bundle
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.boulin.eventverse.R

import com.boulin.eventverse.data.model.User
import com.boulin.eventverse.data.viewmodels.EventViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.boulin.eventverse.data.model.Event
import com.boulin.eventverse.data.model.Location
import com.hadiyarajesh.flower.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity displaying all main pages of the app
 */
class MainActivity : AppCompatActivity() {

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Sets up the navigation
        val navBar = findViewById<BottomNavigationView>(R.id.bottom_nav_bar)
        val navController = findNavController(R.id.main_fragment_view)
        val bottomNavConfig = AppBarConfiguration(setOf(
            R.id.navigation_profile,
            R.id.navigation_home,
            R.id.navigation_events
        ))

        setupActionBarWithNavController(navController, bottomNavConfig)
        navBar.setupWithNavController(navController)
    }
}

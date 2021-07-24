package com.boulin.eventverse.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.boulin.eventverse.R
import com.boulin.eventverse.data.viewmodels.EventViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Fragment displaying the events page
 */
class EventsFragment : Fragment() {
    private var eventCategory = EventViewModel.EVENT_CATEGORY.COMING_SOON

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // todo : patch le problème de double navigation

        // Gets the event category from the bundle arguments
        arguments?.let { bundle ->
            eventCategory = EventViewModel.EVENT_CATEGORY.values()[bundle.getInt(ARG_EVENT_CATEGORY_TAB)]
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sets up the tab navigation
        val navBar = view.findViewById<BottomNavigationView>(R.id.events_tabs)
        val navController = findNavController(requireActivity(), R.id.fragment_events_gallery)
        val bottomNavConfig = AppBarConfiguration(setOf(
            R.id.navigation_tab_soon,
            R.id.navigation_tab_near,
            R.id.navigation_tab_for_me
        ))
        setupActionBarWithNavController((requireActivity() as AppCompatActivity), navController, bottomNavConfig)
        navBar.setupWithNavController(navController)

        // Redirects to the good fragment
        when(eventCategory) {
            EventViewModel.EVENT_CATEGORY.COMING_SOON -> { navController.navigate(R.id.navigation_tab_soon) }
            EventViewModel.EVENT_CATEGORY.NEAR_ME -> { navController.navigate(R.id.navigation_tab_near) }
            EventViewModel.EVENT_CATEGORY.FOR_ME -> { navController.navigate(R.id.navigation_tab_for_me) }
            else -> {}
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.events_menu_label)
    }

    companion object {
        const val ARG_EVENT_CATEGORY_TAB = "EventsFragment.ARG_EVENT_CATEGORY_TAB"
    }
}
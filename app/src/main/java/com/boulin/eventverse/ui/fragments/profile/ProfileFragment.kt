package com.boulin.eventverse.ui.fragments.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.boulin.eventverse.R
import com.boulin.eventverse.data.viewmodels.UserViewModel
import com.boulin.eventverse.ui.activities.EventFormActivity
import com.boulin.eventverse.ui.activities.ProfileParametersActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hadiyarajesh.flower.Resource
import com.squareup.picasso.Picasso
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Fragment displaying the profile page
 */
class ProfileFragment : Fragment() {
    private lateinit var loading: ProgressBar
    private lateinit var layout: LinearLayout
    private lateinit var error: TextView
    private lateinit var nameTv: TextView
    private lateinit var surnameTv: TextView
    private lateinit var avatarIv: ImageView
    private val userViewModel: UserViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Sets up the view as loading while waiting for data
        loading = view.findViewById(R.id.profile_loading)
        layout = view.findViewById(R.id.profile_layout)
        setLoading(true)

        // Sets up the error layout
        error = view.findViewById(R.id.profile_error)
        setError(null)

        nameTv = view.findViewById(R.id.profile_name)
        surnameTv = view.findViewById(R.id.profile_surname)
        avatarIv = view.findViewById(R.id.profile_avatar)

        return view
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.profile_parameters_icon).setOnClickListener {
            redirectToProfileParameters()
        }

        view.findViewById<FloatingActionButton>(R.id.profile_create_event_button).setOnClickListener{
            redirectToEventCreation()
        }

        val navBar = view.findViewById<BottomNavigationView>(R.id.profile_tabs)
        val navController = Navigation.findNavController(requireActivity(), R.id.fragment_profile_gallery)
        val bottomNavConfig = AppBarConfiguration(setOf(
            R.id.navigation_tab_liked,
            R.id.navigation_tab_created
        ))
        NavigationUI.setupActionBarWithNavController(
            (requireActivity() as AppCompatActivity),
            navController,
            bottomNavConfig
        )
        navBar.setupWithNavController(navController)
        /* Retrieve asynchronously the user data
         * The layout is adapted according to the status of the request : LOADING, SUCCESS or ERROR
         */
        userViewModel.getUser(forceRefresh = false).observe(viewLifecycleOwner) { userResource ->
            when (userResource.status) {
                Resource.Status.LOADING -> {
                    setLoading(true)
                    setError(null)
                }
                Resource.Status.SUCCESS -> {
                    setLoading(false)
                    setError(null)

                    val user = userResource.data

                    if(user != null) {
                        if(!user.isOrganizer) {
                            // Disables the created events tab for non organizer users
                            navBar.findViewById<View>(R.id.navigation_tab_created).visibility = View.GONE
                            navBar.findViewById<View>(R.id.navigation_tab_liked).isClickable = false
                            view.findViewById<FloatingActionButton>(R.id.profile_create_event_button).visibility = View.GONE
                        }

                        nameTv.apply { text = user.name }
                        surnameTv.apply { text = user.surname }

                        if(user.urlPicture != "") {
                            Picasso.get().load(user.urlPicture).into(avatarIv)
                            avatarIv.scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    }
                    else {
                        setError(getString(R.string.error_get_user))
                    }
                }
                Resource.Status.ERROR -> {
                    setLoading(false)
                    setError(userResource.message!!)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.profile_menu_label)
    }

    /** Switches layout based on [load] value */
    private fun setLoading(load: Boolean) {
        if(load) {
            loading.visibility = View.VISIBLE
            setShowLayout(false)
        }
        else {
            loading.visibility = View.GONE
            setShowLayout(true)
        }
    }

    /** Switches layout based on [show] value */
    private fun setShowLayout(show: Boolean) {
        if(show) {
            layout.visibility = View.VISIBLE
        }
        else {
            layout.visibility = View.GONE
        }
    }

    /** Sets the error layout text based on [message] and switches layout */
    private fun setError(message: String?) {
        if(message == null) {
            error.text = ""
            error.visibility = View.GONE
        }
        else {
            error.text = message
            error.visibility = View.VISIBLE
            setShowLayout(false)
        }
    }

    /** Redirects to the activity to manage one's profile settings */
    private fun redirectToProfileParameters() {
        val intent = Intent(requireContext(), ProfileParametersActivity::class.java)
        startActivity(intent)
    }

    private fun redirectToEventCreation(){
        val intent = Intent(requireContext(), EventFormActivity::class.java)
        startActivity(intent)
    }
}
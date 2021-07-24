package com.boulin.eventverse.ui.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.*
import com.boulin.eventverse.R
import androidx.constraintlayout.widget.Group
import com.boulin.eventverse.data.model.User
import com.boulin.eventverse.data.network.interceptors.BearerInterceptor
import com.boulin.eventverse.data.viewmodels.UserViewModel
import com.boulin.eventverse.services.SoonEventService
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.hadiyarajesh.flower.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity loaded by default to start the application, manage authentification with firebase then fetching of the actual user account (or creation) and redirects to the MainActivity
 */
class StartActivity : AppCompatActivity() {

    private lateinit var signInGroup: Group
    private lateinit var loadingGroup: Group
    private lateinit var radioGroup: RadioGroup
    private lateinit var submitButton: Button
    private lateinit var codeEditText: EditText
    private lateinit var errorText : TextView
    private val userViewModel: UserViewModel by viewModel()

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        setContentView(R.layout.activity_start)

        // get element from the view
        signInGroup = findViewById(R.id.signin_group)
        loadingGroup = findViewById(R.id.loading_group)

        radioGroup = findViewById(R.id.signin_radio_group)
        submitButton = findViewById(R.id.signin_submit)
        codeEditText = findViewById(R.id.signin_organizer_code_edit_text)
        errorText = findViewById(R.id.signin_error_text)

        // creates account when clicking on submit button
        submitButton.setOnClickListener(submitCreateAccountListener)

        // hide the signin form and loading screen
        setShowSignInGroup(false)
        setLoading(false)

        // listen for changes in authentification
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    /**
     * Creates account when clicking on submit button
     */
    @ExperimentalCoroutinesApi
    private val submitCreateAccountListener = View.OnClickListener {
        val selectedOption: Int = radioGroup.checkedRadioButtonId

        val isOrganizer: Boolean?
        var organizerCode: String? = null

        // if an option is selected
        if(selectedOption == R.id.signin_organizer_radio || selectedOption == R.id.signin_participant_radio) {
            isOrganizer = selectedOption == R.id.signin_organizer_radio

            if(isOrganizer) organizerCode = codeEditText.text.toString()

            val userInputParams = User.UserInputParams(isOrganizer = isOrganizer, organizerCode = organizerCode)

            // creates the user account from the input parameters
            userViewModel.createUser(userInputParams).observe(this) {
                when (it.status) {
                    Resource.Status.LOADING -> {
                        setLoading(true)
                    }
                    Resource.Status.SUCCESS -> {
                        setLoading(false)

                        val user = it.data

                        // if the user in the backend exists, start the main activity
                        if(user != null) {
                            startMainActivity(user)
                        }
                        // show the fragment to create an account
                        else {
                            showError("Une erreur est survenue pendant la création de votre compte !")
                        }
                    }
                    Resource.Status.ERROR -> {
                        setLoading(false)
                        showError(it.message!!)
                    }
                }
            }
        }
        else {
            showError("Veuillez selectionner une option")
        }
    }

    /**
     * When authentification change, get the user and check things
     */
    @ExperimentalCoroutinesApi
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->

        // get the current user
        val firebaseUser = firebaseAuth.currentUser

        // if user is signed in
        if(firebaseUser != null) {
            // get the token (for request authentification)
            firebaseUser.getIdToken(true)
                .addOnCompleteListener { task ->
                    var error = false

                    if(!task.isSuccessful) error = true
                    else {
                        val token = task.result?.token

                        if(token == null) error = true
                        else {
                            val bearerInterceptor: BearerInterceptor by inject()

                            // set the authentification token (bearer)
                            bearerInterceptor.setToken(token)

                            // try to get the actual user from the backend
                            userViewModel.getUser().observe(this) {
                                when (it.status) {
                                    Resource.Status.LOADING -> {
                                        setLoading(true)
                                    }
                                    Resource.Status.SUCCESS -> {
                                        setLoading(false)

                                        val user = it.data

                                        // if the user in the backend exists, start the main activity
                                        if(user != null) {
                                            startMainActivity(user)
                                        }
                                        // if the user doesn't exists, show the fragment to create an account
                                        else {
                                            setShowSignInGroup(true)
                                        }
                                    }
                                    Resource.Status.ERROR -> {
                                        setLoading(false)

                                        if(it.message!! == NO_ACCOUNT_MSG)
                                            setShowSignInGroup(true) // the user doesn't exist, show the account creation layout
                                        else {
                                            showError(it.message!!)
                                            reload()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if(error) showError("Une erreur est survenue pendant la connexion !")
                }
        }
        // else, login the user with his google account
        else {
            // only provider here is google
            val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

            // start auth UI (automagically manage the authentification)
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
                RC_SIGN_IN)
        }
    }

    /**
     * when auth ui results
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            // if fails, reload activity
            if (resultCode != Activity.RESULT_OK) reload()
        }
    }

    /**
     * Start main activity once the authentification process is finished
     */
    private fun startMainActivity(user: User) {
        // launch the main activity
        Intent(this, MainActivity::class.java).also { activityIntent ->
            startActivity(activityIntent)
        }

        // launch the service
        Intent(this, SoonEventService::class.java).also { serviceIntent ->
            serviceIntent.putExtra(SoonEventService.NAME_EXTRA, user.name)
            serviceIntent.putExtra(SoonEventService.SURNAME_EXTRA, user.surname)
            startService(serviceIntent)
        }

        finish()
    }

    /**
     * show or hide loading screen
     */
    fun setLoading(load: Boolean) {
        if(load) loadingGroup.visibility = View.VISIBLE
        else loadingGroup.visibility = View.GONE
    }

    /**
     * Show an error to the user
     */
    fun showError(message: String) {
        errorText.text = message
    }

    /**
     * Reload the activity (in case of fatal error, just reload it and hope it works)
     */
    private fun reload() {
        finish()
        startActivity(intent)
    }

    /**
     * Show or hide the signin group (form)
     */
    private fun setShowSignInGroup(show: Boolean) {
        if(show) signInGroup.visibility = View.VISIBLE
        else signInGroup.visibility = View.GONE
    }

    companion object {
        const val RC_SIGN_IN = 100001
        const val NO_ACCOUNT_MSG = "Vous devez créer un compte !"
    }
}

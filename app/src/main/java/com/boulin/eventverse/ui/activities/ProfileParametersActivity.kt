package com.boulin.eventverse.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.boulin.eventverse.R
import com.boulin.eventverse.data.model.Location
import com.boulin.eventverse.data.model.User
import com.boulin.eventverse.data.viewmodels.UserViewModel
import com.boulin.eventverse.sensors.LocationSensor
import com.hadiyarajesh.flower.Resource
import com.squareup.picasso.Picasso
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity managing user parameters
 */
class ProfileParametersActivity : AppCompatActivity() {
    lateinit var sensorManager: SensorManager
    lateinit var locationSensor : LocationSensor;

    private var rationalDisplayed = false;

    private lateinit var requestPermisionLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var loading: ProgressBar
    private lateinit var linearLayout: LinearLayout
    private lateinit var error: TextView
    private lateinit var nameEt: EditText
    private lateinit var surnameEt: EditText
    private lateinit var targetSpin: Spinner
    private lateinit var weeksSb: SeekBar
    private lateinit var currentWeeksTv: TextView
    private lateinit var radiusSb: SeekBar
    private lateinit var currentRadiusTv: TextView
    private lateinit var addressNameEt: EditText
    private lateinit var addressLongitudeEt: EditText
    private lateinit var addressLatitudeEt: EditText
    private lateinit var locationButton : ImageButton

    private val userViewModel: UserViewModel by viewModel()

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_parameters)

        // Sets up the view as loading while waiting for data
        loading = findViewById(R.id.profile_parameters_loading)
        linearLayout = findViewById(R.id.profile_parameters_layout)
        setLoading(true)

        // Sets up the error layout
        error = findViewById(R.id.profile_parameters_error)
        setError(null)

        addressNameEt = findViewById(R.id.paramLocationName)
        addressLongitudeEt = findViewById(R.id.paramLongitude)
        addressLatitudeEt = findViewById(R.id.paramLatitude)
        locationButton = findViewById(R.id.locationButton)
        nameEt = findViewById(R.id.profile_parameters_name)
        surnameEt = findViewById(R.id.profile_parameters_surname)
        targetSpin = findViewById(R.id.parameters_target)
        weeksSb = findViewById(R.id.time_before_event)
        radiusSb = findViewById(R.id.distance_to_event)
        currentWeeksTv = findViewById(R.id.time_before_event_current_value)
        currentRadiusTv = findViewById(R.id.distance_to_event_current_value)

        findViewById<Button>(R.id.parameters_commit_button).setOnClickListener { _ ->
            updateUser()
        }

        /* Retrieves asynchronously the user data
         * The layout is adapted according to the status of the request : LOADING, SUCCESS or ERROR
         */
        userViewModel.getUser(forceRefresh = false).observe(this) { userResource ->
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
                        fillViewValues(user)
                    }
                    else {
                        setError("Échec de la récupération des données de l'utilisateur.")
                    }
                }
                Resource.Status.ERROR -> {
                    setLoading(false)
                    setError(userResource.message!!)
                }
            }
        }
        requestPermisionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        {
                result: Map<String, Boolean> ->
            if(result[Manifest.permission.ACCESS_FINE_LOCATION]!! && result[Manifest.permission.ACCESS_COARSE_LOCATION]!!)
            {
                locationSensor.update()
            }else{
                Toast.makeText(this, "Tu te rapproches ?", Toast.LENGTH_SHORT).show()
            }
        }

        locationButton.setOnClickListener{
            askForLocationPermission()
        }

        locationSensor = LocationSensor(this)
        locationSensor.onValueChanged { loc ->
            addressNameEt.setText("Votre Position")
            addressLatitudeEt.setText(loc.latitude.toString())
            addressLongitudeEt.setText(loc.longitude.toString())
        }
    }

    override fun onStart() {
        super.onStart()
        supportActionBar?.title = getString(R.string.profile_parameters_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Allows backtracking from the appbar
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** Populates the view with data from specified [user] */
    private fun fillViewValues(user: User) {
        // Populates the view elements with their respective data
        nameEt.apply { setText(user.name) }
        surnameEt.apply { setText(user.surname) }
        addressNameEt.setText(user.preferences.location.name)
        addressLatitudeEt.setText(user.preferences.location.latitude.toString())
        addressLongitudeEt.setText(user.preferences.location.longitude.toString())
        if(user.urlPicture != "") {
            val avatarIv = findViewById<ImageView>(R.id.parameters_profil_avatar)
            Picasso.get().load(user.urlPicture).into(avatarIv)
            avatarIv.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        // Initializes the seekbars for week and radius selection
        weeksSb.apply { progress = user.preferences.weeks }
        weeksSb.setOnSeekBarChangeListener(generateSeekBarChangeListenerObject(currentWeeksTv))
        weeksSb.post {
            // Initializes the current value indicator of the seekbar after the latter creation
            val value = (weeksSb.progress * (weeksSb.width - 2 * weeksSb.thumbOffset)) / weeksSb.max
            currentWeeksTv.apply {
                text = weeksSb.progress.toString()
                x = weeksSb.x + value - weeksSb.thumbOffset / 2
            }
        }
        radiusSb.apply { progress = user.preferences.radius.toInt() }
        radiusSb.setOnSeekBarChangeListener(generateSeekBarChangeListenerObject(currentRadiusTv))
        radiusSb.post {
            // Initializes the current value indicator of the seekbar after the latter creation
            val value = (radiusSb.progress * (radiusSb.width - 2 * radiusSb.thumbOffset)) / radiusSb.max
            currentRadiusTv.apply {
                text = radiusSb.progress.toString()
                x = radiusSb.x + value - radiusSb.thumbOffset / 2
            }
        }

        // Initializes the spinner of the audience target
        val targetAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            User.TargetAudience.values()
        )
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        targetSpin.adapter = targetAdapter
        targetSpin.onItemSelectedListener = object :
            OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) { }
                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }
        targetSpin.setSelection(user.preferences.target.ordinal)
    }

    /** Returns a seekbar change listener object allowing the [valueTv] 
     * to keep track of the seekbar progress 
     */
    private fun generateSeekBarChangeListenerObject(valueTv: TextView): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val value = (progress * (seekBar.width - 2 * seekBar.thumbOffset)) / seekBar.max
                valueTv.apply {
                    text = progress.toString()
                    x = seekBar.x + value - seekBar.thumbOffset / 2
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        }
    }

    /** Retrieves asynchronously the user current data and updates them
     * The layout is also adapted according to the status of the request : SUCCESS or ERROR
     */
    @ExperimentalCoroutinesApi
    private fun updateUser() {
        userViewModel.getUser(forceRefresh = false).observe(this) { userResource ->
            when (userResource.status) {
                Resource.Status.SUCCESS -> {
                    setLoading(false)
                    setError(null)

                    val user = userResource.data

                    if(user != null) {
                        sendUpdateToAPI()
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.parameters_commit_error_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                Resource.Status.ERROR -> {
                    Toast.makeText(
                        this,
                        getString(R.string.parameters_commit_error_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }
    }

    /** Sends modification call for the user data to the API */
    @ExperimentalCoroutinesApi
    private fun sendUpdateToAPI() {
        val userPreferences = User.UserPreferences(
            radius = radiusSb.progress.toFloat(),
            weeks = weeksSb.progress,
            location = Location(
                name = addressNameEt.text.toString(),
                latitude = addressLatitudeEt.text.toString().toDouble(),
                longitude = addressLongitudeEt.text.toString().toDouble()
            ),
            target = targetSpin.selectedItem as User.TargetAudience
        )
        val userMutableParams = User.UserMutableParams(
            name = nameEt.text.toString(),
            surname = surnameEt.text.toString(),
            preferences = userPreferences
        )

        /* The layout is adapted according to the status of the request : LOADING, SUCCESS or ERROR */
        userViewModel.updateUser(userMutableParams).observe(this) { userResource ->
            when (userResource.status) {
                Resource.Status.LOADING -> {
                    loading.visibility = View.VISIBLE
                }
                Resource.Status.SUCCESS -> {
                    Toast.makeText(this, getString(R.string.parameters_commit_success_msg), Toast.LENGTH_SHORT).show()
                    finish()
                    loading.visibility = View.GONE
                }
                Resource.Status.ERROR -> {
                    Toast.makeText(this, getString(R.string.parameters_commit_error_msg), Toast.LENGTH_SHORT).show()
                    loading.visibility = View.GONE
                }
            }
        }
    }

    /** Switches layout based on [load] value */
    private fun setLoading(load: Boolean) {
        if(load) {
            loading.visibility = View.VISIBLE
            setShowLinearLayout(false)
        }
        else {
            loading.visibility = View.GONE
            setShowLinearLayout(true)
        }
    }

    /** Switches layout based on [show] value */
    private fun setShowLinearLayout(show: Boolean) {
        if(show) {
            linearLayout.visibility = View.VISIBLE
        }
        else {
            linearLayout.visibility = View.GONE
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
            setShowLinearLayout(false)
        }
    }

    private fun askForLocationPermission(){
        when{
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ->
            {
                locationSensor.update()
            }
            (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                    && !rationalDisplayed -> {
                Toast.makeText(this, "I need the location", Toast.LENGTH_SHORT).show()
                rationalDisplayed = true
            }
            else -> {
                requestPermisionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION))
                rationalDisplayed = false
            }
        }
    }
}

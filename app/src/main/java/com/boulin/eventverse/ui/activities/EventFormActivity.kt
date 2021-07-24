package com.boulin.eventverse.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.SensorManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.boulin.eventverse.R
import com.boulin.eventverse.data.model.Event
import com.boulin.eventverse.data.model.Location
import com.boulin.eventverse.data.model.User
import com.boulin.eventverse.data.viewmodels.EventViewModel
import com.boulin.eventverse.sensors.LocationSensor
import com.hadiyarajesh.flower.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DateFormat.getDateInstance
import java.text.DateFormat.getTimeInstance
import java.text.SimpleDateFormat
import java.util.*


class EventFormActivity
    : AppCompatActivity() {
    @SuppressLint("SimpleDateFormat")
    private val dateFormat : SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.CANADA)

    private var eventId: String? = null
    private lateinit var eventName: EditText
    private lateinit var eventStartDate: EditText
    private lateinit var eventStartTime: EditText
    private lateinit var eventEndDate: EditText
    private lateinit var eventEndTime: EditText
    private lateinit var eventLocationName: EditText
    private lateinit var eventLocationLongitude: EditText
    private lateinit var eventLocationLatitude: EditText
    private lateinit var eventTarget: Spinner
    private lateinit var eventDescription: EditText
    private lateinit var locationButton : ImageButton
    private lateinit var chooseFromGalleryButton: Button
    private lateinit var takePictureButton: Button
    private lateinit var imageCover: ImageView

    private val eventViewModel: EventViewModel by viewModel()

    lateinit var sensorManager: SensorManager
    lateinit var locationSensor : LocationSensor;

    private var rationalDisplayed = false;

    private lateinit var requestPermissionLauncherLocation: ActivityResultLauncher<Array<String>>
    private lateinit var requestPermissionLauncherCamera: ActivityResultLauncher<Array<String>>

    private val pickImageRequest = 22
    private val requestImageCapture = 1

    companion object {
        const val EXTRA_EVENT_FORM_DATA = "com.boulin.eventverse.ui.activities.EVENT_FORM_DATA"
    }

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_form)

        eventId = intent.getStringExtra(EXTRA_EVENT_FORM_DATA)

        eventName = findViewById(R.id.eventNameInput)
        eventStartDate = findViewById(R.id.eventStartDateInput)
        eventStartTime = findViewById(R.id.eventStartTimeInput)
        eventEndDate = findViewById(R.id.eventEndDateInput)
        eventEndTime = findViewById(R.id.eventEndTimeInput)
        eventLocationName = findViewById(R.id.eventLocationName)
        eventLocationLongitude = findViewById(R.id.eventLocationLongitude)
        eventLocationLatitude = findViewById(R.id.eventLocationLatitude)
        locationButton = findViewById(R.id.locationButton)
        eventTarget = findViewById(R.id.eventTargetInput)
        eventDescription = findViewById(R.id.eventDescriptionInput)
        chooseFromGalleryButton = findViewById(R.id.addPicture)
        takePictureButton = findViewById(R.id.takePicture)
        imageCover = findViewById(R.id.eventPicture)

        val eventValidateButton = findViewById<Button>(R.id.validateButton)

        eventStartDate.displayDatePicker(this)
        eventStartTime .displayTimePicker(this)

        eventEndDate.displayDatePicker(this)
        eventEndTime.displayTimePicker(this)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, User.TargetAudience.values())
        eventTarget.adapter = adapter

        eventTarget.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        if(eventId != null){
            eventViewModel.getEventById(eventId!!).observe(this) { eventResource ->
                when (eventResource.status) {
                    Resource.Status.LOADING -> {
                        //setLoading(true)
                    }
                    Resource.Status.SUCCESS -> {
                        //setLoading(false)

                        val event = eventResource.data
                        Log.d("debug", ""+eventId+": "+event?.id)
                        if(event != null) {
                            updateView(event)
                            supportActionBar?.apply { title = getString(R.string.modify_event_title) }
                            eventValidateButton.apply { text = getString(R.string.modify_event_button_label) }
                            eventValidateButton.setOnClickListener {
                                val target = User.TargetAudience.valueOf(eventTarget.selectedItem.toString())
                                val location = Location(eventLocationName.text.toString(), eventLocationLongitude.text.toString().toDouble(), eventLocationLatitude.text.toString().toDouble())
                                sendEventUpdateToAPI(event.id, eventName.text.toString(), getDateFromDateTimeEditText(eventStartDate, eventStartTime) ?: event.startDate, getDateFromDateTimeEditText(eventEndDate, eventEndTime) ?: event.endDate, eventDescription.text.toString(), location, target)
                            }
                        }
                    }
                    Resource.Status.ERROR -> {
                        //setLoading(false)
                        //setError(eventResource.message!!)
                    }
                }
            }
        }else {
            supportActionBar?.apply { title = getString(R.string.create_event_title) }
            eventValidateButton.apply { text = getString(R.string.create_event_button_label) }
            eventValidateButton.setOnClickListener{
                val start = getDateFromDateTimeEditText(eventStartDate, eventStartTime) ?: Date()
                val end = getDateFromDateTimeEditText(eventEndDate, eventEndTime) ?: Date()

                val target = User.TargetAudience.valueOf(eventTarget.selectedItem.toString())

                val location = Location(eventLocationName.text.toString(), eventLocationLongitude.text.toString().toDouble(), eventLocationLatitude.text.toString().toDouble())
                addEvent( eventName.text.toString(), start, end, eventDescription.text.toString(), location, target)
            }
        }

        requestPermissionLauncherLocation = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        {
                result: Map<String, Boolean> ->
            if(result[Manifest.permission.ACCESS_FINE_LOCATION]!! && result[Manifest.permission.ACCESS_COARSE_LOCATION]!!)
            {
                locationSensor.update()
            }
        }

        requestPermissionLauncherCamera = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        {
                result: Map<String, Boolean> ->
            if(result[Manifest.permission.READ_EXTERNAL_STORAGE]!!) {
                selectImage()
            }
            if(result[Manifest.permission.CAMERA]!!)
            {
                takePicture()
            }
        }

        locationButton.setOnClickListener{
            askForLocationPermission()
        }

        locationSensor = LocationSensor(this)
        locationSensor.onValueChanged { loc ->
            eventLocationName.setText("Votre Position")
            eventLocationLatitude.setText(loc.latitude.toString())
            eventLocationLongitude.setText(loc.longitude.toString())
        }

        chooseFromGalleryButton.setOnClickListener { selectImage() }
        takePictureButton.setOnClickListener { takePicture() }
    }

    /** Opens on intent allowing image selection from phone gallery */
    private fun selectImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
                // Defining Implicit Intent to mobile gallery
                val selectImageIntent = Intent()
                selectImageIntent.type = "image/*"
                selectImageIntent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    selectImageIntent,
                    pickImageRequest
                )
        } else {
            requestPermissionLauncherCamera.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    /** Opens on intent allowing camera shot from phone gallery */
    private fun takePicture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(takePictureIntent, requestImageCapture)
        } else {
            requestPermissionLauncherCamera.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // TODO : Ajouter la sauvegarde dans Firebase
        /* To save the image to local gallery (if we wish to do so for any reason),
           check WRITE_EXTERNAL_STORAGE permission */
        if (requestCode == pickImageRequest && resultCode == RESULT_OK &&
            data != null && data.data != null) {
                val filePath = data.data
                val bitmap = MediaStore.Images.Media
                    .getBitmap(
                        contentResolver,
                        filePath
                    )
                imageCover.setImageBitmap(bitmap)
        }
        if (requestCode == requestImageCapture && resultCode == RESULT_OK && data != null) {
            val imageBitmap = data.extras!!.get("data") as Bitmap
            imageCover.setImageBitmap(imageBitmap)
        }
    }

    private fun getDateFromDateTimeEditText(dateEditText: EditText, timeEditText: EditText): Date? {
        val date = getDateInstance().parse(dateEditText.text.toString())
        val time = getTimeInstance().parse(timeEditText.text.toString())

        return if(date != null && time != null) {
            Date().apply {
                this.year = date.year
                this.month = date.month
                this.date = date.date
                this.hours = time.hours
                this.minutes = time.minutes
                this.seconds = time.seconds
            }
        } else null
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
                requestPermissionLauncherLocation.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION))
                rationalDisplayed = false
            }
        }
    }


    override fun onStart() {
        super.onStart()
        // TODO : externalise string
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        locationSensor.register(this)
    }

    override fun onPause() {
        super.onPause()
        locationSensor.unregister(this)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // exemple de création d'event
    @ExperimentalCoroutinesApi
    fun addEvent(eventTitle: String, eventStart: Date, eventEnd: Date, eventDescription: String, location:Location, target: User.TargetAudience) {
        val eventInputParams = Event.EventInputParams(
            eventTitle,
            eventDescription,
            eventStart,
            eventEnd,
            location,
            target
        )

        eventViewModel.createEvent(eventInputParams).observe(this) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    setLoading(true)
                }
                Resource.Status.SUCCESS -> {
                    setLoading(false)

                    val event = it.data

                    if (event != null) {
                        finish()
                    } else {
                        showMessageToast("Échec de la création de l'event")
                    }
                }
                Resource.Status.ERROR -> {
                    setLoading(false)
                    showMessageToast(it.message!!)
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun updateView(event: Event?) {
        eventName.setText(event?.title)
        eventStartDate.setText(getDateInstance().format(event?.startDate))
        eventStartTime.setText(getTimeInstance().format(event?.startDate))
        eventEndDate.setText(getDateInstance().format(event?.endDate))
        eventEndTime.setText(getTimeInstance().format(event?.endDate))
        eventLocationName.setText(event?.location?.name)
        eventLocationLongitude.setText(event?.location?.longitude.toString())
        eventLocationLatitude.setText(event?.location?.latitude.toString())
        event?.target?.let { eventTarget.setSelection(it.ordinal) }
        eventDescription.setText(event?.description)
    }

    @ExperimentalCoroutinesApi
    private fun sendEventUpdateToAPI(eventId: String, eventTitle: String, eventStart: Date, eventEnd: Date, eventDescription: String, location:Location, target: User.TargetAudience) {
        val eventInputParams = Event.EventInputParams(
            eventTitle,
            eventDescription,
            eventStart,
            eventEnd,
            location,
            target
        )
        eventViewModel.updateEvent(eventId, eventInputParams).observe(this) { resourceEvent ->
            when (resourceEvent.status) {
                Resource.Status.LOADING -> {
                    setLoading(true)
                }
                Resource.Status.SUCCESS -> {
                    setLoading(false)

                    val eventReturned = resourceEvent.data

                    if (eventReturned != null) {
                        finish()
                    } else {
                        showMessageToast("Échec de la création de l'event")
                    }
                }
                Resource.Status.ERROR -> {
                    setLoading(false)
                    showMessageToast(resourceEvent.message!!)
                }
            }
        }
    }



    fun setLoading(load: Boolean) {
        // todo : chargement

        if(load) {
            Log.d(this.javaClass.name, "Début du chargement")
        }
        else {
            Log.d(this.javaClass.name, "Fin du chargement")
        }
    }

    fun showMessageToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun EditText.displayDatePicker(context: Context, maxDate:Date? = null){
        isFocusableInTouchMode = false
        isClickable = true
        isFocusable = false

        val myCalendar = Calendar.getInstance()
        val datePickerOnDataSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                setText(getDateInstance().format(myCalendar.time))
            }

        setOnClickListener {
            DatePickerDialog(
                context, datePickerOnDataSetListener, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).run {
                maxDate?.time?.also { datePicker.maxDate = it }
                show()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun EditText.displayTimePicker(context: Context){
        isFocusableInTouchMode = false
        isClickable = true
        isFocusable = false

        val myCalendar = Calendar.getInstance()
        val timePickerOnDataSetListener =
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                myCalendar.set(Calendar.HOUR_OF_DAY, hour)
                myCalendar.set(Calendar.MINUTE, minute)
                setText(getTimeInstance().format(myCalendar.time))
            }

        setOnClickListener {
            TimePickerDialog(
                context, timePickerOnDataSetListener, myCalendar
                    .get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true
            ).run {
                show()
            }
        }
    }
}
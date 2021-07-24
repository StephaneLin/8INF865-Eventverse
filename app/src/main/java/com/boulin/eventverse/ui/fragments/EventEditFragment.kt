package com.boulin.eventverse.ui.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.boulin.eventverse.R
import com.boulin.eventverse.data.model.Event
import com.boulin.eventverse.data.model.Location
import com.boulin.eventverse.data.model.User
import com.boulin.eventverse.data.viewmodels.EventViewModel
import com.hadiyarajesh.flower.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class EventEditFragment : Fragment() {

    private var eventId: String? = null

    @SuppressLint("SimpleDateFormat")
    private val dateFormat : SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.CANADA)

    private lateinit var loading: ProgressBar
    private lateinit var scrollview: ScrollView
    private lateinit var error: TextView

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
    private val eventViewModel: EventViewModel by viewModel()

    @ExperimentalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_event_edit, container, false)

        loading = view.findViewById(R.id.event_details_loading)
        scrollview = view.findViewById(R.id.event_details_scrollview)
        setLoading(true)

        // set error to false
        error = view.findViewById(R.id.event_details_error)
        setError(null)

        eventViewModel.getEventById(eventId!!).observe(viewLifecycleOwner) { eventResource ->
            when (eventResource.status) {
                Resource.Status.LOADING -> {
                    setLoading(true)
                }
                Resource.Status.SUCCESS -> {
                    setLoading(false)

                    val event = eventResource.data
                    retrieveData(event)

                    if(event == null) {
                        showMessageToast("Échec lors de la récupération des détails de l'événement $eventId.")
                    }
                }
                Resource.Status.ERROR -> {
                    setLoading(false)
                    setError(eventResource.message!!)
                }
            }
        }

        eventStartDate.displayDatePicker(requireContext(), "dd-MM-yyyy")
        eventStartTime .displayTimePicker(requireContext(), "HH:mm")

        eventEndDate.displayDatePicker(requireContext(), "dd-MM-yyyy")
        eventEndTime.displayTimePicker(requireContext(), "HH:mm")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, User.TargetAudience.values())
        eventTarget.adapter = adapter

        eventTarget.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        val eventEdit = view.findViewById<Button>(R.id.validateButton)
        eventEdit.setOnClickListener{
                button ->
            val dateStartString = eventStartDate.text.toString()+" "+eventStartTime.text.toString()
            val start = dateFormat.parse(dateStartString)

            val dateEndString = eventEndDate.text.toString()+" "+eventEndTime.text.toString()
            val end= dateFormat.parse(dateEndString)

            val target = User.TargetAudience.valueOf(eventTarget.getSelectedItem().toString())

            val location = Location(eventLocationName.text.toString(), eventLocationLongitude.text.toString().toDouble(), eventLocationLatitude.text.toString().toDouble())
            eventId?.let { editEvent(it, eventName.text.toString(), start, end, eventDescription.text.toString(), location, target) }
        }
        return view
    }

    private fun retrieveData(event: Event?) {
        view?.findViewById<EditText>(R.id.eventNameInput)?.setText(event?.title)
        view?.findViewById<EditText>(R.id.eventLocationName)?.setText(event?.location?.name)
        view?.findViewById<EditText>(R.id.eventLocationLongitude)?.setText(event?.location?.longitude?.toString())
        view?.findViewById<EditText>(R.id.eventLocationLatitude)?.setText(event?.location?.latitude?.toString())
        view?.findViewById<EditText>(R.id.eventTargetInput)?.setText(event?.target.toString())
        view?.findViewById<EditText>(R.id.eventDescriptionInput)?.setText(event?.description)
    }

    override fun onStart() {
        super.onStart()
        // TODO : externalise string
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Création d'événement"
    }

    // mis à jour d'un evenement
    @ExperimentalCoroutinesApi
    fun editEvent(eventId: String, eventTitle: String, eventStart: Date, eventEnd: Date, eventDescription: String, location:Location, target: User.TargetAudience) {
        val eventInputParams = Event.EventInputParams(
            eventTitle,
            eventDescription,
            eventStart,
            eventEnd,
            location,
            target
        )

        eventViewModel.updateEvent(eventId, eventInputParams).observe(viewLifecycleOwner) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    setLoading(true)
                }
                Resource.Status.SUCCESS -> {
                    setLoading(false)

                    val event = it.data

                    if (event != null) {
                        activity?.supportFragmentManager?.popBackStackImmediate()
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

    private fun setError(message: String?) {
        if(message == null) {
            error.text = ""
            error.visibility = View.GONE
        }
        else {
            error.text = message
            error.visibility = View.VISIBLE
            setShowScrollView(false)
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
    private fun setShowScrollView(show: Boolean) {
        if(show) {
            scrollview.visibility = View.VISIBLE
        }
        else {
            scrollview.visibility = View.GONE
        }
    }

    fun showMessageToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    //todo: Corriger position FloatingButton
    fun EditText.displayDatePicker(context: Context, format:String, maxDate:Date? = null){
        isFocusableInTouchMode = false
        isClickable = true
        isFocusable = false

        val myCalendar = Calendar.getInstance()
        val datePickerOnDataSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(format, Locale.CANADA)
                setText(sdf.format(myCalendar.time))
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
    fun EditText.displayTimePicker(context: Context, format: String){
        isFocusableInTouchMode = false
        isClickable = true
        isFocusable = false

        val myCalendar = Calendar.getInstance()
        val timePickerOnDataSetListener =
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                myCalendar.set(Calendar.HOUR_OF_DAY, hour)
                myCalendar.set(Calendar.MINUTE, minute)
                val sdf = SimpleDateFormat(format)
                setText(sdf.format(myCalendar.time))
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

    private fun getConcatFormattedHours(startDate: Date, endDate: Date): CharSequence {
        return formatDate(startDate, DateFormat.getTimeInstance()) +
                " - " +
                formatDate(endDate, DateFormat.getTimeInstance())
    }

    private fun getFormattedDates(startDate: Date, endDate: Date): Pair<CharSequence, CharSequence> {
        /* If the event starts and ends the same day,
         *  -> print date on the first line and the time (start - end) on the second
         * Else
         *  -> print start datetime on the first line and end datetime on the second
         */
        val startDateWithoutTime = formatDate(startDate, DateFormat.getDateInstance())
        val endDateWithoutTime = formatDate(endDate, DateFormat.getDateInstance())
        return when (startDateWithoutTime == endDateWithoutTime) {
            true -> {
                Pair(startDateWithoutTime, getConcatFormattedHours(startDate, endDate))
            }
            else -> {
                Pair(formatDate(startDate, DateFormat.getDateTimeInstance()), formatDate(endDate,
                    DateFormat.getDateTimeInstance()
                ))
            }
        }
    }

    private fun formatDate(date: Date, formatter: DateFormat): String {
        return formatter.format(date)
    }
}
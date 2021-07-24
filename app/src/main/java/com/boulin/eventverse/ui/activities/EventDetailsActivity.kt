package com.boulin.eventverse.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.boulin.eventverse.R
import com.boulin.eventverse.data.model.Event
import com.boulin.eventverse.data.viewmodels.EventViewModel
import com.boulin.eventverse.data.viewmodels.UserViewModel
import com.hadiyarajesh.flower.Resource
import com.squareup.picasso.Picasso
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DateFormat
import java.text.DateFormat.*
import java.util.*

// TODO Symph : Remplacer l'image verte par le Qr code de l'event
// TODO Symph : Modifier l'affichage du public (Traduction de ALL, TEENAGERS etc.)

/**
 * Activity displaying all the details of a given event
 */
class EventDetailsActivity : AppCompatActivity() {
    private var eventId: String? = null
    private lateinit var loading: ProgressBar
    private lateinit var scrollView: ScrollView
    private lateinit var error: TextView
    private lateinit var likeIcon: AppCompatImageView
    private val eventViewModel: EventViewModel by viewModel()
    private val userViewModel: UserViewModel by viewModel()

    companion object {
        const val EXTRA_EVENT_ID = "com.boulin.eventverse.ui.activities.DETAILS_EVENT_ID"
    }

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        eventId = intent.getStringExtra(EXTRA_EVENT_ID)
        likeIcon = findViewById(R.id.like_icon)

        // Sets up the view as loading while waiting for data
        loading = findViewById(R.id.event_details_loading)
        scrollView = findViewById(R.id.event_details_scrollview)
        setLoading(true)

        // Sets up the error layout
        error = findViewById(R.id.event_details_error)
        setError(null)

        /* Retrieves asynchronously the event data by its id (given as extra to the intent)
         * The layout is adapted according to the status of the request : LOADING, SUCCESS or ERROR
         */
        eventViewModel.getEventById(eventId!!).observe(this) { eventResource ->
            when (eventResource.status) {
                Resource.Status.LOADING -> {
                    setLoading(true)
                }
                Resource.Status.SUCCESS -> {
                    setLoading(false)

                    val event = eventResource.data

                    if(event == null) {
                        showMessageToast(
                            getString(R.string.error_get_event, eventId)
                        )
                    } else {
                        fillViewValues(event)
                    }
                }
                Resource.Status.ERROR -> {
                    setLoading(false)
                    setError(eventResource.message!!)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        supportActionBar?.title = getString(R.string.event_details_title)
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

    /** Populates the view with data from specified [event] */
    @ExperimentalCoroutinesApi
    private fun fillViewValues(event: Event) {
        // Populates the view elements with their respective data
        val startDate = event.startDate
        val endDate = event.endDate
        val (dateText, hourText) = getFormattedDates(startDate, endDate)
        findViewById<TextView>(R.id.title_tv).apply { text = event.title }
        findViewById<TextView>(R.id.date_tv).apply { text = dateText }
        findViewById<TextView>(R.id.hour_tv).apply { text = hourText }
        findViewById<TextView>(R.id.adress_tv).apply { text = event.location.name }
        findViewById<TextView>(R.id.public_tv).apply { text = event.target.toString() }
        findViewById<TextView>(R.id.description_tv).apply { text = event.description }
        val cover = findViewById<ImageView>(R.id.event_details_cover)
        if(event.cover != "") {
            Picasso.get().load(event.cover).into(cover)
            cover.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        /* Retrieve asynchronously the user data
         * The layout is adapted according to the status of the request : SUCCESS or ERROR
         */
        userViewModel.getUser(forceRefresh = false).observe(this) { userResource ->
            when (userResource.status) {
                Resource.Status.SUCCESS -> {
                    setLoading(false)
                    setError(null)

                    val user = userResource.data

                    if(user != null) {
                        // Set up the like icon
                        setLikeIcon(event.liked.contains(user.uid))
                        likeIcon.setOnClickListener {
                            updateEventLike(user.uid, event.liked)
                        }
                    }
                }
                Resource.Status.ERROR -> {
                    showMessageToast(
                        getString(R.string.error_get_user),
                    )
                }
                else -> { }
            }
        }
    }

    /** Sets the correct image according to [isLiked] value */
    private fun setLikeIcon(isLiked: Boolean) {
        if(isLiked) {
            likeIcon.setImageResource(R.drawable.ic_liked)
        } else {
            likeIcon.setImageResource(R.drawable.ic_unliked)
        }
    }

    /** Updates the event like list [liked] by adding / removing the user like based on his [uid] */
    private fun updateEventLike(uid: String, liked: List<String>?) {
        val mutableLiked: MutableList<String> = liked as MutableList<String>
        val isLiked = mutableLiked.contains(uid)
        if(isLiked) {
            mutableLiked.remove(uid)
        } else {
            mutableLiked.add(uid)
        }
        // TODO Symph : Pour Antoine Bouabana : update le like de l'event dans l'API...
        setLikeIcon(!isLiked)
    }

    /** Shows given [message] as a toast */
    private fun showMessageToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    /** Switches layout based on [load] value */
    private fun setLoading(load: Boolean) {
        if(load) {
            loading.visibility = View.VISIBLE
            setShowScrollView(false)
        }
        else {
            loading.visibility = View.GONE
            setShowScrollView(true)
        }
    }

    /** Switches layout based on [show] value */
    private fun setShowScrollView(show: Boolean) {
        if(show) {
            scrollView.visibility = View.VISIBLE
        }
        else {
            scrollView.visibility = View.GONE
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
            setShowScrollView(false)
        }
    }

    /** Returns the concatenated timestamps of [startDate] and [endDate] */
    private fun getConcatFormattedHours(startDate: Date, endDate: Date): CharSequence {
        return formatDate(startDate, getTimeInstance()) +
                " - " +
                formatDate(endDate, getTimeInstance())
    }

    /** Returns the pair of lines of the concatenation of [startDate] and [endDate] dates  */
    private fun getFormattedDates(
        startDate: Date,
        endDate: Date
    ): Pair<CharSequence, CharSequence> {
        val startDateWithoutTime = formatDate(startDate, getDateInstance())
        val endDateWithoutTime = formatDate(endDate, getDateInstance())

        return if(startDateWithoutTime == endDateWithoutTime) {
            // If the event starts and ends the same day ->
                // print date on the first line and the time (start - end) on the second
            Pair(
                startDateWithoutTime,
                getConcatFormattedHours(startDate, endDate)
            )
        } else {
            // Else -> print start datetime on the first line and end datetime on the second
            Pair(
                formatDate(startDate, getDateTimeInstance()),
                formatDate(endDate, getDateTimeInstance())
            )
        }
    }

    /** Returns the formatted date according to the specified [date] and [formatter] */
    private fun formatDate(date: Date, formatter: DateFormat): String {
        return formatter.format(date)
    }
}

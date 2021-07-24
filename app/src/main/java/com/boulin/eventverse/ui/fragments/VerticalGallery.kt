package com.boulin.eventverse.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.boulin.eventverse.R
import com.boulin.eventverse.adapters.EventAdapter
import com.boulin.eventverse.data.model.Event
import com.boulin.eventverse.data.model.User
import com.boulin.eventverse.data.viewmodels.EventViewModel
import com.boulin.eventverse.data.viewmodels.UserViewModel
import com.boulin.eventverse.ui.activities.EventFormActivity
import com.hadiyarajesh.flower.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * Fragment displaying a gallery of events displayed Vertically
 */
class VerticalGallery : Fragment() {
    private lateinit var eventCategory: EventViewModel.EVENT_CATEGORY
    private lateinit var eventsAdapter: EventAdapter
    private lateinit var loading: ProgressBar
    private lateinit var recycler: RecyclerView
    private lateinit var error: TextView
    private lateinit var empty: TextView

    private val eventViewModel: EventViewModel by viewModel()
    private val userViewModel: UserViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eventCategory = getEventCategoryFromFragmentId(id)
    }

    @ExperimentalCoroutinesApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_vertical_gallery, container, false)

        // Sets up the view as loading while waiting for data
        loading = view.findViewById(R.id.vgallery_loading)
        recycler = view.findViewById(R.id.gallery_events)
        setLoading(true)

        // Sets up the error layout
        error = view.findViewById(R.id.vgallery_error)
        setError(null)

        empty = view.findViewById(R.id.vgallery_empty)
        val isOrganizerCreatedEventsFragment = id == R.id.fragment_created_profile_vgallery

        // Creates and assigns a new event adapter
        eventsAdapter = EventAdapter(showActions = isOrganizerCreatedEventsFragment)
        recycler.adapter = eventsAdapter

        // If the fragment is the created fragment of an organizer, adds listeners for edit and delete actions
        if(isOrganizerCreatedEventsFragment) {
            eventsAdapter.onEditButtonClick = { event ->
                    val intent = Intent(requireContext(), EventFormActivity::class.java).apply{
                        putExtra(EventFormActivity.EXTRA_EVENT_FORM_DATA, event.id)
                    }
                    startActivity(intent)
            }

            eventsAdapter.onDeleteButtonClick = { event ->
                createDeleteAlertDialog(event.id, event.title)
            }
        }

        // Initializes recyclerview
        initRecyclerView()

        return view
    }

    /** Gets the user and call initEventsFromUser */
    @ExperimentalCoroutinesApi
    private fun initRecyclerView() {
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
                        /* Retrieve asynchronously the list of events according to the user
                         * The layout is adapted according to the status of the request : LOADING, SUCCESS or ERROR
                         */
                        getLiveDataFromUser(user).observe(viewLifecycleOwner) { eventsResource ->
                            when (eventsResource.status) {
                                Resource.Status.LOADING -> {
                                    setLoading(true)
                                    setError(null)
                                }
                                Resource.Status.SUCCESS -> {
                                    setLoading(false)
                                    setError(null)

                                    val events = eventsResource.data

                                    if(events != null) {
                                        eventsAdapter.updateData(events)
                                        setEmpty(events.isEmpty())
                                    }
                                    else {
                                        setError(getString(
                                            R.string.error_get_events_category,
                                            eventCategory.name
                                        ))
                                    }
                                }
                                Resource.Status.ERROR -> {
                                    setLoading(false)
                                    setError(eventsResource.message!!)
                                }
                            }
                        }
                    }
                    else {
                        setError(getString(
                            R.string.error_get_events_category,
                            eventCategory.name
                        ))
                    }
                }
                Resource.Status.ERROR -> {
                    setLoading(false)
                    setError(userResource.message!!)
                }
            }
        }
    }

    /** Generates an AlertDialog deleting the event with the id [eventId]
     *  in case of user confirmation
     */
    @ExperimentalCoroutinesApi
    private fun createDeleteAlertDialog(eventId: String, eventTitle: String) {
        val title = SpannableString(getString(R.string.delete_alert_dialog_title))
            .apply { setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue)),
                0,
                this.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )}
        val deleteDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(getString(R.string.delete_alert_dialog_message, eventId, eventTitle))
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(R.string.delete_alert_dialog_positive_button) { dialog, _ ->
                eventViewModel.deleteEvent(eventId).observe(viewLifecycleOwner) { eventResource ->
                    when (eventResource.status) {
                        Resource.Status.SUCCESS -> {
                            val event = eventResource.data

                            if (event == null) {
                                setError(
                                    getString(
                                        R.string.error_get_events_category,
                                        eventCategory.name
                                    )
                                )
                            }
                        }
                        Resource.Status.ERROR -> {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.delete_alert_dialog_error, eventId),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> { }
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.delete_alert_dialog_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        deleteDialog.show()
        deleteDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
        deleteDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_blue))
        deleteDialog.findViewById<ImageView>(android.R.id.icon)
            .setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.blue),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
    }

    /** Initializes events data from user based on [user] preferences */
    @ExperimentalCoroutinesApi
    fun getLiveDataFromUser(user: User): LiveData<Resource<List<Event>>> {
        return when(eventCategory) {
            EventViewModel.EVENT_CATEGORY.FOR_ME -> eventViewModel.getTargetedEvents(user.preferences.target)
            EventViewModel.EVENT_CATEGORY.NEAR_ME -> eventViewModel.getNearEvents(user.preferences.location, user.preferences.radius)
            EventViewModel.EVENT_CATEGORY.COMING_SOON -> eventViewModel.getComingSoonEvents(user.preferences.weeks)
            EventViewModel.EVENT_CATEGORY.LIKED -> eventViewModel.getEventByIds(user.likedEvents)
            EventViewModel.EVENT_CATEGORY.CREATED -> eventViewModel.getEventByIds(user.createdEvents)
        }
    }

    /** Switches layout based on [load] value */
    private fun setLoading(load: Boolean) {
        if(load) {
            loading.visibility = View.VISIBLE
            setShowRecyclerView(false)
        }
        else {
            loading.visibility = View.GONE
            setShowRecyclerView(true)
        }
    }

    /** Switches layout based on [isEmpty] value */
    private fun setEmpty(isEmpty: Boolean) {
        if(isEmpty) {
            empty.visibility = View.VISIBLE
            setShowRecyclerView(false)
        }
        else {
            empty.visibility = View.GONE
            setShowRecyclerView(true)
        }
    }

    /** Switches layout based on [show] value */
    private fun setShowRecyclerView(show: Boolean) {
        if(show) {
            recycler.visibility = View.VISIBLE
        }
        else {
            recycler.visibility = View.GONE
        }
    }

    /** Sets the error layout text based on [message] and switches layout */
    private fun setError(message: String?) {
        if(message == null) {
            error.text = ""
            error.visibility = View.VISIBLE
        }
        else {
            error.text = message
            error.visibility = View.GONE
            setShowRecyclerView(false)
        }
    }

    companion object {
        /** Returns the gallery name from the [fragmentId] associated */
        fun getEventCategoryFromFragmentId(fragmentId: Int): EventViewModel.EVENT_CATEGORY {
            return when(fragmentId) {
                R.id.fragment_soon_event_vgallery -> EventViewModel.EVENT_CATEGORY.COMING_SOON
                R.id.fragment_near_event_vgallery -> EventViewModel.EVENT_CATEGORY.NEAR_ME
                R.id.fragment_for_me_event_vgallery -> EventViewModel.EVENT_CATEGORY.FOR_ME
                R.id.fragment_liked_profile_vgallery -> EventViewModel.EVENT_CATEGORY.LIKED
                R.id.fragment_created_profile_vgallery -> EventViewModel.EVENT_CATEGORY.CREATED
                else -> EventViewModel.EVENT_CATEGORY.COMING_SOON
            }
        }
    }
}
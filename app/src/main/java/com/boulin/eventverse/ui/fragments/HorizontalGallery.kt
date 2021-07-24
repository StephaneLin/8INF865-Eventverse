package com.boulin.eventverse.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.boulin.eventverse.R
import com.boulin.eventverse.adapters.EventAdapter
import com.boulin.eventverse.data.model.Event

import com.boulin.eventverse.data.model.User
import com.boulin.eventverse.data.viewmodels.EventViewModel
import com.boulin.eventverse.data.viewmodels.UserViewModel
import com.hadiyarajesh.flower.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Fragment displaying a gallery of events displayed horizontally
 */
class HorizontalGallery : Fragment() {
    private lateinit var eventCategory: EventViewModel.EVENT_CATEGORY
    private lateinit var galleryTitle: String
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
        galleryTitle = getString(getGalleryNameIdFromCategory(eventCategory))
    }

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_horizontal_gallery, container, false)

        view.findViewById<TextView>(R.id.gallery_title).text = galleryTitle
        empty = view.findViewById(R.id.hgallery_empty)

        // Adds listener for "view more button"
        view.findViewById<TextView>(R.id.gallery_view_more_link).setOnClickListener {
            val bundle  = Bundle()
            bundle.putInt(EventsFragment.ARG_EVENT_CATEGORY_TAB, eventCategory.ordinal)
            // TODO Symph : Question for M. Plantevin : Why can't the action be declared in the HorizontalGallery fragment ?!
            findNavController().navigate(R.id.action_ViewMoreEvents, bundle)
        }

        // Sets up the view as loading while waiting for data
        loading = view.findViewById(R.id.hgallery_loading)
        recycler = view.findViewById(R.id.gallery_events)
        setLoading(true)

        // Sets up the error layout
        error = view.findViewById(R.id.hgallery_error)
        setError(null)

        // Creates and assigns a new event adapter
        eventsAdapter = EventAdapter()
        recycler.adapter = eventsAdapter

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
                        /* Retrieve asynchronously the list of events according to the user preferences
                         * The layout is adapted according to the status of the request : LOADING, SUCCESS or ERROR
                         */
                        getLiveDataFromUserPreferences(user.preferences).observe(viewLifecycleOwner) {
                                eventsResource ->
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

    /** Initializes events data from user based on [userPreferences] */
    @ExperimentalCoroutinesApi
    fun getLiveDataFromUserPreferences(userPreferences: User.UserPreferences): LiveData<Resource<List<Event>>> {
        return when(eventCategory) {
            EventViewModel.EVENT_CATEGORY.FOR_ME -> eventViewModel.getTargetedEvents(targetAudience = userPreferences.target, limit = DEFAULT_LIMIT)
            EventViewModel.EVENT_CATEGORY.NEAR_ME -> eventViewModel.getNearEvents(location = userPreferences.location, radius = userPreferences.radius, limit = DEFAULT_LIMIT)
            EventViewModel.EVENT_CATEGORY.COMING_SOON -> eventViewModel.getComingSoonEvents(weeks = userPreferences.weeks, limit = DEFAULT_LIMIT)
            else -> MutableLiveData() // shouldn't happen
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
            error.visibility = View.GONE
        }
        else {
            error.text = message
            error.visibility = View.VISIBLE
            setShowRecyclerView(false)
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

    companion object {
        // Limit of event per gallery
        const val DEFAULT_LIMIT = 5

        /** Returns the gallery name from the [fragmentId] associated */
        fun getEventCategoryFromFragmentId(fragmentId: Int): EventViewModel.EVENT_CATEGORY {
            return when(fragmentId) {
                R.id.fragment_soon_event_hgallery -> EventViewModel.EVENT_CATEGORY.COMING_SOON
                R.id.fragment_near_event_hgallery -> EventViewModel.EVENT_CATEGORY.NEAR_ME
                R.id.fragment_for_me_event_hgallery -> EventViewModel.EVENT_CATEGORY.FOR_ME
                else -> EventViewModel.EVENT_CATEGORY.COMING_SOON
            }
        }

        /** Returns the gallery name from the event [category] associated */
        fun getGalleryNameIdFromCategory(category: EventViewModel.EVENT_CATEGORY): Int {
            return when(category) {
                EventViewModel.EVENT_CATEGORY.NEAR_ME -> R.string.near_tab_menu_label
                EventViewModel.EVENT_CATEGORY.FOR_ME -> R.string.for_me_tab_menu_label
                EventViewModel.EVENT_CATEGORY.COMING_SOON -> R.string.soon_tab_menu_label
                EventViewModel.EVENT_CATEGORY.LIKED -> R.string.liked_tab_menu_label
                EventViewModel.EVENT_CATEGORY.CREATED -> R.string.created_tab_menu_label
            }
        }
    }
}
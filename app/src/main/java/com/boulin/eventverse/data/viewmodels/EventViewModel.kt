package com.boulin.eventverse.data.viewmodels

import androidx.lifecycle.*
import com.boulin.eventverse.data.model.Event
import com.boulin.eventverse.data.model.Location
import com.boulin.eventverse.data.model.User
import com.hadiyarajesh.flower.Resource
import com.boulin.eventverse.data.repositories.EventRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import kotlin.math.min

/**
 * Creates a view model for events
 */
class EventViewModel(
    val eventRepository: EventRepository
) : ViewModel() {

    @ExperimentalCoroutinesApi
    fun getAllEvents(forceRefresh: Boolean = false): LiveData<Resource<List<Event>>> {
        return eventRepository.getAllEvents(forceRefresh).asLiveData(viewModelScope.coroutineContext)
    }

    @ExperimentalCoroutinesApi
    fun getNearEvents(location: Location, radius: Float, limit: Int? = null): LiveData<Resource<List<Event>>> {
        return filterSortAndLimitEvents(getFilterNearEvents(location, radius), sortEventsByDistance,limit)
    }

    @ExperimentalCoroutinesApi
    fun getComingSoonEvents(weeks: Int, limit: Int? = null): LiveData<Resource<List<Event>>> {
        return filterSortAndLimitEvents(getFilterComingEvents(weeks), sortEventsByDate,limit)
    }

    @ExperimentalCoroutinesApi
    fun getTargetedEvents(targetAudience: User.TargetAudience, limit: Int? = null): LiveData<Resource<List<Event>>> {
        return filterSortAndLimitEvents(getFilterTargetEvents(targetAudience), sortEventsByDate,limit)
    }

    @ExperimentalCoroutinesApi
    fun getEventByIds(eventIds : List<String>): LiveData<Resource<List<Event>>> {
        return filterSortAndLimitEvents(getFilterByIds(eventIds), sortEventsByDate)
    }

    @ExperimentalCoroutinesApi
    fun getEventById(eventId : String, forceRefresh: Boolean = false): LiveData<Resource<Event>> {
        return eventRepository.getEventById(eventId, forceRefresh).asLiveData(viewModelScope.coroutineContext)
    }

    @ExperimentalCoroutinesApi
    fun createEvent(eventInputParams: Event.EventInputParams): LiveData<Resource<Event>> {
        return eventRepository.createEvent(eventInputParams).asLiveData(viewModelScope.coroutineContext)
    }

    @ExperimentalCoroutinesApi
    fun updateEvent(eventId : String, eventMutableParams: Event.EventInputParams): LiveData<Resource<Event>> {
        return eventRepository.updateEvent(eventId, eventMutableParams).asLiveData(viewModelScope.coroutineContext)
    }

    @ExperimentalCoroutinesApi
    fun deleteEvent(eventId : String): LiveData<Resource<Event>> {
        return eventRepository.deleteEvent(eventId).asLiveData(viewModelScope.coroutineContext)
    }

    /**
     * Categories for display
     */
    enum class EVENT_CATEGORY {
        FOR_ME, // events within the same target audience of the user
        NEAR_ME, // events near the user or a specified location
        COMING_SOON, // events starting withing a certain amount of weeks
        LIKED, // events that are liked by the user
        CREATED // events created by the user
    }

    /**
     * Returns a livedata of events filtered and sorted by the passed functions, we also can limit the amount of returned events
     */
    @ExperimentalCoroutinesApi
    private fun filterSortAndLimitEvents(filter: (Event) -> Boolean, sort: ((List<Event>) -> List<Event>)?, limit: Int? = null): LiveData<Resource<List<Event>>> {
        return getAllEvents().switchMap {
            MutableLiveData<Resource<List<Event>>>().apply {
                if(it.status == Resource.Status.SUCCESS && it.data != null) {
                    var events = it.data!!

                    events = events.filter(filter)

                    if(limit != null) {
                        events = events.subList(0, min(limit, events.size))
                    }

                    if(sort != null) {
                        events = sort(events)
                    }


                    value = Resource.success(events)
                }
                else {
                    value = it
                }

            }
        }
    }

    companion object {
        /**
         * Returns a filter that accepts events within a radius of the passed location
         */
        fun getFilterNearEvents(location: Location, radius: Float): (Event) -> Boolean {
            val androidLocation = location.getAndroidLocation()
            return { event ->
                event.distance = androidLocation.distanceTo(event.location.getAndroidLocation()) / 1000
                event.distance!! < radius
            }
        }

        /**
         * Returns a filter that accepts events with the passed target audience
         */
        fun getFilterTargetEvents(target: User.TargetAudience): (Event) -> Boolean {
            return { event ->
                event.target == target
            }
        }

        /**
         * Returns a filter that accepts events starting in the "weeks" next weeks
         */
        fun getFilterComingEvents(weeks: Int): (Event) -> Boolean {
            return { event ->
                val now = Calendar.getInstance().time

                val maxDiff = weeks * MS_IN_A_WEEK
                val actualDiff = now.time - event.startDate.time

                actualDiff < maxDiff
            }
        }

        /**
         * Returns a filter that accepts events whose id is in the passed list
         */
        fun getFilterByIds(ids: List<String>): (Event) -> Boolean {
            return { event ->
                ids.contains(event.id)
            }
        }

        /**
         * Sort list of events by date and returns the sorted list
         */
        val sortEventsByDate: (List<Event>) -> List<Event> = { events ->
            events.sortedBy { event -> event.startDate }
        }

        /**
         * Sort list of events by distance (precomputed) and returns the sorted list
         */
        val sortEventsByDistance: (List<Event>) -> List<Event> = { events ->
            events.sortedBy { event -> event.distance }
        }
    }
}

private const val MS_IN_A_WEEK = 604800000

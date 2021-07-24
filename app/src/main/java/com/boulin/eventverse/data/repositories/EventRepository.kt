package com.boulin.eventverse.data.repositories

import com.boulin.eventverse.data.database.dao.EventDao
import com.boulin.eventverse.data.database.dao.UtilityDao
import com.boulin.eventverse.data.model.Event
import com.boulin.eventverse.data.model.Utility
import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkBoundResource
import com.boulin.eventverse.data.network.ApiInterface
import com.boulin.eventverse.data.network.fetchNetworkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.*

class EventRepository(
    private val apiInterface: ApiInterface,
    private val eventDao: EventDao,
    private val utilityDao: UtilityDao
) {

    /**
     * Returns true if we should fetch events based on last fetch date
     */
    private fun shouldFetchData() : Boolean {
        val utility = utilityDao.getUtility(Utility.UNIQUE_ID)
        val now = Calendar.getInstance().time

        // if need fetch (utility is null or last fetch expired)
        val needFetch = utility == null || now.time - utility.lastEventFetch.time > Utility.MAX_FETCH_INTERVAL

        // if fetch needed, refresh (or create) utility
        if(needFetch) {
            val newUtility = Utility(Utility.UNIQUE_ID, now)
            utilityDao.insertOrUpdateUtility(newUtility)
        }

        return needFetch
    }

    @ExperimentalCoroutinesApi
    fun getEventById(id: String, forceRefresh: Boolean, onFailed: (String?,Int) -> Unit = { _: String?, _: Int -> }): Flow<Resource<Event>> {
        val networkBoundFlow = networkBoundResource(
            fetchFromLocal = {
                eventDao.getEventById(id)
            },
            shouldFetchFromRemote = {
                // fetch from remote if event is null or need refresh
                it == null || forceRefresh || shouldFetchData()
            },
            fetchFromRemote = {
                apiInterface.getEventById(id)
            },
            saveRemoteData = { event ->
                eventDao.insertOrUpdateEvent(event)
            },
            onFetchFailed = { errorBody, statusCode -> onFailed(errorBody, statusCode) }
        ).map {
            when (it.status) {
                Resource.Status.LOADING -> {
                    Resource.loading(null)
                }
                Resource.Status.SUCCESS -> {
                    val event = it.data
                    Resource.success(event)
                }
                Resource.Status.ERROR -> {
                    Resource.error(it.message!!, null)
                }
            }
        }

        return networkBoundFlow.flowOn(Dispatchers.IO)
    }

    @ExperimentalCoroutinesApi
    fun getAllEvents(forceRefresh: Boolean,onFailed: (String?,Int) -> Unit = { _: String?, _: Int -> }): Flow<Resource<List<Event>>> {
        val networkBoundFlow = networkBoundResource(
            fetchFromLocal = {
                eventDao.getAllEvents()
            },
            shouldFetchFromRemote = {
                // fetch from remote if event is null, empty or need refresh
                it == null || it.isEmpty() ||  forceRefresh || shouldFetchData()
            },
            fetchFromRemote = {
                apiInterface.getAllEvents()
            },
            saveRemoteData = { events ->
                // first delete all events
                eventDao.deleteEvents()

                // then save each event
                events.forEach { event -> eventDao.insertOrUpdateEvent(event) }
            },
            onFetchFailed = { errorBody, statusCode -> onFailed(errorBody, statusCode) }
        ).map {
            when (it.status) {
                Resource.Status.LOADING -> {
                    Resource.loading(null)
                }
                Resource.Status.SUCCESS -> {
                    val events = it.data
                    Resource.success(events)
                }
                Resource.Status.ERROR -> {
                    Resource.error(it.message!!, null)
                }
            }
        }

        return networkBoundFlow.flowOn(Dispatchers.IO)
    }

    @ExperimentalCoroutinesApi
    fun createEvent(eventInputParams: Event.EventInputParams, onFailed: (String?,Int) -> Unit = { _: String?, _: Int -> }): Flow<Resource<Event>> {
        val networkBoundFlow = fetchNetworkBoundResource(
            fetchFromRemote = {
                apiInterface.createEvent(eventInputParams)
            },
            saveRemoteData = { event ->
                eventDao.insertOrUpdateEvent(event)
            },
            fetchFromLocal = {
                eventDao.getEventById(it.body!!.id)
            },
            onFetchFailed = { errorBody, statusCode -> onFailed(errorBody, statusCode) }
        ).map {
            when (it.status) {
                Resource.Status.LOADING -> {
                    Resource.loading(null)
                }
                Resource.Status.SUCCESS -> {
                    val events = it.data
                    Resource.success(events)
                }
                Resource.Status.ERROR -> {
                    Resource.error(it.message!!, null)
                }
            }
        }

        return networkBoundFlow.flowOn(Dispatchers.IO)
    }

    @ExperimentalCoroutinesApi
    fun updateEvent(eventId : String, eventMutableParams: Event.EventInputParams, onFailed: (String?,Int) -> Unit = { _: String?, _: Int -> }): Flow<Resource<Event>> {
        val networkBoundFlow = fetchNetworkBoundResource(
            fetchFromRemote = {
                apiInterface.updateEvent(eventId, eventMutableParams)
            },
            saveRemoteData = { event ->
                eventDao.insertOrUpdateEvent(event)
            },
            fetchFromLocal = {
                eventDao.getEventById(it.body!!.id)
            },
            onFetchFailed = { errorBody, statusCode -> onFailed(errorBody, statusCode) }
        ).map {
            when (it.status) {
                Resource.Status.LOADING -> {
                    Resource.loading(null)
                }
                Resource.Status.SUCCESS -> {
                    val events = it.data
                    Resource.success(events)
                }
                Resource.Status.ERROR -> {
                    Resource.error(it.message!!, null)
                }
            }
        }

        return networkBoundFlow.flowOn(Dispatchers.IO)
    }

    @ExperimentalCoroutinesApi
    fun deleteEvent(eventId : String, onFailed: (String?,Int) -> Unit = { _: String?, _: Int -> }): Flow<Resource<Event>> {
        val networkBoundFlow = fetchNetworkBoundResource(
            fetchFromRemote = {
                apiInterface.deleteEvent(eventId)
            },
            saveRemoteData = { event ->
                eventDao.deleteEvent(event.id)
            },
            fetchFromLocal = {
                eventDao.getEventById(it.body!!.id)
            },
            onFetchFailed = { errorBody, statusCode -> onFailed(errorBody, statusCode) }
        ).map {
            when (it.status) {
                Resource.Status.LOADING -> {
                    Resource.loading(null)
                }
                Resource.Status.SUCCESS -> {
                    val events = it.data
                    Resource.success(events)
                }
                Resource.Status.ERROR -> {
                    Resource.error(it.message!!, null)
                }
            }
        }

        return networkBoundFlow.flowOn(Dispatchers.IO)
    }
}
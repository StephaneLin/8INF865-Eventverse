package com.boulin.eventverse.data.network

import com.boulin.eventverse.data.model.Event
import com.boulin.eventverse.data.model.User
import com.hadiyarajesh.flower.ApiResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.http.*

/**
 * Interface with our API (every request need to be done under authentification and response are made with kotlin Flow coroutines)
 */
interface ApiInterface {

    /**
     * EVENT INTERFACE
     */

    @GET("events")
    fun getAllEvents() : Flow<ApiResponse<List<Event>>>

    @GET("events/{id}")
    fun getEventById(@Path("id") eventId: String) : Flow<ApiResponse<Event>>

    @POST("events")
    fun createEvent(@Body eventInputParams: Event.EventInputParams) : Flow<ApiResponse<Event>>

    @PATCH("events/{id}")
    fun updateEvent(@Path("id") eventId: String, @Body eventInputParams: Event.EventInputParams) : Flow<ApiResponse<Event>>

    @DELETE("events/{id}")
    fun deleteEvent(@Path("id") eventId: String) : Flow<ApiResponse<Event>>


    /**
     * USER INTERFACE
     */

    @GET("user")
    fun getUser() : Flow<ApiResponse<User>>

    @POST("user")
    fun createUser(@Body params: User.UserInputParams) : Flow<ApiResponse<User>>

    @PATCH("user")
    fun updateUser(@Body user: User.UserMutableParams) : Flow<ApiResponse<User>>

    @DELETE("user")
    fun deleteUser() : Flow<ApiResponse<User>>
}
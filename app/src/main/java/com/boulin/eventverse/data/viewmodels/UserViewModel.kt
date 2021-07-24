package com.boulin.eventverse.data.viewmodels

import androidx.lifecycle.*
import com.boulin.eventverse.data.model.User
import com.boulin.eventverse.data.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.hadiyarajesh.flower.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Creates a view model for users
 */
class UserViewModel(
    val userRepository: UserRepository
) : ViewModel() {

    @ExperimentalCoroutinesApi
    fun getUser(forceRefresh: Boolean = true): LiveData<Resource<User>> {
        return userRepository.getUser(FirebaseAuth.getInstance().currentUser!!.uid, forceRefresh).asLiveData(viewModelScope.coroutineContext)
    }

    @ExperimentalCoroutinesApi
    fun createUser(userInputParams: User.UserInputParams) : LiveData<Resource<User>> {
        return userRepository.createUser(userInputParams).asLiveData(viewModelScope.coroutineContext)
    }

    @ExperimentalCoroutinesApi
    fun updateUser(userMutableParams: User.UserMutableParams) : LiveData<Resource<User>> {
        return userRepository.updateUser(userMutableParams).asLiveData(viewModelScope.coroutineContext)
    }

    @ExperimentalCoroutinesApi
    fun deleteUser() : LiveData<Resource<User>> {
        return userRepository.deleteUser().asLiveData(viewModelScope.coroutineContext)
    }
}


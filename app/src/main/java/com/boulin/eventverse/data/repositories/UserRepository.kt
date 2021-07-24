package com.boulin.eventverse.data.repositories

import com.boulin.eventverse.data.database.dao.UserDao
import com.boulin.eventverse.data.model.User
import com.boulin.eventverse.data.network.ApiInterface
import com.boulin.eventverse.data.network.fetchNetworkBoundResource
import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class UserRepository(
    private val apiInterface: ApiInterface,
    private val userDao: UserDao
) {
        @ExperimentalCoroutinesApi
        fun getUser(uid: String, forceRefresh: Boolean, onFailed: (String?,Int) -> Unit = { _: String?, _: Int -> }): Flow<Resource<User>> {
            val networkBoundFlow = networkBoundResource(
                fetchFromLocal = {
                    userDao.getUser(uid)
                },
                shouldFetchFromRemote = {
                    it == null || forceRefresh
                },
                fetchFromRemote = {
                    apiInterface.getUser()
                },
                saveRemoteData = { user ->
                    userDao.insertOrUpdateUser(user)
                },
                onFetchFailed = { errorBody, statusCode -> onFailed(errorBody, statusCode) }
            ).map {
                when (it.status) {
                    Resource.Status.LOADING -> {
                        Resource.loading(null)
                    }
                    Resource.Status.SUCCESS -> {
                        val user = it.data
                        Resource.success(user)
                    }
                    Resource.Status.ERROR -> {
                        Resource.error(it.message!!, null)
                    }
                }
            }

            return networkBoundFlow.flowOn(Dispatchers.IO)
        }

        @ExperimentalCoroutinesApi
        fun createUser(userInputParams: User.UserInputParams, onFailed: (String?,Int) -> Unit = { _: String?, _: Int -> }): Flow<Resource<User>> {
            val networkBoundFlow = fetchNetworkBoundResource(
                fetchFromRemote = {
                    apiInterface.createUser(userInputParams)
                },
                saveRemoteData = { distantUser ->
                    userDao.insertOrUpdateUser(distantUser)
                },
                fetchFromLocal = {
                    userDao.getUser(it.body!!.uid)
                },
                onFetchFailed = { errorBody, statusCode -> onFailed(errorBody, statusCode) }
            ).map {
                when (it.status) {
                    Resource.Status.LOADING -> {
                        Resource.loading(null)
                    }
                    Resource.Status.SUCCESS -> {
                        val user = it.data
                        Resource.success(user)
                    }
                    Resource.Status.ERROR -> {
                        Resource.error(it.message!!, null)
                    }
                }
            }

            return networkBoundFlow.flowOn(Dispatchers.IO)
        }

        @ExperimentalCoroutinesApi
        fun updateUser(userMutableParams: User.UserMutableParams, onFailed: (String?,Int) -> Unit = { _: String?, _: Int -> }): Flow<Resource<User>> {
            val networkBoundFlow = fetchNetworkBoundResource(
                fetchFromRemote = {
                    apiInterface.updateUser(userMutableParams)
                },
                saveRemoteData = { distantUser ->
                    userDao.insertOrUpdateUser(distantUser)
                },
                fetchFromLocal = {
                    userDao.getUser(it.body!!.uid)
                },
                onFetchFailed = { errorBody, statusCode -> onFailed(errorBody, statusCode) }
            ).map {
                when (it.status) {
                    Resource.Status.LOADING -> {
                        Resource.loading(null)
                    }
                    Resource.Status.SUCCESS -> {
                        val updatedUser = it.data
                        Resource.success(updatedUser)
                    }
                    Resource.Status.ERROR -> {
                        Resource.error(it.message!!, null)
                    }
                }
            }

            return networkBoundFlow.flowOn(Dispatchers.IO)
        }

        @ExperimentalCoroutinesApi
        fun deleteUser(onFailed: (String?,Int) -> Unit = { _: String?, _: Int -> }): Flow<Resource<User>> {
            val networkBoundFlow = fetchNetworkBoundResource(
                fetchFromRemote = {
                    apiInterface.deleteUser()
                },
                saveRemoteData = { distantUser ->
                    userDao.deleteUser(distantUser.uid)
                },
                fetchFromLocal = {
                    userDao.getUser(it.body!!.uid)
                },
                onFetchFailed = { errorBody, statusCode -> onFailed(errorBody, statusCode) }
            ).map {
                when (it.status) {
                    Resource.Status.LOADING -> {
                        Resource.loading(null)
                    }
                    Resource.Status.SUCCESS -> {
                        val user = it.data
                        Resource.success(user)
                    }
                    Resource.Status.ERROR -> {
                        Resource.error(it.message!!, null)
                    }
                }
            }

            return networkBoundFlow.flowOn(Dispatchers.IO)
        }
    }
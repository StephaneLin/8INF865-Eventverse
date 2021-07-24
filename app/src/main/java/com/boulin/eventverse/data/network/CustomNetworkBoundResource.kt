package com.boulin.eventverse.data.network

import com.hadiyarajesh.flower.ApiErrorResponse
import com.hadiyarajesh.flower.ApiResponse
import com.hadiyarajesh.flower.ApiSuccessResponse
import com.hadiyarajesh.flower.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*


/**
 * Custom version of the network bound resource implemented in the flower library (by hadiyarajesh).
 * This version of the network bound resource allows to always fetch from the API and store it afterwards
 */
@ExperimentalCoroutinesApi
inline fun <DB, REMOTE> fetchNetworkBoundResource(
    crossinline fetchFromRemote: suspend () -> Flow<ApiResponse<REMOTE>>,
    crossinline processRemoteResponse: (response: ApiSuccessResponse<REMOTE>) -> Unit = { Unit },
    crossinline saveRemoteData: suspend (REMOTE) -> Unit = { Unit },
    crossinline fetchFromLocal: suspend (response: ApiSuccessResponse<REMOTE>) -> Flow<DB>,
    crossinline onFetchFailed: (errorBody: String?, statusCode: Int) -> Unit = { _: String?, _: Int -> Unit }
) = flow<Resource<DB>> {

    // 1 - the resource is loading
    emit(Resource.loading(null))

    // 2 - we fetch from the API
    fetchFromRemote().collect { apiResponse ->
        when (apiResponse) {
            is ApiSuccessResponse -> {
                processRemoteResponse(apiResponse)

                // 3 - we save the remote data
                apiResponse.body?.let { saveRemoteData(it) }

                // 4 - we emit the remote data
                emitAll(fetchFromLocal(apiResponse).map { dbData ->
                    Resource.success(dbData)
                })
            }

            is ApiErrorResponse -> {
                onFetchFailed(apiResponse.errorMessage, apiResponse.statusCode)
                emit(
                    Resource.error(
                        apiResponse.errorMessage,
                        null
                    )
                )
            }

            else -> { }
        }
    }
}
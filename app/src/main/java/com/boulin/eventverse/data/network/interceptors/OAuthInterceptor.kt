package com.boulin.eventverse.data.network.interceptors

import okhttp3.Interceptor

/**
 * Defines an Authentificator interceptor for okhttpclient that add a custom header for Authorization. It allows the edition of the token so that it can be updated at runtime
 */
open class OAuthInterceptor(private val tokenType: String, private var accessToken: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        request = request.newBuilder().header("Authorization", "$tokenType $accessToken").build()

        return chain.proceed(request)
    }

    /**
     * Change the token used in the interceptor (for runtime edition)
     */
    fun setToken(token: String) {
        accessToken = token
    }
}
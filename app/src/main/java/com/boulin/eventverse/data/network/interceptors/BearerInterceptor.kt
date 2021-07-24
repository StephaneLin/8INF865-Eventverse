package com.boulin.eventverse.data.network.interceptors

/**
 * Defines an intercepter for okhttpclient that add a header with a bearer token (to authentificate requests)
 */
class BearerInterceptor() : OAuthInterceptor("Bearer", "")
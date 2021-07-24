package com.boulin.eventverse.di

import com.boulin.eventverse.data.repositories.EventRepository
import com.boulin.eventverse.data.repositories.UserRepository
import org.koin.dsl.module

/**
 * Creates a module for the repositories with singleton for each repository (with constructor injection)
 */
val repositoryModule = module {
    single { EventRepository(apiInterface = get(), eventDao = get(), utilityDao = get()) }
    single { UserRepository(apiInterface = get(), userDao = get()) }
}
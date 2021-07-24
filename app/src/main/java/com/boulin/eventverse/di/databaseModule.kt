package com.boulin.eventverse.di

import com.boulin.eventverse.data.database.EventverseDatabase
import org.koin.dsl.module

/**
 * Defines module with dependency injection for the database and DAOs
 */
val databaseModule = module {
    single { EventverseDatabase(context = get()) }
    single { EventverseDatabase(context = get()).eventDao() }
    single { EventverseDatabase(context = get()).userDao() }
    single { EventverseDatabase(context = get()).utilityDao() }
}
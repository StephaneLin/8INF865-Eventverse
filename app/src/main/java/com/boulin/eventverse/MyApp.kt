package com.boulin.eventverse

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.boulin.eventverse.di.databaseModule
import com.boulin.eventverse.di.networkModule
import com.boulin.eventverse.di.repositoryModule
import com.boulin.eventverse.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Base application that enables multidexing (for firebase to work) and inject all modules into the app
 */
class MyApp: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MyApp)
            modules(listOf(databaseModule, networkModule, repositoryModule, viewModelModule))
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
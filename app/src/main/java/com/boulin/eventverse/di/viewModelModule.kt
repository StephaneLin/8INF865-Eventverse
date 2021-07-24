package com.boulin.eventverse.di

import com.boulin.eventverse.data.viewmodels.EventViewModel
import com.boulin.eventverse.data.viewmodels.UserViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Module that exposes viewModels with the android way to do it
 */
val viewModelModule = module {
    viewModel { EventViewModel(eventRepository = get()) }
    viewModel { UserViewModel(userRepository = get()) }
}
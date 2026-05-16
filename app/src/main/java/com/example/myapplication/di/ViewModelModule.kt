package com.example.myapplication.di

import com.example.myapplication.ui.inventory.InventoryViewModel
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.profile.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { LoginViewModel(get(), get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { InventoryViewModel(get()) }
}

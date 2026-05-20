package com.example.myapplication.di

import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.example.myapplication.ui.inventory.InventoryViewModel
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.logs.LogViewModel
import com.example.myapplication.ui.profile.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AuthViewModel(get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { InventoryViewModel(get(), get()) }
    viewModel { LogViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
}

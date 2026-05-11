package com.example.myapplication.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import com.example.myapplication.data.local.SessionManager

val localModule = module {
    single { SessionManager(androidContext()) }
}

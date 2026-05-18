package com.example.myapplication.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import com.example.myapplication.data.local.SessionManager

val localModule = module {
    single { SessionManager(androidContext()) }
    single {
        androidx.room.Room.databaseBuilder(
            androidContext(),
            com.example.myapplication.data.local.AppDatabase::class.java,
            "my_app_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    single { get<com.example.myapplication.data.local.AppDatabase>().inventoryDao() }
    single { get<com.example.myapplication.data.local.AppDatabase>().logDao() }
}

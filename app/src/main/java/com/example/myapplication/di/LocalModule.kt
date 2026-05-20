package com.example.myapplication.di

import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localModule = module {
    single { SessionManager(androidContext()) }

    single {
        androidx.room.Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "my_app_database"
        )
        .addMigrations(AppDatabase.MIGRATION_2_3)
        .addCallback(AppDatabase.SEED_CALLBACK)
        .build()
    }

    single { get<AppDatabase>().inventoryDao() }
    single { get<AppDatabase>().logDao() }
    single { get<AppDatabase>().userDao() }
}

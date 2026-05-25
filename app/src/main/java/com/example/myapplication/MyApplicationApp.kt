package com.example.myapplication

import android.app.Application
import com.example.myapplication.di.repositoryModule
import com.example.myapplication.di.localModule
import com.example.myapplication.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class MyApplicationApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Timber: only log in debug builds
        if (AppConfig.IS_DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            // Koin logger: verbose in debug, silent in release
            androidLogger(if (AppConfig.IS_DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@MyApplicationApp)
            modules(
                repositoryModule,
                localModule,
                viewModelModule
            )
        }
    }
}

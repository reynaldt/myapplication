package com.example.myapplication.di

import com.example.myapplication.data.repository.LoginRepositoryImpl
import com.example.myapplication.domain.repository.LoginRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<LoginRepository> { LoginRepositoryImpl(get()) }
}

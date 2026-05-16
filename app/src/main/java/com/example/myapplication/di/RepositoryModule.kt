package com.example.myapplication.di

import com.example.myapplication.data.repository.InventoryRepositoryImpl
import com.example.myapplication.data.repository.LoginRepositoryImpl
import com.example.myapplication.data.repository.ProfileRepositoryImpl
import com.example.myapplication.domain.repository.InventoryRepository
import com.example.myapplication.domain.repository.LoginRepository
import com.example.myapplication.domain.repository.ProfileRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<LoginRepository> { LoginRepositoryImpl(get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    single<InventoryRepository> { InventoryRepositoryImpl(get()) }
}

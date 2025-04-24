package com.OxGames.Pluvia.di

import com.OxGames.Pluvia.service.PcgwApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providePcgwApiService(): PcgwApiService {
        return PcgwApiService()
    }
} 
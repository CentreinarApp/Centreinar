package com.example.centreinar.di

import com.example.centreinar.data.repository.*
import com.example.centreinar.domain.repository.DiscountRepositoryMilho
import com.example.centreinar.util.Utilities

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideUtilities(): Utilities = Utilities()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindsModule {

    @Binds
    @Singleton
    abstract fun bindClassificationRepositorySoja(
        impl: ClassificationRepositoryImpl
    ): ClassificationRepository

    @Binds
    @Singleton
    abstract fun bindDiscountRepositorySoja(
        impl: DiscountRepositoryImpl
    ): DiscountRepository

    @Binds
    abstract fun bindClassificationRepositoryMilho(
        impl: ClassificationRepositoryMilhoImpl
    ): ClassificationRepositoryMilho

    @Binds
    abstract fun bindDiscountRepositoryMilho(
        impl: DiscountRepositoryMilhoImpl
    ): DiscountRepositoryMilho
}

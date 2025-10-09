package com.example.centreinar.di

import com.example.centreinar.data.repository.*
import com.example.centreinar.util.Utilities

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


// Imports para as interfaces e implementações de repositório
import com.example.centreinar.data.repository.ClassificationRepository
import com.example.centreinar.data.repository.ClassificationRepositoryImpl
import com.example.centreinar.data.repository.DiscountRepository
import com.example.centreinar.data.repository.DiscountRepositoryImpl
import com.example.centreinar.data.repository.ClassificationRepositoryMilho
import com.example.centreinar.data.repository.ClassificationRepositoryMilhoImpl
import com.example.centreinar.data.repository.DiscountRepositoryMilhoImpl
import com.example.centreinar.domain.repository.DiscountRepositoryMilho


// Módulo de PROVIDES (Utilitários)
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Apenas utilitários e outras dependências gerais
    @Provides
    fun provideUtilities(): Utilities = Utilities()
}


// Módulo Abstrato para os BINDINGS (Ligações Interface -> Implementação)
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindsModule {

    // --- LIGAÇÕES DE SOJA ---
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

    // --- LIGAÇÕES DE MILHO ---
    @Binds
    abstract fun bindClassificationRepositoryMilho(
        impl: ClassificationRepositoryMilhoImpl
    ): ClassificationRepositoryMilho

    @Binds
    abstract fun bindDiscountRepositoryMilho(
        impl: DiscountRepositoryMilhoImpl
    ): DiscountRepositoryMilho
}
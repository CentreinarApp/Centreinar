package com.example.centreinar.di

import com.example.centreinar.ui.classificationProcess.strategy.GrainStrategy
import com.example.centreinar.ui.classificationProcess.strategy.MilhoClassificationStrategy
import com.example.centreinar.ui.classificationProcess.strategy.SojaClassificationStrategy
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
@InstallIn(SingletonComponent::class)
abstract class ClassificationStrategyModule {
    // As chaves que usamos para buscar as estratégias
    @Binds
    @IntoMap
    @StringKey("Soja")
    abstract fun bindSojaStrategy(
        sojaStrategy: SojaClassificationStrategy
    ): GrainStrategy

    @Binds
    @IntoMap
    @StringKey("Milho")
    abstract fun bindMilhoStrategy(
        milhoStrategy: MilhoClassificationStrategy
    ): GrainStrategy
}
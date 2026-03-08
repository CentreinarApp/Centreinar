package com.example.centreinar.di

import com.example.centreinar.ui.discount.strategy.GrainDiscountStrategy
import com.example.centreinar.ui.discount.strategy.MilhoDiscountStrategy
import com.example.centreinar.ui.discount.strategy.SojaDiscountStrategy
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
@InstallIn(SingletonComponent::class)
abstract class DiscountStrategyModule {
    // As chaves que usamos para buscar as estratégias
    @Binds
    @IntoMap
    @StringKey("Soja")
    abstract fun bindSojaDiscountStrategy(
        strategy: SojaDiscountStrategy
    ): GrainDiscountStrategy

    @Binds
    @IntoMap
    @StringKey("Milho")
    abstract fun bindMilhoDiscountStrategy(
        strategy: MilhoDiscountStrategy
    ): GrainDiscountStrategy
}
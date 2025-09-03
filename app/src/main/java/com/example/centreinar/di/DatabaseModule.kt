package com.example.centreinar.di

import android.content.Context
import androidx.room.Room
import com.example.centreinar.data.local.AppDatabase
import com.example.centreinar.data.local.dao.ClassificationSojaDao
import com.example.centreinar.data.local.dao.ColorClassificationSojaDao
import com.example.centreinar.data.local.dao.DiscountSojaDao
import com.example.centreinar.data.local.dao.DisqualificationSojaDao
import com.example.centreinar.data.local.dao.InputDiscountSojaDao
import com.example.centreinar.data.local.dao.LimitSojaDao
import com.example.centreinar.data.local.dao.SampleSojaDao
import com.example.centreinar.data.repository.ClassificationRepository
import com.example.centreinar.data.repository.ClassificationRepositoryImpl
import com.example.centreinar.data.repository.DiscountRepository
import com.example.centreinar.data.repository.DiscountRepositoryImp
import com.example.centreinar.util.Utilities
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "grains_db"
        )
            .createFromAsset("database/innit.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAO Providers
    @Provides fun limitDao(db: AppDatabase) = db.limitSojaDao()
    @Provides fun classificationDao(db: AppDatabase) = db.classificationSojaDao()
    @Provides fun sampleDao(db: AppDatabase) = db.sampleSojaDao()
    @Provides fun discountDao(db: AppDatabase) = db.discountSojaDao()
    @Provides fun inputDiscountDao(db: AppDatabase) = db.inputDiscountSojaDao()
    @Provides fun colorClassificationDao(db: AppDatabase) = db.colorClassificationSojaDao()
    @Provides fun disqualificationDao(db: AppDatabase) = db.disqualificationSojaDao()

}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideClassificationRepository(
        limitDao: LimitSojaDao,
        classificationDao: ClassificationSojaDao,
        sampleDao: SampleSojaDao,
        tools: Utilities,
        colorClassificationDao: ColorClassificationSojaDao,
        disqualificationDao: DisqualificationSojaDao
    ): ClassificationRepository {
        return ClassificationRepositoryImpl(
            limitDao,
            classificationDao,
            sampleDao,
            tools,
            colorClassificationDao,
            disqualificationDao
        )
    }

    @Provides
    @Singleton
    fun provideDiscountRepository(
        limitDao: LimitSojaDao,
        classificationDao: ClassificationSojaDao,
        sampleDao: SampleSojaDao,
        discountDao: DiscountSojaDao,
        tools: Utilities,
        inputDiscountDao: InputDiscountSojaDao,
        colorClassificationDao: ColorClassificationSojaDao,
        disqualificationDao: DisqualificationSojaDao
    ): DiscountRepository {
        return DiscountRepositoryImp(
            limitDao,
            classificationDao,
            sampleDao,
            discountDao,
            inputDiscountDao,
            tools
        )
    }
}
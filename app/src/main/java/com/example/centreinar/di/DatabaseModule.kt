package com.example.centreinar.di

import android.content.Context
import androidx.room.Room
import com.example.centreinar.data.local.AppDatabase

// DAOs Soja
import com.example.centreinar.data.local.dao.ClassificationSojaDao
import com.example.centreinar.data.local.dao.ColorClassificationSojaDao
import com.example.centreinar.data.local.dao.DiscountSojaDao
import com.example.centreinar.data.local.dao.DisqualificationSojaDao
import com.example.centreinar.data.local.dao.InputDiscountSojaDao
import com.example.centreinar.data.local.dao.LimitSojaDao
import com.example.centreinar.data.local.dao.SampleSojaDao

// DAOs Milho
import com.example.centreinar.data.local.dao.ClassificationMilhoDao
import com.example.centreinar.data.local.dao.ColorClassificationMilhoDao
import com.example.centreinar.data.local.dao.DiscountMilhoDao
import com.example.centreinar.data.local.dao.DisqualificationMilhoDao
import com.example.centreinar.data.local.dao.InputDiscountMilhoDao
import com.example.centreinar.data.local.dao.LimitMilhoDao
import com.example.centreinar.data.local.dao.SampleMilhoDao

// Repositórios Soja
import com.example.centreinar.data.repository.ClassificationRepositoryImpl
import com.example.centreinar.data.repository.DiscountRepositoryImp
import com.example.centreinar.data.repository.DiscountRepository
import com.example.centreinar.data.repository.ClassificationRepository
// Repositórios Milho
import com.example.centreinar.data.repository.ClassificationRepositoryMilhoImpl
import com.example.centreinar.data.repository.DiscountRepositoryMilhoImpl
import com.example.centreinar.domain.repository.ClassificationRepositoryMilho
import com.example.centreinar.domain.repository.DiscountRepositoryMilho

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
            .fallbackToDestructiveMigration()
            .build()
    }

    // ---------- DAOs Soja ----------
    @Provides fun limitSojaDao(db: AppDatabase) = db.limitSojaDao()
    @Provides fun classificationSojaDao(db: AppDatabase) = db.classificationSojaDao()
    @Provides fun sampleSojaDao(db: AppDatabase) = db.sampleSojaDao()
    @Provides fun discountSojaDao(db: AppDatabase) = db.discountSojaDao()
    @Provides fun inputDiscountSojaDao(db: AppDatabase) = db.inputDiscountSojaDao()
    @Provides fun colorClassificationSojaDao(db: AppDatabase) = db.colorClassificationSojaDao()
    @Provides fun disqualificationSojaDao(db: AppDatabase) = db.disqualificationSojaDao()

    // ---------- DAOs Milho ----------
    @Provides fun limitMilhoDao(db: AppDatabase) = db.limitMilhoDao()
    @Provides fun classificationMilhoDao(db: AppDatabase) = db.classificationMilhoDao()
    @Provides fun sampleMilhoDao(db: AppDatabase) = db.sampleMilhoDao()
    @Provides fun discountMilhoDao(db: AppDatabase) = db.discountMilhoDao()
    @Provides fun inputDiscountMilhoDao(db: AppDatabase) = db.inputDiscountMilhoDao()
    @Provides fun colorClassificationMilhoDao(db: AppDatabase) = db.colorClassificationMilhoDao()
    @Provides fun disqualificationMilhoDao(db: AppDatabase) = db.disqualificationMilhoDao()
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    // ---------- Repositórios Soja ----------
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

    // ---------- Repositórios Milho ----------
    @Provides
    @Singleton
    fun provideClassificationRepositoryMilho(
        limitDao: LimitMilhoDao,
        classificationDao: ClassificationMilhoDao,
        sampleDao: SampleMilhoDao,
        tools: Utilities
    ): ClassificationRepositoryMilho {
        return ClassificationRepositoryMilhoImpl(
            limitDao,
            classificationDao,
            sampleDao,
            tools
        )
    }

    @Provides
    @Singleton
    fun provideDiscountRepositoryMilho(
        limitDao: LimitMilhoDao,
        classificationDao: ClassificationMilhoDao,
        sampleDao: SampleMilhoDao,
        discountDao: DiscountMilhoDao,
        inputDiscountDao: InputDiscountMilhoDao,
        tools: Utilities
    ): DiscountRepositoryMilho {
        return DiscountRepositoryMilhoImpl(
            limitDao,
            classificationDao,
            sampleDao,
            discountDao,
            inputDiscountDao,
            tools
        )
    }
}


package com.example.centreinar

import android.content.Context
import androidx.room.Room
import com.example.centreinar.AppDatabase
import com.example.centreinar.repositories.ClassificationRepository
import com.example.centreinar.repositories.ClassificationRepositoryImpl
import com.example.centreinar.repositories.DiscountRepository
import com.example.centreinar.repositories.DiscountRepositoryImp
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
    @Provides fun limitDao(db: AppDatabase) = db.limitDao()
    @Provides fun classificationDao(db: AppDatabase) = db.classificationDao()
    @Provides fun sampleDao(db: AppDatabase) = db.sampleDao()
    @Provides fun discountDao(db: AppDatabase) = db.discountDao()
    @Provides fun inputDiscountDao(db: AppDatabase) = db.inputDiscountDao()
    @Provides fun colorClassificationDao(db: AppDatabase) = db.colorClassificationDao()
    @Provides fun disqualificationDao(db: AppDatabase) = db.disqualificationDao()

}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideClassificationRepository(
        limitDao: LimitDao,
        classificationDao: ClassificationDao,
        sampleDao: SampleDao,
        tools: Utilities,
        colorClassificationDao: ColorClassificationDao,
        disqualificationDao: DisqualificationDao
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
        limitDao: LimitDao,
        classificationDao: ClassificationDao,
        sampleDao: SampleDao,
        discountDao: DiscountDao,
        tools: Utilities,
        colorClassificationDao: ColorClassificationDao,
        disqualificationDao: DisqualificationDao
    ): DiscountRepository {
        return DiscountRepositoryImp(
            limitDao,
            classificationDao,
            sampleDao,
            discountDao,
            tools
        )
    }
}
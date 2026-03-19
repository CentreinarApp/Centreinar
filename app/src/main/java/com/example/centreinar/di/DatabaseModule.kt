package com.example.centreinar.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.centreinar.data.local.DataSeeder
import com.example.centreinar.data.local.AppDatabase
import com.example.centreinar.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        provider: Provider<AppDatabase>
    ): AppDatabase {

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database.db"
        )
            .fallbackToDestructiveMigration() // Se mudar versão, limpa o banco e recria
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val database = provider.get()
                            DataSeeder(
                                limitSojaDao  = database.limitSojaDao(),
                                limitMilhoDao = database.limitMilhoDao()
                            ).seedAll()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })
            .build()
    }

    @Provides fun provideInputDiscountSojaDao(db: AppDatabase): InputDiscountSojaDao = db.inputDiscountSojaDao()
    @Provides fun provideDiscountSojaDao(db: AppDatabase): DiscountSojaDao = db.discountSojaDao()
    @Provides fun provideDisqualificationSojaDao(db: AppDatabase): DisqualificationSojaDao = db.disqualificationSojaDao()
    @Provides fun provideColorClassificationSojaDao(db: AppDatabase): ColorClassificationSojaDao = db.colorClassificationSojaDao()
    @Provides fun provideSampleSojaDao(db: AppDatabase): SampleSojaDao = db.sampleSojaDao()
    @Provides fun provideClassificationSojaDao(db: AppDatabase): ClassificationSojaDao = db.classificationSojaDao()
    @Provides fun provideLimitSojaDao(db: AppDatabase): LimitSojaDao = db.limitSojaDao()
    @Provides fun provideToxicSeedSojaDao(db: AppDatabase): ToxicSeedSojaDao = db.toxicSeedSojaDao()

    @Provides fun provideLimitMilhoDao(db: AppDatabase): LimitMilhoDao = db.limitMilhoDao()
    @Provides fun provideDiscountMilhoDao(db: AppDatabase): DiscountMilhoDao = db.discountMilhoDao()
    @Provides fun provideInputDiscountMilhoDao(db: AppDatabase): InputDiscountMilhoDao = db.inputDiscountMilhoDao()
    @Provides fun provideClassificationMilhoDao(db: AppDatabase): ClassificationMilhoDao = db.classificationMilhoDao()
    @Provides fun provideSampleMilhoDao(db: AppDatabase): SampleMilhoDao = db.sampleMilhoDao()

    @Provides fun provideDisqualificationMilhoDao(db: AppDatabase): DisqualificationMilhoDao = db.disqualificationMilhoDao()

    @Provides
    fun provideColorClassificationMilhoDao(database: AppDatabase): ColorClassificationMilhoDao {
        return database.colorClassificationMilhoDao()
    }
    @Provides fun provideToxicSeedMilhoDao(db: AppDatabase): ToxicSeedMilhoDao = db.toxicSeedMilhoDao()
}
package com.example.centreinar.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.centreinar.data.local.AppDatabase
import com.example.centreinar.data.local.dao.*
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.LimitSoja
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = provideAppDatabase(context)


                    val limitSojaDao = database.limitSojaDao()


                    limitSojaDao.insertLimit(
                        LimitSoja(
                            source = 1,
                            grain = "soja",
                            group = 1,
                            type = 1,
                            moistureLowerLim = 0f,
                            moistureUpLim = 14f,
                            impuritiesLowerLim = 0f,
                            impuritiesUpLim = 1f,
                            brokenCrackedDamagedLowerLim = 0f,
                            brokenCrackedDamagedUpLim = 30f,
                            greenishLowerLim = 0f,
                            greenishUpLim = 8f,
                            burntLowerLim = 0f,
                            burntUpLim = 6f,
                            moldyLowerLim = 0f,
                            moldyUpLim = 6f,
                            burntOrSourLowerLim = 0f,
                            burntOrSourUpLim = 6f,
                            spoiledTotalLowerLim = 0f,
                            spoiledTotalUpLim = 8f
                        )
                    )

                    limitSojaDao.insertLimit(
                        LimitSoja(
                            source = 1,
                            grain = "soja",
                            group = 1,
                            type = 2,
                            moistureLowerLim = 0f,
                            moistureUpLim = 14f,
                            impuritiesLowerLim = 0f,
                            impuritiesUpLim = 2f,
                            brokenCrackedDamagedLowerLim = 0f,
                            brokenCrackedDamagedUpLim = 40f,
                            greenishLowerLim = 0f,
                            greenishUpLim = 8f,
                            burntLowerLim = 0f,
                            burntUpLim = 6f,
                            moldyLowerLim = 0f,
                            moldyUpLim = 6f,
                            burntOrSourLowerLim = 0f,
                            burntOrSourUpLim = 6f,
                            spoiledTotalLowerLim = 0f,
                            spoiledTotalUpLim = 8f
                        )
                    )

                    limitSojaDao.insertLimit(
                        LimitSoja(
                            source = 1,
                            grain = "soja",
                            group = 2,
                            type = 1,
                            moistureLowerLim = 0f,
                            moistureUpLim = 14f,
                            impuritiesLowerLim = 0f,
                            impuritiesUpLim = 1f,
                            brokenCrackedDamagedLowerLim = 0f,
                            brokenCrackedDamagedUpLim = 30f,
                            greenishLowerLim = 0f,
                            greenishUpLim = 8f,
                            burntLowerLim = 0f,
                            burntUpLim = 6f,
                            moldyLowerLim = 0f,
                            moldyUpLim = 6f,
                            burntOrSourLowerLim = 0f,
                            burntOrSourUpLim = 6f,
                            spoiledTotalLowerLim = 0f,
                            spoiledTotalUpLim = 8f
                        )
                    )


                    val limitMilhoDao = database.limitMilhoDao()

                    limitMilhoDao.insertLimit(
                        LimitMilho(
                            source = 1,
                            grain = "milho",
                            group = 1,
                            type = 1,
                            moistureUpLim = 14f,
                            impuritiesUpLim = 1f,
                            brokenUpLim = 3f,
                            ardidoUpLim = 3f,
                            mofadoUpLim = 3f,
                            carunchadoUpLim = 3f
                        )
                    )


                    limitMilhoDao.insertLimit(
                        LimitMilho(
                            source = 1,
                            grain = "milho",
                            group = 1,
                            type = 2,
                            moistureUpLim = 14f,
                            impuritiesUpLim = 2f,
                            brokenUpLim = 5f,
                            ardidoUpLim = 5f,
                            mofadoUpLim = 5f,
                            carunchadoUpLim = 5f
                        )
                    )


                    limitMilhoDao.insertLimit(
                        LimitMilho(
                            source = 1,
                            grain = "milho",
                            group = 2,
                            type = 1,
                            moistureUpLim = 14f,
                            impuritiesUpLim = 2f,
                            brokenUpLim = 5f,
                            ardidoUpLim = 5f,
                            mofadoUpLim = 5f,
                            carunchadoUpLim = 5f
                        )
                    )
                }
            }
        }).fallbackToDestructiveMigration().build()
    }


    @Provides fun limitSojaDao(db: AppDatabase) = db.limitSojaDao()
    @Provides fun classificationSojaDao(db: AppDatabase) = db.classificationSojaDao()
    @Provides fun sampleSojaDao(db: AppDatabase) = db.sampleSojaDao()
    @Provides fun discountSojaDao(db: AppDatabase) = db.discountSojaDao()
    @Provides fun inputDiscountSojaDao(db: AppDatabase) = db.inputDiscountSojaDao()
    @Provides fun colorClassificationSojaDao(db: AppDatabase) = db.colorClassificationSojaDao()
    @Provides fun disqualificationSojaDao(db: AppDatabase) = db.disqualificationSojaDao()


    @Provides fun limitMilhoDao(db: AppDatabase) = db.limitMilhoDao()
    @Provides fun classificationMilhoDao(db: AppDatabase) = db.classificationMilhoDao()
    @Provides fun sampleMilhoDao(db: AppDatabase) = db.sampleMilhoDao()
    @Provides fun discountMilhoDao(db: AppDatabase) = db.discountMilhoDao()
    @Provides fun inputDiscountMilhoDao(db: AppDatabase) = db.inputDiscountMilhoDao()
    @Provides fun colorClassificationMilhoDao(db: AppDatabase) = db.colorClassificationMilhoDao()
    @Provides fun disqualificationMilhoDao(db: AppDatabase) = db.disqualificationMilhoDao()
}

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
            "app_database.db"
        )
            .fallbackToDestructiveMigration()   // 🔥 RECRIA O BANCO AUTOMATICAMENTE
            .addCallback(object : RoomDatabase.Callback() {

                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    // ⚠️ IMPORTANTE:
                    // o appDatabase JÁ ESTÁ CONSTRUÍDO quando o callback roda
                    // NÃO devemos chamar dbBuilder.build() novamente!
                    CoroutineScope(Dispatchers.IO).launch {

                        // Pegando instância já criada
                        val appDatabaseInstance = Room.databaseBuilder(
                            context,
                            AppDatabase::class.java,
                            "app_database.db"
                        ).build()

                        val sojaDao = appDatabaseInstance.limitSojaDao()
                        val milhoDao = appDatabaseInstance.limitMilhoDao()

                        // ==============================================
                        // INSERÇÃO DOS LIMITES MAPA — SOJA
                        // ==============================================

                        sojaDao.insertLimit(
                            LimitSoja(
                                source = 0, grain = "Soja", group = 1, type = 1,
                                impuritiesLowerLim = 0.0f, impuritiesUpLim = 1.0f,
                                moistureLowerLim = 0.0f, moistureUpLim = 14.0f,
                                brokenCrackedDamagedLowerLim = 0.0f, brokenCrackedDamagedUpLim = 8.0f,
                                greenishLowerLim = 0.0f, greenishUpLim = 2.0f,
                                burntLowerLim = 0.0f, burntUpLim = 0.3f,
                                burntOrSourLowerLim = 0.0f, burntOrSourUpLim = 1.0f,
                                moldyLowerLim = 0.0f, moldyUpLim = 0.5f,
                                spoiledTotalLowerLim = 0.0f, spoiledTotalUpLim = 4.0f
                            )
                        )

                        sojaDao.insertLimit(
                            LimitSoja(
                                source = 0, grain = "Soja", group = 2, type = 1,
                                impuritiesLowerLim = 0.0f, impuritiesUpLim = 1.0f,
                                moistureLowerLim = 0.0f, moistureUpLim = 14.0f,
                                brokenCrackedDamagedLowerLim = 0.0f, brokenCrackedDamagedUpLim = 30.0f,
                                greenishLowerLim = 0.0f, greenishUpLim = 8.0f,
                                burntLowerLim = 0.0f, burntUpLim = 1.0f,
                                burntOrSourLowerLim = 0.0f, burntOrSourUpLim = 4.0f,
                                moldyLowerLim = 0.0f, moldyUpLim = 6.0f,
                                spoiledTotalLowerLim = 0.0f, spoiledTotalUpLim = 8.0f
                            )
                        )

                        // ==============================================
                        // INSERÇÃO DOS LIMITES MAPA — MILHO
                        // ==============================================

                        milhoDao.insertLimit(
                            LimitMilho(
                                source = 0, grain = "Milho", group = 1, type = 1,
                                moistureUpLim = 14.0f,
                                impuritiesUpLim = 1.00f,
                                brokenUpLim = 3.00f,
                                ardidoUpLim = 1.00f,
                                mofadoUpLim = 6.00f,
                                carunchadoUpLim = 2.00f
                            )
                        )

                        milhoDao.insertLimit(
                            LimitMilho(
                                source = 0, grain = "Milho", group = 1, type = 2,
                                moistureUpLim = 14.0f,
                                impuritiesUpLim = 1.50f,
                                brokenUpLim = 4.00f,
                                ardidoUpLim = 2.00f,
                                mofadoUpLim = 10.00f,
                                carunchadoUpLim = 3.00f
                            )
                        )

                        milhoDao.insertLimit(
                            LimitMilho(
                                source = 0, grain = "Milho", group = 1, type = 3,
                                moistureUpLim = 14.0f,
                                impuritiesUpLim = 2.00f,
                                brokenUpLim = 5.00f,
                                ardidoUpLim = 3.00f,
                                mofadoUpLim = 15.00f,
                                carunchadoUpLim = 4.00f
                            )
                        )
                    }
                }
            })
            .build()
    }

    // ----- DAOs -----
    @Provides fun provideInputDiscountSojaDao(db: AppDatabase): InputDiscountSojaDao = db.inputDiscountSojaDao()
    @Provides fun provideDiscountSojaDao(db: AppDatabase): DiscountSojaDao = db.discountSojaDao()
    @Provides fun provideDisqualificationSojaDao(db: AppDatabase): DisqualificationSojaDao = db.disqualificationSojaDao()
    @Provides fun provideColorClassificationSojaDao(db: AppDatabase): ColorClassificationSojaDao = db.colorClassificationSojaDao()
    @Provides fun provideSampleSojaDao(db: AppDatabase): SampleSojaDao = db.sampleSojaDao()
    @Provides fun provideClassificationSojaDao(db: AppDatabase): ClassificationSojaDao = db.classificationSojaDao()
    @Provides fun provideLimitSojaDao(db: AppDatabase): LimitSojaDao = db.limitSojaDao()

    @Provides fun provideLimitMilhoDao(db: AppDatabase): LimitMilhoDao = db.limitMilhoDao()
    @Provides fun provideDiscountMilhoDao(db: AppDatabase): DiscountMilhoDao = db.discountMilhoDao()
    @Provides fun provideInputDiscountMilhoDao(db: AppDatabase): InputDiscountMilhoDao = db.inputDiscountMilhoDao()
    @Provides fun provideClassificationMilhoDao(db: AppDatabase): ClassificationMilhoDao = db.classificationMilhoDao()
    @Provides fun provideSampleMilhoDao(db: AppDatabase): SampleMilhoDao = db.sampleMilhoDao()
}

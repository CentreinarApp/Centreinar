package com.example.centreinar.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.centreinar.data.local.AppDatabase
import com.example.centreinar.data.local.dao.LimitMilhoDao
import com.example.centreinar.data.local.dao.LimitSojaDao
import com.example.centreinar.LimitSoja
import com.example.centreinar.data.local.entity.LimitMilho
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
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                CoroutineScope(Dispatchers.IO).launch {
                    val database = provideAppDatabase(context)
                    val sojaDao = database.limitSojaDao()
                    val milhoDao = database.limitMilhoDao()

                    // =============================
                    // 🚀 Inicialização SOJA (MAPA)
                    // =============================
                    sojaDao.insertLimit(
                        LimitSoja(
                            id = 0, source = 0, grain = "soja", group = 1, type = 1,
                            moistureLowerLim = 14f, moistureUpLim = 14f,
                            impuritiesLowerLim = 0f, impuritiesUpLim = 1f,
                            brokenCrackedDamagedLowerLim = 0f, brokenCrackedDamagedUpLim = 8f,
                            greenishLowerLim = 0f, greenishUpLim = 2f,
                            burntLowerLim = 0f, burntUpLim = 0.3f,
                            moldyLowerLim = 0f, moldyUpLim = 0.5f,
                            burntOrSourLowerLim = 0f, burntOrSourUpLim = 1f,
                            spoiledTotalLowerLim = 0f, spoiledTotalUpLim = 4f
                        )
                    )

                    sojaDao.insertLimit(
                        LimitSoja(
                            id = 0, source = 0, grain = "soja", group = 1, type = 2,
                            moistureLowerLim = 14f, moistureUpLim = 14f,
                            impuritiesLowerLim = 0f, impuritiesUpLim = 1f,
                            brokenCrackedDamagedLowerLim = 0f, brokenCrackedDamagedUpLim = 15f,
                            greenishLowerLim = 0f, greenishUpLim = 4f,
                            burntLowerLim = 0f, burntUpLim = 1f,
                            moldyLowerLim = 0f, moldyUpLim = 1.5f,
                            burntOrSourLowerLim = 0f, burntOrSourUpLim = 2f,
                            spoiledTotalLowerLim = 0f, spoiledTotalUpLim = 6f
                        )
                    )

                    sojaDao.insertLimit(
                        LimitSoja(
                            id = 0, source = 0, grain = "soja", group = 2, type = 1,
                            moistureLowerLim = 14f, moistureUpLim = 14f,
                            impuritiesLowerLim = 0f, impuritiesUpLim = 1f,
                            brokenCrackedDamagedLowerLim = 0f, brokenCrackedDamagedUpLim = 30f,
                            greenishLowerLim = 0f, greenishUpLim = 8f,
                            burntLowerLim = 0f, burntUpLim = 1f,
                            moldyLowerLim = 0f, moldyUpLim = 6f,
                            burntOrSourLowerLim = 0f, burntOrSourUpLim = 4f,
                            spoiledTotalLowerLim = 0f, spoiledTotalUpLim = 8f
                        )
                    )

                    // =============================
                    // 🚀 Inicialização MILHO (MAPA)
                    // =============================
                    milhoDao.insertLimit(
                        LimitMilho(
                            id = 0, source = 0, grain = "milho", group = 1, type = 1,
                            moistureUpLim = 14f, impuritiesUpLim = 1f,
                            brokenUpLim = 3f, ardidoUpLim = 1f,
                            mofadoUpLim = 6f, carunchadoUpLim = 2f
                        )
                    )

                    milhoDao.insertLimit(
                        LimitMilho(
                            id = 0, source = 0, grain = "milho", group = 1, type = 2,
                            moistureUpLim = 14f, impuritiesUpLim = 1.5f,
                            brokenUpLim = 4f, ardidoUpLim = 2f,
                            mofadoUpLim = 10f, carunchadoUpLim = 3f
                        )
                    )

                    milhoDao.insertLimit(
                        LimitMilho(
                            id = 0, source = 0, grain = "milho", group = 1, type = 3,
                            moistureUpLim = 14f, impuritiesUpLim = 2f,
                            brokenUpLim = 5f, ardidoUpLim = 3f,
                            mofadoUpLim = 15f, carunchadoUpLim = 4f
                        )
                    )
                }
            }
        }).build()
    }

    @Provides
    fun provideLimitSojaDao(database: AppDatabase): LimitSojaDao = database.limitSojaDao()

    @Provides
    fun provideLimitMilhoDao(database: AppDatabase): LimitMilhoDao = database.limitMilhoDao()
}

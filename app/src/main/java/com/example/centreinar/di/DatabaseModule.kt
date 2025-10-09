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

        val dbBuilder = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database.db"
        )

        dbBuilder.addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // Inicialização de dados assíncrona (CORREÇÃO FINAL DE LÓGICA E NOMES)
                CoroutineScope(Dispatchers.IO).launch {

                    val appDatabaseInstance = dbBuilder.build()
                    val sojaDao = appDatabaseInstance.limitSojaDao()
                    val milhoDao = appDatabaseInstance.limitMilhoDao()

                    // ----------------------------------------------------
                    // INSERÇÃO DOS LIMITES OFICIAIS (MAPA - SOJA)
                    // Valores baseados na IN MAPA Nº 11/2007
                    // ----------------------------------------------------

                    // === 🅰️ SOJA: LIMITE OFICIAL GRUPO I (Tipo 1) ===
                    sojaDao.insertLimit(
                        LimitSoja(
                            source = 0, grain = "Soja", group = 1, type = 1,
                            impuritiesLowerLim = 0.0f, impuritiesUpLim = 1.0f, // Matérias Estranhas/Impurezas
                            moistureLowerLim = 0.0f, moistureUpLim = 14.0f, // Umidade (recomendado máx)
                            brokenCrackedDamagedLowerLim = 0.0f, brokenCrackedDamagedUpLim = 8.0f, // Partidos/Quebrados/Amassados
                            greenishLowerLim = 0.0f, greenishUpLim = 2.0f, // Esverdeados
                            burntLowerLim = 0.0f, burntUpLim = 0.3f, // Máximo de Queimados
                            burntOrSourLowerLim = 0.0f, burntOrSourUpLim = 1.0f, // Total de Ardidos e Queimados
                            moldyLowerLim = 0.0f, moldyUpLim = 0.5f, // Mofados
                            spoiledTotalLowerLim = 0.0f, spoiledTotalUpLim = 4.0f // Total de Avariados
                        )
                    )

                    // === 🅱️ SOJA: LIMITE OFICIAL GRUPO II (Padrão Básico) ===
                    sojaDao.insertLimit(
                        LimitSoja(
                            source = 0, grain = "Soja", group = 2, type = 1,
                            impuritiesLowerLim = 0.0f, impuritiesUpLim = 1.0f, // Matérias Estranhas/Impurezas
                            moistureLowerLim = 0.0f, moistureUpLim = 14.0f, // Umidade (recomendado máx)
                            brokenCrackedDamagedLowerLim = 0.0f, brokenCrackedDamagedUpLim = 30.0f, // Partidos/Quebrados/Amassados
                            greenishLowerLim = 0.0f, greenishUpLim = 8.0f, // Esverdeados
                            burntLowerLim = 0.0f, burntUpLim = 1.0f, // Máximo de Queimados
                            burntOrSourLowerLim = 0.0f, burntOrSourUpLim = 4.0f, // Total de Ardidos e Queimados
                            moldyLowerLim = 0.0f, moldyUpLim = 6.0f, // Mofados
                            spoiledTotalLowerLim = 0.0f, spoiledTotalUpLim = 8.0f // Total de Avariados
                        )
                    )

                    // ----------------------------------------------------
                    // INSERÇÃO DOS LIMITES OFICIAIS (MAPA - MILHO)
                    // NOMES DAS VARIÁVEIS AJUSTADAS PARA LimitMilho ENTIDADE
                    // ----------------------------------------------------

                    // === 🅰️ MILHO: LIMITE OFICIAL TIPO 1 ===
                    milhoDao.insertLimit(
                        LimitMilho(
                            source = 0, grain = "Milho", group = 1, type = 1,
                            moistureUpLim = 14.0f,
                            impuritiesUpLim = 1.00f,
                            brokenUpLim = 3.00f,
                            ardidoUpLim = 1.00f, // Ardidos
                            mofadoUpLim = 6.00f, // Mofados (Avariados Total do Tipo 1)
                            carunchadoUpLim = 2.00f
                        )
                    )

                    // === 🅱️ MILHO: LIMITE OFICIAL TIPO 2 ===
                    milhoDao.insertLimit(
                        LimitMilho(
                            source = 0, grain = "Milho", group = 1, type = 2,
                            moistureUpLim = 14.0f,
                            impuritiesUpLim = 1.50f,
                            brokenUpLim = 4.00f,
                            ardidoUpLim = 2.00f, // Ardidos
                            mofadoUpLim = 10.00f, // Mofados (Avariados Total do Tipo 2)
                            carunchadoUpLim = 3.00f
                        )
                    )

                    // === 🇨 MILHO: LIMITE OFICIAL TIPO 3 ===
                    milhoDao.insertLimit(
                        LimitMilho(
                            source = 0, grain = "Milho", group = 1, type = 3,
                            moistureUpLim = 14.0f,
                            impuritiesUpLim = 2.00f,
                            brokenUpLim = 5.00f,
                            ardidoUpLim = 3.00f, // Ardidos
                            mofadoUpLim = 15.00f, // Mofados (Avariados Total do Tipo 3)
                            carunchadoUpLim = 4.00f
                        )
                    )
                }
            }
        })

        return dbBuilder.build()
    }

    // ----- DAOs (Todos os DAOs de Soja e Milho) -----
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
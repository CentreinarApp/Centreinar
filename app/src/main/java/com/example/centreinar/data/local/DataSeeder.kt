package com.example.centreinar.data.local

import com.example.centreinar.LimitSoja
import com.example.centreinar.data.local.dao.LimitMilhoDao
import com.example.centreinar.data.local.dao.LimitSojaDao
import com.example.centreinar.data.local.entity.LimitMilho

/*
 * Responsável por popular o banco com os dados iniciais obrigatórios.
 * Garante que os limites existam antes de qualquer query ser executada.
*/

class DataSeeder(
    private val limitSojaDao: LimitSojaDao,
    private val limitMilhoDao: LimitMilhoDao
) {
    suspend fun seedAll() {
        seedSoja()
        seedMilho()
    }

    // -------------------------------------------------------------------------
    // Soja — Limites Oficiais (source = 0)
    // -------------------------------------------------------------------------

    private suspend fun seedSoja() {
        limitSojaDao.insertLimit(
            LimitSoja(
                source = 0, grain = "Soja", group = 1, type = 1,
                impuritiesLowerLim = 0f, impuritiesUpLim = 1.0f,
                moistureLowerLim = 0f,   moistureUpLim = 14.0f,
                brokenCrackedDamagedLowerLim = 0f, brokenCrackedDamagedUpLim = 8.0f,
                greenishLowerLim = 0f,   greenishUpLim = 2.0f,
                burntLowerLim = 0f,      burntUpLim = 0.3f,
                burntOrSourLowerLim = 0f, burntOrSourUpLim = 1.0f,
                moldyLowerLim = 0f,      moldyUpLim = 0.5f,
                spoiledTotalLowerLim = 0f, spoiledTotalUpLim = 4.0f
            )
        )
        limitSojaDao.insertLimit(
            LimitSoja(
                source = 0, grain = "Soja", group = 1, type = 2,
                impuritiesLowerLim = 0f, impuritiesUpLim = 1.0f,
                moistureLowerLim = 0f,   moistureUpLim = 14.0f,
                brokenCrackedDamagedLowerLim = 0f, brokenCrackedDamagedUpLim = 15.0f,
                greenishLowerLim = 0f,   greenishUpLim = 4.0f,
                burntLowerLim = 0f,      burntUpLim = 1.0f,
                burntOrSourLowerLim = 0f, burntOrSourUpLim = 2.0f,
                moldyLowerLim = 0f,      moldyUpLim = 1.5f,
                spoiledTotalLowerLim = 0f, spoiledTotalUpLim = 6.0f
            )
        )
        limitSojaDao.insertLimit(
            LimitSoja(
                source = 0, grain = "Soja", group = 2, type = 1,
                impuritiesLowerLim = 0f, impuritiesUpLim = 1.0f,
                moistureLowerLim = 0f,   moistureUpLim = 14.0f,
                brokenCrackedDamagedLowerLim = 0f, brokenCrackedDamagedUpLim = 30.0f,
                greenishLowerLim = 0f,   greenishUpLim = 8.0f,
                burntLowerLim = 0f,      burntUpLim = 1.0f,
                burntOrSourLowerLim = 0f, burntOrSourUpLim = 4.0f,
                moldyLowerLim = 0f,      moldyUpLim = 6.0f,
                spoiledTotalLowerLim = 0f, spoiledTotalUpLim = 8.0f
            )
        )
    }

    // -------------------------------------------------------------------------
    // Milho — Limites Oficiais (source = 0)
    // -------------------------------------------------------------------------

    private suspend fun seedMilho() {
        // Tipo 1
        limitMilhoDao.insertLimit(
            LimitMilho(
                source = 0, grain = "Milho", group = 1, type = 1,
                moistureUpLim = 14.0f, impuritiesUpLim = 1.00f,
                brokenUpLim = 3.00f,   ardidoUpLim = 1.00f,
                mofadoUpLim = 1.00f,   carunchadoUpLim = 2.00f,
                spoiledTotalUpLim = 6.00f
            )
        )
        // Tipo 2
        limitMilhoDao.insertLimit(
            LimitMilho(
                source = 0, grain = "Milho", group = 1, type = 2,
                moistureUpLim = 14.0f, impuritiesUpLim = 1.50f,
                brokenUpLim = 4.00f,   ardidoUpLim = 2.00f,
                mofadoUpLim = 2.00f,   carunchadoUpLim = 3.00f,
                spoiledTotalUpLim = 10.00f
            )
        )
        // Tipo 3
        limitMilhoDao.insertLimit(
            LimitMilho(
                source = 0, grain = "Milho", group = 1, type = 3,
                moistureUpLim = 14.0f, impuritiesUpLim = 2.00f,
                brokenUpLim = 5.00f,   ardidoUpLim = 3.00f,
                mofadoUpLim = 3.00f,   carunchadoUpLim = 4.00f,
                spoiledTotalUpLim = 15.00f
            )
        )
        // Fora de Tipo
        limitMilhoDao.insertLimit(
            LimitMilho(
                source = 0, grain = "Milho", group = 1, type = 0,
                moistureUpLim = 14.0f, impuritiesUpLim = 2.00f,
                brokenUpLim = 5.00f,   ardidoUpLim = 5.00f,
                mofadoUpLim = 3.00f,   carunchadoUpLim = 8.00f,
                spoiledTotalUpLim = 20.00f
            )
        )
    }
}
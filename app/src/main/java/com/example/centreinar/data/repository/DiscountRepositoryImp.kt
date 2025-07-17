package com.example.centreinar.data.repository

import com.example.centreinar.Discount
import com.example.centreinar.InputDiscount
import com.example.centreinar.Limit
import com.example.centreinar.data.local.dao.ClassificationDao
import com.example.centreinar.data.local.dao.DiscountDao
import com.example.centreinar.data.local.dao.LimitDao
import com.example.centreinar.data.local.dao.SampleDao
import com.example.centreinar.util.Utilities
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscountRepositoryImp @Inject constructor(
    private val limitDao: LimitDao,
    private val classificationDao: ClassificationDao,
    private val sampleDao: SampleDao,
    private val discountDao: DiscountDao,
    private val tools : Utilities
): DiscountRepository {
    override suspend fun calculateDiscount(
        grain: String,
        group: Int,
        tipo: Int,
        sample: InputDiscount,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long {

        val limit: Map<String,Float> = getLimitsByType(grain, group, tipo, sample.limitSource) // change request : get limit by id instead
        return calculateDiscount(grain = grain, group = group,tipo = tipo, sample = sample,limit = limit, doesTechnicalLoss = doesTechnicalLoss,doesClassificationLoss = doesClassificationLoss,doesDeduction = doesDeduction)
    }

    override suspend fun calculateDiscount(grain:String, group:Int, tipo:Int, sample: InputDiscount, limit: Map<String,Float>, doesTechnicalLoss:Boolean,doesClassificationLoss:Boolean, doesDeduction:Boolean): Long {

        val lotWeight =  sample.lotWeight
        val storageDays  = sample.daysOfStorage
        val deductionValue = sample.deductionValue
        var technicalLoss  = 0.0f
        val lotPrice = sample.lotPrice

        //impurities and humidity loss
        val impuritiesLoss = tools.calculateDifference(sample.foreignMattersAndImpurities, limit["impurities"]!!)

        val humidityLoss = tools.calculateDifference(sample.humidity , limit["humidity"]!!)

        val impuritiesLossFinal = (impuritiesLoss/100) * lotWeight

        val humidityLossFinal = (humidityLoss/100) * (lotWeight - impuritiesLossFinal)

        var impuritiesAndHumidityLoss = humidityLossFinal+impuritiesLossFinal

        if(doesTechnicalLoss){
            technicalLoss = calculateTechnicalLoss(humidityAndImpuritiesLoss = impuritiesAndHumidityLoss, storageDays = storageDays, lotWeight = lotWeight)
        }

        impuritiesAndHumidityLoss += technicalLoss
        //Classification Loss

        var burntLoss  = 0.0f
        var burntOrSourLoss = 0.0f
        var brokenLoss = 0.0f
        var greenishLoss = 0.0f
        var moldyLoss = 0.0f
        var spoiledLoss = 0.0f
        var classificationDiscount  = 0.0f
        var deduction = 0.0f

        if(doesClassificationLoss){

            burntLoss = tools.calculateDifference(sample.burnt,limit["burnt"]!!)

            burntOrSourLoss = tools.calculateDifference(sample.burntOrSour - burntLoss, limit["burntOrSour"]!!)

            moldyLoss = tools.calculateDifference(sample.moldy, limit["moldy"]!!)

            spoiledLoss = tools.calculateDifference((sample.spoiled) - moldyLoss - burntLoss - burntOrSourLoss,limit["spoiled"]!!)

            greenishLoss = tools.calculateDifference(sample.greenish,limit["greenish"]!!)

            brokenLoss = tools.calculateDifference(sample.brokenCrackedDamaged, limit["broken"]!!)

            classificationDiscount = ((brokenLoss + burntLoss + burntOrSourLoss + moldyLoss + greenishLoss + spoiledLoss )/100 ) * lotWeight

            if(doesDeduction){
                deduction = calculateDeduction(deductionValue,classificationDiscount)
                classificationDiscount = deduction
            }

        }
        val finalLoss = classificationDiscount + impuritiesAndHumidityLoss + technicalLoss

        val finalWeight = lotWeight - finalLoss

        //prices

        val impuritiesLossPrice = (lotPrice * impuritiesLoss/100)
        val humidityLossPrice= ((lotPrice-impuritiesLossPrice) * humidityLoss/100)
        var impuritiesAndHumidityLossPrice = impuritiesLossPrice + humidityLossPrice
        val technicalLossPrice = (lotPrice / lotWeight) * technicalLoss
        impuritiesAndHumidityLossPrice += technicalLossPrice


        val burntLossPrice = lotPrice * burntLoss / 100
        val burntOrSourLossPrice = lotPrice * burntOrSourLoss / 100
        val brokenLossPrice = lotPrice * brokenLoss / 100
        val greenishLossPrice = lotPrice * greenishLoss / 100
        val moldyLossPrice = lotPrice * moldyLoss / 100
        val spoiledLossPrice = lotPrice * spoiledLoss / 100

        val classificationDiscountPrice = burntLossPrice + burntOrSourLossPrice + brokenLossPrice + greenishLossPrice + moldyLossPrice + spoiledLossPrice

        val finalDiscountPrice = classificationDiscountPrice + impuritiesAndHumidityLossPrice
        val finalWeightPrice = sample.lotPrice - finalDiscountPrice


        val discount =
        Discount(
            inputDiscountId = sample.id,
           impuritiesLoss = impuritiesLoss, humidityLoss = humidityLoss, technicalLoss =  technicalLoss, burntLoss =  burntLoss,
           burntOrSourLoss = burntOrSourLoss, moldyLoss =  moldyLoss, spoiledLoss = spoiledLoss, classificationDiscount = classificationDiscount,
           greenishLoss = greenishLoss, brokenLoss = brokenLoss,
            burntLossPrice = burntLossPrice, burntOrSourLossPrice = burntOrSourLossPrice, brokenLossPrice = brokenLossPrice, greenishLossPrice = greenishLossPrice,
            moldyLossPrice = moldyLossPrice, spoiledLossPrice = spoiledLossPrice, classificationDiscountPrice = classificationDiscountPrice,
            humidityAndImpuritiesDiscountPrice = impuritiesAndHumidityLossPrice,
           humidityAndImpuritiesDiscount = impuritiesAndHumidityLoss, deductionValue = deductionValue ,
            deduction = deduction, finalDiscount = finalLoss, finalDiscountPrice = finalDiscountPrice, impuritiesLossPrice = impuritiesLossPrice, humidityLossPrice = humidityLossPrice, technicalLossPrice = technicalLossPrice ,  finalWeightPrice = finalWeightPrice , finalWeight = finalWeight )

        return discountDao.insert(discount)
    }

    override suspend fun calculateTechnicalLoss(
        storageDays: Int,
        humidityAndImpuritiesLoss: Float,
        lotWeight: Float
    ): Float {
        return (0.0001f * storageDays) * (lotWeight-humidityAndImpuritiesLoss)
    }

    override suspend fun getLimitsByType(grain:String, group:Int, tipo:Int, limitSource:Int): Map<String,Float> {
        val limit:Limit = limitDao.getLimitsByType(grain, group, tipo, limitSource)
        return mapOf(
           "humidity" to 14.0f,
           "impurities" to 1.0f,
           "broken" to limit.brokenCrackedDamagedUpLim,
           "greenish" to limit.greenishUpLim,
           "burnt" to limit.burntUpLim,
           "burntOrSour" to limit.burntOrSourUpLim,
           "moldy" to limit.moldyUpLim,
           "spoiled" to limit.spoiledTotalUpLim
        )
    }
    override suspend fun getDiscountById(id: Long): Discount? {
        return discountDao.getDiscountById(id.toInt())
  }

    override suspend fun calculateDeduction(
        deductionValue: Float,
        classificationLoss: Float
    ): Float {
       return ((100 - deductionValue)/100 * classificationLoss)
    }

    override suspend fun setLimit(
        grain:String,
        group:Int,
        type:Int,
        impurities:Float,
        brokenCrackedDamaged: Float,
        greenish: Float,
        burnt:Float,
        burntOrSour:Float,
        moldy:Float,
        spoiled:Float
    ):Long {
        val lastSource = limitDao.getLastSource()
        val source = lastSource + 1
        val limit = Limit(
            source = source,
            grain = grain,
            group = group,
            type = type,
            impuritiesLowerLim = 0.0f,
            impuritiesUpLim = impurities,
            brokenCrackedDamagedLowerLim = 0.0f,
            brokenCrackedDamagedUpLim = brokenCrackedDamaged,
            greenishLowerLim = 0.0f,
            greenishUpLim = greenish,
            burntLowerLim = 0.0f,
            burntUpLim = burnt,
            burntOrSourLowerLim = 0.0f,
            burntOrSourUpLim = burntOrSour,
            moldyLowerLim = 0.0f,
            moldyUpLim = moldy,
            spoiledTotalLowerLim = 0.0f,
            spoiledTotalUpLim = spoiled
        )
        return limitDao.insertLimit(limit)
    }

    override suspend fun getLimit(grain: String, group: Int, tipo: Int, source: Int):Limit {
        return limitDao.getLimitsByType(grain,group,tipo,source)
    }

    override suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float> {
        val limit = limitDao.getLimitsByType(grain,group,1,0)
        return mapOf(
            "impuritiesLowerLim" to limit.impuritiesLowerLim,
            "impuritiesUpLim" to limit.impuritiesUpLim,
            "brokenLowerLim" to limit.brokenCrackedDamagedLowerLim,
            "brokenUpLim" to limit.brokenCrackedDamagedUpLim,
            "greenishLowerLim" to limit.greenishLowerLim,
            "greenishUpLim" to limit.greenishUpLim,
            "burntLowerLim" to limit.burntLowerLim,
            "burntUpLim" to limit.burntUpLim,
            "burntOrSourLowerLim" to limit.burntOrSourLowerLim,
            "burntOrSourUpLim" to limit.burntOrSourUpLim,
            "moldyLowerLim" to limit.moldyLowerLim,
            "moldyUpLim" to limit.moldyUpLim,
            "spoiledTotalLowerLim" to limit.spoiledTotalLowerLim,
            "spoiledTotalUpLim" to limit.spoiledTotalUpLim
        )
    }
}
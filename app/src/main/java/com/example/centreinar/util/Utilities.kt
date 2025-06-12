package com.example.centreinar.util

import com.example.centreinar.domain.model.LimitCategory
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Utilities @Inject constructor()  {
   fun findCategoryForValue(intervals: List<LimitCategory>, value: Float): Int {
        if(value == 0.0f) return 1
        for (interval in intervals) {
            if (interval.lowerL < value && interval.upperL >= value) {
                return interval.type
            }
        }
        return 7
    }
    fun calculateDefectPercentage(defect: Float, weight: Float): Float {
       val percentage: Float = (defect * 100) / weight
        return percentage
            .toBigDecimal()
            .setScale(2, RoundingMode.HALF_UP)
            .toFloat()
    }

    fun calculateDifference(defect:Float,defectTolerance:Float): Float{
        if(defectTolerance >= defect ) {
            return 0f
        }
        return ((defect - defectTolerance)/ (100-defectTolerance) ) * 100
    }

    fun calculatePercentage(item:Float, weight: Float):Float{
        val percentage: Float = (item * 100) / weight
        return percentage
            .toBigDecimal()
            .setScale(2, RoundingMode.HALF_UP)
            .toFloat()
    }
}
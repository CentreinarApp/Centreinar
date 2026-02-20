package com.example.centreinar.data.local.entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


    @Entity(tableName = "color_group_classification_milho")
    data class ColorClassificationMilho(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,

        @ColumnInfo(name = "grain") val grain: String = "milho",
        @ColumnInfo(name = "classificationId") val classificationId: Long,

        // --- Campo de classe (Cor) ---
        @ColumnInfo(name = "yellowPercentage") val yellowPercentage: Float,
        @ColumnInfo(name = "otherColorPercentage") val otherColorPercentage: Float,
        @ColumnInfo(name = "class") val framingClass: String = "", // amarela, branca, cores, misturada

        // --- Campo de grupo (Forma) ---
        @ColumnInfo(name = "duroPercentage") val duroPercentage: Float = 0f,
        @ColumnInfo(name = "dentadoPercentage") val dentadoPercentage: Float = 0f,
        @ColumnInfo(name = "semiDuroPercentage") val semiDuroPercentage: Float = 0f,
        @ColumnInfo(name = "group") val framingGroup: String = "" // DURO, DENTADO, SEMIDURO

    )

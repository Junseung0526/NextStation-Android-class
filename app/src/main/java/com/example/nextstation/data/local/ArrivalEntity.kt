package com.example.nextstation.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nextstation.domain.model.ArrivalInfo

@Entity(tableName = "arrival_history")
data class ArrivalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val destinationName: String,
    val arrivalTime: Long,
    val phoneNumber: String,
    val message: String,
    val isCompleted: Boolean
) {
    fun toDomain(): ArrivalInfo {
        return ArrivalInfo(id, destinationName, arrivalTime, phoneNumber, message, isCompleted)
    }

    companion object {
        fun fromDomain(info: ArrivalInfo): ArrivalEntity {
            return ArrivalEntity(info.id, info.destinationName, info.arrivalTime, info.phoneNumber, info.message, info.isCompleted)
        }
    }
}
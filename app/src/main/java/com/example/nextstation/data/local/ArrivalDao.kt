package com.example.nextstation.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArrivalDao {
    @Query("SELECT * FROM arrival_history ORDER BY arrivalTime DESC")
    fun getArrivalHistory(): Flow<List<ArrivalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArrivalInfo(entity: ArrivalEntity)

    @Delete
    suspend fun deleteArrivalInfo(entity: ArrivalEntity)

    @Query("SELECT * FROM arrival_history WHERE id = :id")
    suspend fun getArrivalInfoById(id: Int): ArrivalEntity?
}
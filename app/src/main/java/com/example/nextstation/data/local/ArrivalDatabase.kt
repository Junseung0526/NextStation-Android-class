package com.example.nextstation.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ArrivalEntity::class], version = 1, exportSchema = false)
abstract class ArrivalDatabase : RoomDatabase() {
    abstract val dao: ArrivalDao
}
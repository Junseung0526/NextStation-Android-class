package com.example.nextstation.di

import android.content.Context
import androidx.room.Room
import com.example.nextstation.data.local.ArrivalDao
import com.example.nextstation.data.local.ArrivalDatabase
import com.example.nextstation.data.repository.ArrivalRepositoryImpl
import com.example.nextstation.domain.repository.ArrivalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideArrivalDatabase(@ApplicationContext context: Context): ArrivalDatabase {
        return Room.databaseBuilder(
            context,
            ArrivalDatabase::class.java,
            "arrival_db"
        ).build()
    }

    @Provides
    fun provideArrivalDao(database: ArrivalDatabase): ArrivalDao {
        return database.dao
    }

    @Provides
    @Singleton
    fun provideArrivalRepository(dao: ArrivalDao): ArrivalRepository {
        return ArrivalRepositoryImpl(dao)
    }
}